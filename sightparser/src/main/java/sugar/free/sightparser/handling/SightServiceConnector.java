package sugar.free.sightparser.handling;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.IBinder;
import android.os.RemoteException;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import lombok.Getter;
import sugar.free.sightparser.SerializationUtils;
import sugar.free.sightparser.applayer.messages.AppLayerMessage;
import sugar.free.sightparser.pipeline.Status;

public class SightServiceConnector {

    private long statusCallbackId = -1;
    private Context context;
    private ServiceConnectionCallback connectionCallback;
    private Binder localBinder = new Binder();
    private List<StatusCallback> statusCallbacks = new ArrayList<>();
    private ISightService boundService;
    @Getter
    private boolean connectedToService;
    @Getter
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
                    public void onStatusChange(String status, long statusTime, long waitTime) throws RemoteException {
                        if (!connectedToService) return;
                        Status enumStatus = Status.valueOf(status);
                        synchronized (statusCallbacks) {
                            for (StatusCallback statusCallback : new ArrayList<>(statusCallbacks)) {
                                statusCallback.onStatusChange(enumStatus, statusTime, waitTime);
                            }
                        }
                    }
                });
            } catch (RemoteException e) {
            } catch (NullPointerException e) {
                android.util.Log.e("SightServiceConnector", "Null pointer exception, probably incompatible application interface - upgrade them to compantible versions", e);
            }
            if (connectLatch != null) connectLatch.countDown();
            if (connectionCallback != null) connectionCallback.onServiceConnected();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            boundService = null;
            connectingToService = false;
            connectedToService = false;
            statusCallbackId = -1;
            if (connectionCallback != null) connectionCallback.onServiceDisconnected();
        }
    };

    public String getRemoteVersion() {
        try {
            if (boundService != null) return boundService.getRemoteVersion();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return "ERROR";
    }

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
            if (boundService != null) boundService.connect(localBinder);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void disconnect() {
        try {
            if (boundService != null) boundService.disconnect(localBinder);
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
            boundService = null;
            if (connectionCallback != null) connectionCallback.onServiceDisconnected();
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
            if (boundService != null) boundService.pair(mac, localBinder);
        } catch (RemoteException e) {
        }
    }

    public boolean isUseable() {
        if (boundService == null) return false;
        try {
            return boundService.isUseable();
        } catch (RemoteException e) {
        }
        return false;
    }

    public Status getStatus() {
        if (boundService == null) return null;
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

    public void setPassword(String password) {
        try {
            boundService.setPassword(password);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void setAuthorized(String packageName, boolean allowed) {
        try {
            boundService.setAuthorized(packageName, allowed);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void reset() {
        try {
            boundService.reset();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void aclDisconnect(String mac) {
        try {
            boundService.aclDisconnect(mac);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public long getStatusTime() {
        try {
            return boundService.getStatusTime();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public long getWaitTime() {
        try {
            return boundService.getWaitTime();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public void forceConnect() {
        try {
            boundService.forceConnect();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}
