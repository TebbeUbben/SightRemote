package sugar.free.sightparser.handling;

import sugar.free.sightparser.handling.IMessageCallback;
import sugar.free.sightparser.handling.IStatusCallback;

interface ISightService {

    void pair(String mac, boolean connected);
    boolean isUseable();
    String getStatus();
    void requestMessage(in byte[] getClass, IMessageCallback callback);
    long registerStatusCallback(IStatusCallback callback);
    void unregisterStatusCallback(long id);
    void connect();
    void disconnect();

}
