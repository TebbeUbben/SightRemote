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
import sugar.free.sightparser.error.SightError;
import sugar.free.sightparser.pipeline.Pipeline;
import sugar.free.sightparser.pipeline.Status;

public class SightService extends Service {

    private static final int DISCONNECT_DELAY = 5000;

    private String tempMac;
    private int clientsConnected = 0;
    private ConnectionThread connectionThread;
    private Pipeline pipeline;
    private DataStorage dataStorage;
    private Map<Long, IStatusCallback> statusCallbacks = new HashMap<>();
    private int statusCallbackID = 0;
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
                            }));
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
            for (IStatusCallback sc : statusCallbacks.values())
                try {
                    sc.onStatusChange(status.name());
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
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
                if (!pairing) timeoutTimer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        Log.d("SightService", "TIMEOUT");
                        disconnect(true);
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
        public void pair(String mac, boolean connected) throws RemoteException {
            SightService.this.disconnect(false);
            getDataStorage().clear();
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
            MessageRequest messageRequest = new MessageRequest((AppLayerMessage) SerializationUtils.deserialize(message), callback);
            if (pipeline != null && status == Status.CONNECTED) pipeline.requestMessage(messageRequest);
        }

        @Override
        public long registerStatusCallback(IStatusCallback callback) throws RemoteException {
            callback.onStatusChange("DISCONNECTED");
            long id = statusCallbackID++;
            statusCallbacks.put(id, callback);
            return id;
        }

        @Override
        public void unregisterStatusCallback(long id) throws RemoteException {
            statusCallbacks.remove(id);
        }

        @Override
        public void connect() throws RemoteException {
            if (disconnectTimer != null) disconnectTimer.cancel();
            disconnectTimer = new Timer();
            clientsConnected++;
            if (getDataStorage().contains("DEVICEMAC"))
                SightService.this.connect(getDataStorage().get("DEVICEMAC"), false);
        }

        @Override
        public void disconnect() throws RemoteException {
            if (--clientsConnected == 0 && connectionThread != null) {
                disconnectTimer = new Timer();
                disconnectTimer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        SightService.this.disconnect(false);
                    }
                }, DISCONNECT_DELAY);
            }
        }
    };
}
