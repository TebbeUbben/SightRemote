package sugar.free.sightparser.handling;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import sugar.free.sightparser.SerializationUtils;
import sugar.free.sightparser.applayer.AppLayerMessage;
import sugar.free.sightparser.pipeline.Status;

public class SightServiceConnector {

    private long statusCallbackId = -1;
    private Context context;
    private ServiceConnectionCallback connectionCallback;
    private Binder localBinder = new Binder();
    private List<StatusCallback> statusCallbacks = new ArrayList<>();
    private ISightService boundService;
    private boolean connectedToService;
    private boolean connectingToService;
    private CountDownLatch connectLatch;

    public SightServiceConnector(Context context) {
        this.context = context;
    }

    public void setConnectionCallback(ServiceConnectionCallback connectionCallback) {
        this.connectionCallback = connectionCallback;
    }

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            connectedToService = true;
            boundService = ISightService.Stub.asInterface(service);
            try {
                statusCallbackId = boundService.registerStatusCallback(new IStatusCallback.Stub() {
                    @Override
                    public void onStatusChange(String status) throws RemoteException {
                        if (!connectedToService) return;
                        Status enumStatus = Status.valueOf(status);
                        synchronized (statusCallbacks) {
                            for (StatusCallback statusCallback : new ArrayList<>(statusCallbacks)) {
                                statusCallback.onStatusChange(enumStatus);
                            }
                        }
                    }
                });
            } catch (RemoteException e) {
            }
            if (connectLatch != null) connectLatch.countDown();
            if (connectionCallback != null) connectionCallback.onServiceConnected();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            boundService = null;
            connectedToService = false;
            if (connectionCallback != null) connectionCallback.onServiceDisconnected();
        }
    };

    public void connectToService() {
        if (!connectingToService) {
            Intent intent = new Intent();
            intent.setComponent(new ComponentName("sugar.free.sightremote", "sugar.free.sightparser.handling.SightService"));
            context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
        }
        connectingToService = true;
    }

    public void connectToServiceBlockingCall() {
        if (connectedToService) return;
        if (connectLatch == null) connectLatch = new CountDownLatch(1);
        if (!connectingToService) connectToService();
        try {
            connectLatch.await();
            connectLatch = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void connect() {
        try {
            boundService.connect(localBinder);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void disconnect() {
        try {
            boundService.disconnect(localBinder);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void disconnectFromService() {
        if (connectingToService) {
            if (statusCallbackId != -1) try {
                boundService.unregisterStatusCallback(statusCallbackId);
                statusCallbackId = -1;
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            disconnect();
            context.unbindService(serviceConnection);
            disconnectFromService();
        }
        connectingToService = false;
        connectedToService = false;
    }

    public void addStatusCallback(StatusCallback statusCallback) {
        synchronized (statusCallbacks) {
            this.statusCallbacks.add(statusCallback);
        }
    }

    public void removeStatusCallback(StatusCallback statusCallback) {
        synchronized (statusCallbacks) {
            if (statusCallbacks.contains(statusCallback)) this.statusCallbacks.remove(statusCallback);
        }
    }

    public void pair(String mac) {
        try {
            boundService.pair(mac, localBinder);
        } catch (RemoteException e) {
        }
    }

    public boolean isUseable() {
        try {
            return boundService.isUseable();
        } catch (RemoteException e) {
        }
        return false;
    }

    public Status getStatus() {
        try {
            return Status.valueOf(boundService.getStatus());
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void requestMessage(AppLayerMessage message, final MessageCallback callback) {
        try {
            boundService.requestMessage(SerializationUtils.serialize(message), new IMessageCallback.Stub() {
                @Override
                public void onMessage(byte[] bytes) throws RemoteException {
                    if (connectedToService)
                        callback.onMessage((AppLayerMessage) SerializationUtils.deserialize(bytes));
                }

                @Override
                public void onError(byte[] error) throws RemoteException {
                    if (connectedToService)
                        callback.onError((Exception) SerializationUtils.deserialize(error));
                }
            });
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public boolean isConnectedToService() {
        return connectedToService;
    }

    public boolean isConnectingToService() {
        return connectingToService;
    }
}
