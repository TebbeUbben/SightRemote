package sugar.free.sightparser.handling;

interface IMessageCallback {
    oneway void onMessage(in byte[] getClass);
    oneway void onError(in byte[] error);
}
