package sugar.free.sightparser.pipeline.handlers;

import android.util.Log;

import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;

import sugar.free.sightparser.applayer.messages.AppLayerMessage;
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
        Log.d("SightService", "RECEIVE: " + appLayerMessage.getClass());
        pipeline.receive(appLayerMessage);
        Answers.getInstance().logCustom(new CustomEvent("Received Application Layer Message")
                .putCustomAttribute("Message", appLayerMessage.getClass().getName()));
    }

    @Override
    public void onOutboundMessage(Object message, Pipeline pipeline) throws Exception {
        if (!(message instanceof AppLayerMessage)) return;
        Log.d("SightService", "SEND: " + message.getClass());
        DataMessage dataMessage = new DataMessage();
        dataMessage.setData(((AppLayerMessage) message).serialize());
        pipeline.send(dataMessage);
        Answers.getInstance().logCustom(new CustomEvent("Sent Application Layer Message")
            .putCustomAttribute("Message", message.getClass().getName()));
    }

}
