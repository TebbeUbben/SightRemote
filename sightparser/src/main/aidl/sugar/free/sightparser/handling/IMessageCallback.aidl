package sugar.free.sightparser.handling;

interface IMessageCallback {
    void onMessage(in byte[] getClass);
    void onError(in byte[] error);
}
