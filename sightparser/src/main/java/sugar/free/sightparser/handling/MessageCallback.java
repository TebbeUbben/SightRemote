package sugar.free.sightparser.handling;

import sugar.free.sightparser.applayer.messages.AppLayerMessage;

public interface MessageCallback {

    void onMessage(AppLayerMessage message);

    void onError(Exception error);
}
