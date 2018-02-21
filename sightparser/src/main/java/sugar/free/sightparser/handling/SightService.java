package sugar.free.sightparser.handling;

import android.annotation.SuppressLint;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.widget.Toast;

import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import sugar.free.sightparser.DataStorage;
import sugar.free.sightparser.Pref;
import sugar.free.sightparser.SerializationUtils;
import sugar.free.sightparser.applayer.messages.AppLayerMessage;
import sugar.free.sightparser.applayer.messages.status.PumpStatusMessage;
import sugar.free.sightparser.error.DisconnectedError;
import sugar.free.sightparser.error.NotAuthorizedError;
import sugar.free.sightparser.pipeline.Pipeline;
import sugar.free.sightparser.pipeline.Status;

public class SightService extends Service {

    public static final String COMPATIBILITY_VERSION = "asclepius";
    private static final int DISCONNECT_DELAY = 20000;
    private static final int MIN_TIMEOUT_WAIT = 4000;
    private static final int MAX_TIMEOUT_WAIT = 60000;
    private static final int TIMEOUT_WAIT_STEP = 1000;
    private static final String SIGHTREMOTE_PACKAGE_NAME = "sugar.free.sightremote";
    private final SparseBooleanArray allowedUid = new SparseBooleanArray();
    private long statusIdCounter = 0;
    private String tempMac;
    private Map<IBinder, IBinder.DeathRecipient> connectedClients = new HashMap<>();
    private ConnectionThread connectionThread;
    private Pipeline pipeline;
    private DataStorage dataStorage;
    private FirewallConstraint firewall;
    private Map<IStatusCallback, IBinder.DeathRecipient> statusCallbackDeathRecipients = new HashMap<>();
    private Map<Long, IStatusCallback> statusCallbackIds = new HashMap<>();
    private Status status = Status.DISCONNECTED;
    private Timer disconnectTimer;
    private Timer timeoutTimer;
    private boolean reconnect;
    private long timeoutWait = MIN_TIMEOUT_WAIT;
    private volatile BluetoothSocket bluetoothSocket = null;
    private long lastAuthPoll = 0;
    private ISightService.Stub binder = new ISightService.Stub() {

        @Override
        public String getRemoteVersion() {
            return COMPATIBILITY_VERSION;
        }

        @Override
        public void pair(String mac, final IBinder binder) throws RemoteException {
            if (verifyAdminCaller("pair")) {
                if (!connectedClients.containsKey(binder)) {
                    Log.d("SightService", "CLIENT CONNECTS TO PUMP");
                    if (disconnectTimer != null) disconnectTimer.cancel();
                    disconnectTimer = new Timer();
                    DeathRecipient deathRecipient = new DeathRecipient() {
                        @Override
                        public void binderDied() {
                            Log.d("SightService", "CLIENT DIED - CONNECT");
                            try {
                                disconnect(binder);
                            } catch (RemoteException e) {
                                e.printStackTrace();
                            }
                        }
                    };
                    connectedClients.put(binder, deathRecipient);
                    binder.linkToDeath(deathRecipient, 0);
                }
                SightService.this.disconnect(false);
                reset();
                tempMac = mac;
                SightService.this.connect(mac, true);
            } else {
                throw new RemoteException("Not authorized");
            }
        }

        @Override
        public boolean isUseable() throws RemoteException {
            return verifyCaller("isUsable") && getDataStorage().contains("DEVICEMAC");
        }

        @Override
        public String getStatus() throws RemoteException {
            if (verifyCaller("getStatus")) {
                return status.name();
            } else {
                return Status.NOT_AUTHORIZED.toString();
            }
        }

        @Override
        public void requestMessage(byte[] message, IMessageCallback callback) throws RemoteException {
            if (verifyCaller("requestMessage")) {
                final AppLayerMessage msg = (AppLayerMessage) SerializationUtils.deserialize(message);
                Answers.getInstance().logCustom(new CustomEvent("Message Requested")
                        .putCustomAttribute("Application", getCallerName())
                        .putCustomAttribute("Message", msg.getClass().getName()));
                if (firewall.isAllowed(msg)) {
                    MessageRequest messageRequest = new MessageRequest(msg, callback, callback.asBinder());
                    if (pipeline != null && status == Status.CONNECTED)
                        pipeline.requestMessage(messageRequest);
                } else {
                    showToast("Blocked by SiteRemote firewall preference" + " :: " + msg.toString());
                    callback.onError(SerializationUtils.serialize(new NotAuthorizedError("Blocked by Firewall preference")));
                }
            } else {
                callback.onError(SerializationUtils.serialize(new NotAuthorizedError("Application not authorized")));
            }
        }

        @Override
        public long registerStatusCallback(final IStatusCallback callback) throws RemoteException {
            final long id = ++statusIdCounter;
            DeathRecipient deathRecipient = new DeathRecipient() {
                @Override
                public void binderDied() {
                    Log.d("SightService", "CLIENT DIED - STATUS");
                    callback.asBinder().unlinkToDeath(statusCallbackDeathRecipients.get(callback), 0);
                    statusCallbackDeathRecipients.remove(callback);
                    statusCallbackIds.remove(id);
                }
            };
            statusCallbackDeathRecipients.put(callback, deathRecipient);
            statusCallbackIds.put(id, callback);
            callback.asBinder().linkToDeath(deathRecipient, 0);
            return id;
        }

        @Override
        public void unregisterStatusCallback(long id) throws RemoteException {
            IStatusCallback callback = statusCallbackIds.get(id);
            callback.asBinder().unlinkToDeath(statusCallbackDeathRecipients.get(callback), 0);
            statusCallbackDeathRecipients.remove(callback);
            statusCallbackIds.remove(id);
        }

        @Override
        public void connect(final IBinder binder) throws RemoteException {
            if (verifyCaller("connect")) {
                Answers.getInstance().logCustom(new CustomEvent("Requested Connection To Pump")
                        .putCustomAttribute("Application", getCallerName()));
                if (!connectedClients.containsKey(binder)) {
                    Log.d("SightService", "CLIENT CONNECTS TO PUMP");
                    if (disconnectTimer != null) disconnectTimer.cancel();
                    disconnectTimer = new Timer();
                    DeathRecipient deathRecipient = new DeathRecipient() {
                        @Override
                        public void binderDied() {
                            Log.d("SightService", "CLIENT DIED - CONNECT");
                            try {
                                disconnect(binder);
                            } catch (RemoteException e) {
                                e.printStackTrace();
                            }
                        }
                    };
                    connectedClients.put(binder, deathRecipient);
                    binder.linkToDeath(deathRecipient, 0);
                    if (getDataStorage().contains("DEVICEMAC"))
                        SightService.this.connect(getDataStorage().get("DEVICEMAC"), false);
                }
            } else {
                // throw exception?
            }
        }

        @Override
        public void disconnect(IBinder binder) throws RemoteException {
            if (connectedClients.containsKey(binder)) {
                Answers.getInstance().logCustom(new CustomEvent("Connection Request Withdrawn")
                        .putCustomAttribute("Application", getCallerName()));
                Log.d("SightService", "CLIENT DISCONNECTS FROM PUMP");
                binder.unlinkToDeath(connectedClients.get(binder), 0);
                connectedClients.remove(binder);
                if (connectedClients.size() == 0 && connectionThread != null) {
                    disconnectTimer = new Timer();
                    disconnectTimer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            SightService.this.disconnect(false);
                        }
                    }, DISCONNECT_DELAY);
                }
            }
        }

        @Override
        public void setPassword(String password) throws RemoteException {
            if (verifyCaller("setPassword")) {
                getDataStorage().set("PASSWORD", password);
                sugar.free.sightparser.applayer.descriptors.Service.REMOTE_CONTROL.setServicePassword(password);
            } else {
                throw new RemoteException("Not authorized");
            }
        }

        @Override
        public void setAuthorized(String packageName, boolean allowed) throws RemoteException {
            if (verifyAdminCaller("setAuthorized")) {
                if (allowed) {
                    getDataStorage().set("package-allowed-" + packageName, "yes");
                } else {
                    if (packageName.startsWith(Pref.CHANGE_PREFS_SPECIAL_CASE)) {
                        firewall.parsePreference(packageName);
                    } else {
                        getDataStorage().remove("package-allowed-" + packageName);
                    }
                }
            } else {
                throw new RemoteException("Not authorized");
            }
        }

        @Override
        public void reset() throws RemoteException {
            if (verifyAdminCaller("reset")) {
                SightService.this.disconnect(false);
                getDataStorage().remove("INCOMINGKEY");
                getDataStorage().remove("OUTGOINGKEY");
                getDataStorage().remove("COMMID");
                getDataStorage().remove("LASTNONCESENT");
                getDataStorage().remove("LASTNONCERECEIVED");
                getDataStorage().remove("DEVICEMAC");
            } else {
                throw new RemoteException("Not authorized");
            }
        }
    };
    private StatusCallback statusCallback = new StatusCallback() {
        @Override
        public void onStatusChange(Status status) {
            Log.d("SightService", "STATUS: " + status);
            SightService.this.status = status;
            if (status == Status.CONNECTED) {
                timeoutTimer.cancel();
                if (tempMac != null) {
                    getDataStorage().set("DEVICEMAC", tempMac);
                    tempMac = null;
                    reconnect = true;
                }
            }
            for (IStatusCallback sc : new ArrayList<>(statusCallbackIds.values()))
                try {
                    sc.onStatusChange(status.name());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            Answers.getInstance().logCustom(new CustomEvent("Connection Status Changed")
                    .putCustomAttribute("Status", status.toString()));
        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        Log.d("SightService", "CLIENT BOUND TO SERVICE");
        Answers.getInstance().logCustom(new CustomEvent("Client Bound To Service")
                .putCustomAttribute("Application", getCallerName()));
        return binder;
    }

    @Override
    public void onRebind(Intent intent) {
        Log.d("SightService", "CLIENT REBOUND TO SERVICE");

        Answers.getInstance().logCustom(new CustomEvent("Client Rebound To Service")
                .putCustomAttribute("Application", getCallerName()));
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d("SightService", "CLIENT UNBOUND FROM SERVICE");
        Answers.getInstance().logCustom(new CustomEvent("Client Unbound From Service")
                .putCustomAttribute("Application", getCallerName()));
        return true;
    }

    public void connect(String mac, boolean pairing) {
        reconnect = !pairing;
        Log.d("SightService", "CONNECT");
        if (connectionThread == null) {
            connectionThread = new ConnectionThread(mac, pairing);
            connectionThread.start();
        }
    }

    public void disconnect(boolean reconnect) {
        this.reconnect = reconnect;
        Log.d("SightService", "DISCONNECT");
        if (connectionThread != null) {
            connectionThread.interrupt();
        }
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        if (getDataStorage().contains("PASSWORD"))
            sugar.free.sightparser.applayer.descriptors.Service.REMOTE_CONTROL.setServicePassword(getDataStorage().get("PASSWORD"));
        if (firewall == null) {
            firewall = new FirewallConstraint(getApplicationContext());
        }
        return START_STICKY;
    }

    public void onDestroy() {
        if (disconnectTimer != null) disconnectTimer.cancel();
        disconnect(false);
    }

    private DataStorage getDataStorage() {
        if (dataStorage == null)
            dataStorage = new DataStorage(getSharedPreferences("sugar.free.sightremote.services.SIGHTSERVICE", MODE_PRIVATE));
        return dataStorage;
    }

    private boolean verifyAdminCaller(String msg) {
        final int callingUid = Binder.getCallingUid();
        final String[] packages = getPackageManager().getPackagesForUid(callingUid);
        if (packages != null) {
            for (String packageName : packages) {
                if (packageName.equals(SIGHTREMOTE_PACKAGE_NAME)) return true;
            }
        }
        return false;
    }

    private boolean verifyCaller(String msg) {
        final int callingUid = Binder.getCallingUid();
        if (allowedUid.get(callingUid)) return true;

        final String[] packages = getPackageManager().getPackagesForUid(callingUid);
        if (packages == null) return false;
        for (String packageName : packages) {
            Log.d("SightService", msg + " package verify: " + packageName + " " + callingUid);
            if (allowedPackage(packageName)) {
                allowedUid.put(callingUid, true);
                return true;
            }
        }
        return false;
    }

    private String getCallerName() {
        return getPackageManager().getNameForUid(Binder.getCallingUid());
    }

    private boolean allowedPackage(String packageName) {
        if (packageName.equals(SIGHTREMOTE_PACKAGE_NAME)) return true;
        if (getDataStorage().get("package-allowed-" + packageName) != null) {
            Log.d("SightService", "Allowing " + packageName + " as previously approved");
            return true;
        }
        synchronized (this) {
            if (System.currentTimeMillis() - lastAuthPoll > 30000) {
                lastAuthPoll = System.currentTimeMillis();
                final Intent intent = getPackageManager().getLaunchIntentForPackage(SIGHTREMOTE_PACKAGE_NAME);
                if (intent != null) {
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra("authorize_poll", packageName);
                    getApplicationContext().startActivity(intent);
                }
            }
        }
        return false;
    }

    private void showToast(String msg) {
        try {
            runOnUiThread(() -> Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show());
        } catch (Exception e) {
            // don't let an error here cause a crash
        }
    }

    private void runOnUiThread(Runnable theRunnable) {
        try {
            final Handler mainHandler = new Handler(getApplicationContext().getMainLooper());
            mainHandler.post(theRunnable);
        } catch (Exception e) {
            // don't let an error here cause a crash
        }
    }

    private class ConnectionThread extends Thread {

        private String mac;
        private boolean pairing;


        public ConnectionThread(String mac, boolean pairing) {
            this.pairing = pairing;
            this.mac = mac;
        }

        @SuppressLint("MissingPermission")
        @Override
        public void run() {

            try {
                pipeline = new Pipeline(getDataStorage(), statusCallback);
                pipeline.setStatus(Status.CONNECTING);
                BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                if (!bluetoothAdapter.isEnabled()) bluetoothAdapter.enable();
                BluetoothDevice bluetoothDevice = bluetoothAdapter.getRemoteDevice(mac);

                if (pairing) removeBond(bluetoothDevice);
                if (bluetoothSocket == null) {
                    bluetoothSocket = bluetoothDevice.createInsecureRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805f9b34fb"));
                }
                /*
                // recommended by android reference but could easily stomp over other things for example G5 discovery
                if (bluetoothAdapter.isDiscovering()) {
                    Log.d("SightService", "Cancelling someone elses discovery!");
                    bluetoothAdapter.cancelDiscovery();
                }
                */
                bluetoothSocket.connect();
                if (timeoutWait != MIN_TIMEOUT_WAIT) {
                    Log.d("SightService", "Resetting timeout from " + timeoutWait + " to " + MIN_TIMEOUT_WAIT);
                    timeoutWait = 4000;
                }
                pipeline.setChannels(bluetoothSocket.getInputStream(), bluetoothSocket.getOutputStream());
                if (pairing) pipeline.establishPairing();
                else pipeline.establishConnection();
                timeoutTimer = new Timer();
                //  final BluetoothSocket bluetoothSocket1 = bluetoothSocket;
                if (!pairing) timeoutTimer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        Log.d("SightService", "TIMEOUT");
                        disconnect(true);
                        try {
                            bluetoothSocket.close();
                            bluetoothSocket = null;
                            timeoutWait = Math.min(timeoutWait + TIMEOUT_WAIT_STEP, MAX_TIMEOUT_WAIT);
                        } catch (IOException e) {
                        }
                    }
                }, timeoutWait);
                while (pipeline.getStatus() != Status.DISCONNECTED && !Thread.currentThread().isInterrupted())
                    pipeline.loopCall();
                timeoutTimer.cancel();
                pipeline.disconnect();
            } catch (IOException e) {
                Log.d("SightService", "IO Exception in state " + pipeline.getStatus() + " " + e);
                //e.printStackTrace();
            } finally {
                pipeline.receive(new DisconnectedError());

                try {
                    // don't close socket if we were connecting
                    if ((pipeline.getStatus() != Status.CONNECTING) && (bluetoothSocket != null)) {
                        Log.d("SightService", "Closing socket");
                        bluetoothSocket.close();
                        bluetoothSocket = null;
                    } else {
                        Log.d("SightService", "Not closing socket");
                        timeoutWait = Math.min(timeoutWait + TIMEOUT_WAIT_STEP, MAX_TIMEOUT_WAIT);
                        Log.d("SightService", "sleeping " + timeoutWait);
                        try {
                            Thread.sleep(timeoutWait);
                        } catch (InterruptedException e) {
                            //
                        }
                        Log.d("SightService", "waking");
                    }
                } catch (IOException e1) {
                    //
                }

                if (pipeline.getStatus() != Status.DISCONNECTED)
                    pipeline.setStatus(Status.DISCONNECTED);
                pipeline = null;
                connectionThread = null;

                if (reconnect) {
                    connect(mac, pairing);
                }
            }
        }

        private void removeBond(BluetoothDevice bluetoothDevice) {
            try {
                Method method = bluetoothDevice.getClass().getMethod("removeBond", (Class[]) null);
                method.invoke(bluetoothDevice);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
