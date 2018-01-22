package sugar.free.sightparser.handling;

import android.os.IBinder;
import android.os.RemoteException;

import sugar.free.sightparser.SerializationUtils;
import sugar.free.sightparser.applayer.AppLayerMessage;

public abstract class MessageCallback implements IMessageCallback {

    @Override
    public void onMessage(byte[] message) throws RemoteException {
        onMessage((AppLayerMessage) SerializationUtils.deserialize(message));
    }

    @Override
    public void onError(byte[] error) throws RemoteException {
        onError((Exception) SerializationUtils.deserialize(error));
    }

    public abstract void onMessage(AppLayerMessage message);

    public void onError(Exception error) {
        error.printStackTrace();
    }

    @Override
    public IBinder asBinder() {
        return null;
    }
}
