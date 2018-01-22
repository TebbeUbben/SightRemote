package sugar.free.sightparser.handling;

import android.os.IBinder;
import android.os.RemoteException;

import sugar.free.sightparser.pipeline.Status;

public abstract class StatusCallback implements IStatusCallback {

    @Override
    public void onStatusChange(String status) throws RemoteException {
        onStatusChange(Status.valueOf(status));
    }

    public abstract void onStatusChange(Status status);

    @Override
    public IBinder asBinder() {
        return null;
    }
}
