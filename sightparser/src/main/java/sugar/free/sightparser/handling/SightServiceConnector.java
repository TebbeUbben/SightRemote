package sugar.free.sightparser.handling;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import sugar.free.sightparser.SerializationUtils;
import sugar.free.sightparser.applayer.AppLayerMessage;
import sugar.free.sightparser.pipeline.Status;

public class SightServiceConnector {

    private Context context;
    private ServiceConnectionCallback connectionCallback;
    private IBinder binder;
    private List<StatusCallback> statusCallbacks = new ArrayList<>();
    private ISightService boundService;
    private long statusCallbackID;
    private boolean connectedToService;
    private CountDownLatch connectLatch;
    private boolean connected;

    public SightServiceConnector(Context context) {
        this.context = context;
    }

    public void setConnectionCallback(ServiceConnectionCallback connectionCallback) {
        this.connectionCallback = connectionCallback;
    }

    private StatusCallback statusRedirector = new StatusCallback() {
        @Override
        public void onStatusChange(Status status) {
            synchronized (statusCallbacks) {
                for (StatusCallback statusCallback : new ArrayList<StatusCallback>(statusCallbacks)) {
                    statusCallback.onStatusChange(status);
                }
            }
        }

        @Override
        public IBinder asBinder() {
            return binder;
        }
    };

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            binder = service;
            boundService = ISightService.Stub.asInterface(service);
            try {
                statusCallbackID = boundService.registerStatusCallback(statusRedirector);
            } catch (RemoteException e) {
            }
            if (connectLatch != null) connectLatch.countDown();
            if (connectionCallback != null) connectionCallback.onServiceConnected();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            boundService = null;
            if (connectionCallback != null) connectionCallback.onServiceDisconnected();
        }
    };

    public void connectToService() {
        if (!connectedToService) context.bindService(new Intent(context, SightService.class), serviceConnection, Context.BIND_AUTO_CREATE);
        connectedToService = true;
    }

    public void connectToServiceBlockingCall() {
        if (!connectedToService) {
            connectLatch = new CountDownLatch(1);
            connectToService();
            try {
                connectLatch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            connectLatch = null;
        }
    }

    public void connect() {
        try {
            if (!connected) {
                connected = true;
                boundService.connect();
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void disconnect() {
        try {
            if (connected) {
                connected = false;
                boundService.disconnect();
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void disconnectFromService() {
        if (connectedToService) {
            try {
                if (boundService != null) {
                    boundService.unregisterStatusCallback(statusCallbackID);
                    disconnect();
                }
            } catch (RemoteException e) {
            }
            context.unbindService(serviceConnection);
        }
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
            boundService.pair(mac, connected);
            connected = true;
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

    public void requestMessage(AppLayerMessage message, MessageCallback callback) {
        try {
            boundService.requestMessage(SerializationUtils.serialize(message), callback);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public boolean isConnectedToService() {
        return connectedToService;
    }
}
