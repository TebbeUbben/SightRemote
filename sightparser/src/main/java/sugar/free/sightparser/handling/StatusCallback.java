package sugar.free.sightparser.handling;

import sugar.free.sightparser.pipeline.Status;

public interface StatusCallback {

    void onStatusChange(Status status);
}
