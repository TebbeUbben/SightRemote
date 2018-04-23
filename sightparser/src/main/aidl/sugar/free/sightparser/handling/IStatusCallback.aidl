package sugar.free.sightparser.handling;

interface IStatusCallback {
    oneway void onStatusChange(in byte[] status);
}
