package sugar.free.sightparser.pipeline.handlers;

import sugar.free.sightparser.applayer.connection.ConnectMessage;
import sugar.free.sightparser.authlayer.SynAckResponse;
import sugar.free.sightparser.pipeline.InboundHandler;
import sugar.free.sightparser.pipeline.Pipeline;
import sugar.free.sightparser.pipeline.Status;


public class ConnectionEstablisher implements InboundHandler {
    @Override
    public void onInboundMessage(Object message, Pipeline pipeline) throws Exception {
        if (message instanceof SynAckResponse)
            pipeline.send(new ConnectMessage());
        else if (message instanceof ConnectMessage)
            pipeline.setStatus(Status.CONNECTED);
    }
}
