package sugar.free.sightparser.handling;

import sugar.free.sightparser.handling.IMessageCallback;
import sugar.free.sightparser.handling.IStatusCallback;

interface ISightService {

    String getRemoteVersion();
    void pair(String mac, IBinder binder);
    boolean isUseable();
    String getStatus();
    void requestMessage(in byte[] getClass, IMessageCallback callback);
    long registerStatusCallback(IStatusCallback callback);
    void unregisterStatusCallback(long id);
    void connect(IBinder binder);
    void disconnect(IBinder binder);
    void setPassword(String password);
    void setAuthorized(String packageName, boolean allowed);
    void reset();
    void aclDisconnect(String mac);
}
