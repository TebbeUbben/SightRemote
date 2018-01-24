package sugar.free.sightparser.handling;

import android.os.IBinder;
import android.os.RemoteException;

import sugar.free.sightparser.SerializationUtils;
import sugar.free.sightparser.applayer.AppLayerMessage;

public interface MessageCallback {

    void onMessage(AppLayerMessage message);

    void onError(Exception error);
}
