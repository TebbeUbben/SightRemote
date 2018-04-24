package sugar.free.sightparser.handling;

import sugar.free.sightparser.pipeline.Status;

public interface StatusCallback {

    void onStatusChange(Status status, long statusTime, long waitTime);
}
