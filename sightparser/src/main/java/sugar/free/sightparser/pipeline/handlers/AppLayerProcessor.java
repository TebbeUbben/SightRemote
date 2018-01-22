package sugar.free.sightparser.pipeline.handlers;

import android.util.Log;

import sugar.free.sightparser.applayer.AppLayerMessage;
import sugar.free.sightparser.authlayer.DataMessage;
import sugar.free.sightparser.pipeline.ByteBuf;
import sugar.free.sightparser.pipeline.DuplexHandler;
import sugar.free.sightparser.pipeline.Pipeline;

public class AppLayerProcessor implements DuplexHandler {

    @Override
    public void onInboundMessage(Object message, Pipeline pipeline) throws Exception {
        if (!(message instanceof DataMessage)) return;
        DataMessage dataMessage = (DataMessage) message;
        ByteBuf byteBuf = new ByteBuf(dataMessage.getData().length);
        byteBuf.putBytes(dataMessage.getData());
        AppLayerMessage appLayerMessage = AppLayerMessage.deserialize(byteBuf);
        Log.d("sugar.free.sightremote", "RECEIVE: " + appLayerMessage.getClass());
        pipeline.receive(appLayerMessage);
    }

    @Override
    public void onOutboundMessage(Object message, Pipeline pipeline) throws Exception {
        if (!(message instanceof AppLayerMessage)) return;
        Log.d("sugar.free.sightremote", "SEND: " + message.getClass());
        DataMessage dataMessage = new DataMessage();
        dataMessage.setData(((AppLayerMessage) message).serialize());
        pipeline.send(dataMessage);
    }

}
