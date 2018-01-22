package sugar.free.sightparser.pipeline;

public interface InboundHandler extends Handler {

    void onInboundMessage(Object message, Pipeline pipeline) throws Exception;

}
