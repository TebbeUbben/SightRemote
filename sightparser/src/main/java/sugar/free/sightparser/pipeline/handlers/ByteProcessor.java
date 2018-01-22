package sugar.free.sightparser.pipeline.handlers;

import sugar.free.sightparser.pipeline.ByteBuf;
import sugar.free.sightparser.pipeline.DuplexHandler;
import sugar.free.sightparser.pipeline.Pipeline;

public class ByteProcessor implements DuplexHandler {

    private ByteBuf byteBuf;

    public ByteProcessor(ByteBuf byteBuf) {
        this.byteBuf = byteBuf;
    }

    @Override
    public void onInboundMessage(Object message, Pipeline pipeline) throws Exception {
        if (!(message instanceof byte[])) return;
        byteBuf.putBytes((byte[]) message);
        pipeline.receive(byteBuf);
    }

    @Override
    public void onOutboundMessage(Object message, Pipeline pipeline) throws Exception {
        if (!(message instanceof ByteBuf)) return;
        ByteBuf data = (ByteBuf) message;
        while (data.size() > 0) {
            pipeline.getOutputStream().write(data.readBytes(data.size() >= 110 ? 110 : data.size()));
            pipeline.getOutputStream().flush();
        }
    }

}
