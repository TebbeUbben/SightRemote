package sugar.free.sightparser.handling;

import android.annotation.SuppressLint;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import sugar.free.sightparser.DataStorage;
import sugar.free.sightparser.SerializationUtils;
import sugar.free.sightparser.applayer.AppLayerMessage;
import sugar.free.sightparser.applayer.status.PumpStatusMessage;
import sugar.free.sightparser.error.DisconnectedError;
import sugar.free.sightparser.pipeline.Pipeline;
import sugar.free.sightparser.pipeline.Status;

public class SightService extends Service {

    private static final int DISCONNECT_DELAY = 5000;

    private long statusIdCounter = 0;
    private String tempMac;
    private Map<IBinder, IBinder.DeathRecipient> connectedClients = new HashMap<>();
    private ConnectionThread connectionThread;
    private Pipeline pipeline;
    private DataStorage dataStorage;
    private Map<IStatusCallback, IBinder.DeathRecipient> statusCallbackDeathRecipients = new HashMap<>();
    private Map<Long, IStatusCallback> statusCallbackIds = new HashMap<>();
    private Status status = Status.DISCONNECTED;
    private Timer disconnectTimer;
    private Timer timeoutTimer;
    private boolean reconnect;
    private Timer pingTimer;
    private StatusCallback statusCallback = new StatusCallback() {
        @Override
        public void onStatusChange(Status status) {
            Log.d("SightService", "STATUS: " + status);
            SightService.this.status = status;
            if (status == Status.CONNECTED) {
                timeoutTimer.cancel();
                pingTimer = new Timer();
                pingTimer.schedule(new TimerTask() {

                    private boolean received = true;

                    @Override
                    public void run() {
                        if (received) {
                            received = false;
                            pipeline.requestMessage(new MessageRequest(new PumpStatusMessage(), new IMessageCallback() {
                                @Override
                                public IBinder asBinder() {
                                    return null;
                                }

                                @Override
                                public void onMessage(byte[] getClass) throws RemoteException {
                                    received = true;
                                }

                                @Override
                                public void onError(byte[] error) throws RemoteException {
                                    received = true;
                                }
                            }, binder.asBinder()));
                        } else {
                            disconnect(true);
                        }
                    }
                }, 2000, 2000);
                if (tempMac != null) {
                    getDataStorage().set("DEVICEMAC", tempMac);
                    tempMac = null;
                    reconnect = true;
                }
            } else if (status == Status.DISCONNECTED && pingTimer != null) pingTimer.cancel();
            for (IStatusCallback sc : new ArrayList<>(statusCallbackIds.values()))
                try {
                    sc.onStatusChange(status.name());
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        Log.d("SightService", "CLIENT BOUND TO SERVICE");
        return binder;
    }

    @Override
    public void onRebind(Intent intent) {
        Log.d("SightService", "CLIENT REBOUND TO SERVICE");
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d("SightService", "CLIENT UNBOUND FROM SERVICE");
        return true;
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
            BluetoothSocket bluetoothSocket = null;
            try {
                pipeline = new Pipeline(getDataStorage(), statusCallback);
                pipeline.setStatus(Status.CONNECTING);
                BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                if (!bluetoothAdapter.isEnabled()) bluetoothAdapter.enable();
                BluetoothDevice bluetoothDevice = bluetoothAdapter.getRemoteDevice(mac);
                if (pairing) removeBond(bluetoothDevice);
                bluetoothSocket = bluetoothDevice.createInsecureRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805f9b34fb"));
                bluetoothSocket.connect();
                pipeline.setChannels(bluetoothSocket.getInputStream(), bluetoothSocket.getOutputStream());
                if (pairing) pipeline.establishPairing();
                else pipeline.establishConnection();
                timeoutTimer = new Timer();
                final BluetoothSocket bluetoothSocket1 = bluetoothSocket;
                if (!pairing) timeoutTimer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        Log.d("SightService", "TIMEOUT");
                        disconnect(true);
                        try {
                            bluetoothSocket1.close();
                        } catch (IOException e) {
                        }
                    }
                }, 4000);
                while (pipeline.getStatus() != Status.DISCONNECTED && !Thread.currentThread().isInterrupted())
                    pipeline.loopCall();
                timeoutTimer.cancel();
                pipeline.disconnect();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                pipeline.receive(new DisconnectedError());
                if (pipeline.getStatus() != Status.DISCONNECTED) pipeline.setStatus(Status.DISCONNECTED);
                pipeline = null;
                connectionThread = null;
                try {
                    if (bluetoothSocket != null) bluetoothSocket.close();
                } catch (IOException e1) {
                }
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
            sugar.free.sightparser.applayer.Service.REMOTE_CONTROL.setServicePassword(getDataStorage().get("PASSWORD"));
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

    private ISightService.Stub binder = new ISightService.Stub() {
        @Override
        public void pair(String mac, final IBinder binder) throws RemoteException {
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
        }

        @Override
        public boolean isUseable() throws RemoteException {
            return getDataStorage().contains("DEVICEMAC");
        }

        @Override
        public String getStatus() throws RemoteException {
            return status.name();
        }

        @Override
        public void requestMessage(byte[] message, IMessageCallback callback) throws RemoteException {
            MessageRequest messageRequest = new MessageRequest((AppLayerMessage) SerializationUtils.deserialize(message), callback, callback.asBinder());
            if (pipeline != null && status == Status.CONNECTED) pipeline.requestMessage(messageRequest);
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
        }

        @Override
        public void disconnect(IBinder binder) throws RemoteException {
            if (connectedClients.containsKey(binder)) {
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
            getDataStorage().set("PASSWORD", password);
            sugar.free.sightparser.applayer.Service.REMOTE_CONTROL.setServicePassword(password);
        }

        @Override
        public void reset() throws RemoteException {
            SightService.this.disconnect(false);
            getDataStorage().remove("INCOMINGKEY");
            getDataStorage().remove("OUTGOINGKEY");
            getDataStorage().remove("COMMID");
            getDataStorage().remove("LASTNONCESENT");
            getDataStorage().remove("LASTNONCERECEIVED");
            getDataStorage().remove("DEVICEMAC");
        }
    };
}
