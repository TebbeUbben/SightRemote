package sugar.free.sightparser.handling;

interface IStatusCallback {
    void onStatusChange(String status, long statusTime, long waitTime);
}
