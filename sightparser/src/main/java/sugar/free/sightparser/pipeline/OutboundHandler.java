package sugar.free.sightparser.pipeline;

public interface OutboundHandler extends Handler {

    void onOutboundMessage(Object message, Pipeline pipeline) throws Exception;

}
