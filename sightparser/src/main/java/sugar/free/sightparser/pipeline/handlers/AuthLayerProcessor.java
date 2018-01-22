package sugar.free.sightparser.pipeline.handlers;

import java.math.BigInteger;

import sugar.free.sightparser.authlayer.AuthLayerMessage;
import sugar.free.sightparser.authlayer.CRCAuthLayerMessage;
import sugar.free.sightparser.authlayer.KeyRequest;
import sugar.free.sightparser.error.InvalidAuthCRCError;
import sugar.free.sightparser.error.InvalidNonceError;
import sugar.free.sightparser.error.InvalidTrailerError;
import sugar.free.sightparser.pipeline.ByteBuf;
import sugar.free.sightparser.pipeline.DuplexHandler;
import sugar.free.sightparser.pipeline.Pipeline;

public class AuthLayerProcessor implements DuplexHandler {

    @Override
    public void onInboundMessage(Object message, Pipeline pipeline) throws Exception {
        if (!(message instanceof ByteBuf)) return;
        ByteBuf data = (ByteBuf) message;
        while (data.size() >= 37) {
            int length = data.getShortLE(4);
            if (data.size() < length + 8) return;
            try {
                AuthLayerMessage authLayerMessage = AuthLayerMessage.deserialize(data, pipeline.getLastNonceReceived(),
                        pipeline.getDerivedKeys() != null ? pipeline.getDerivedKeys().getIncomingKey() : null);
                pipeline.setLastNonceReceived(authLayerMessage.getNonce());
                pipeline.setCommID(authLayerMessage.getCommID());
                pipeline.receive(authLayerMessage);
            } catch (InvalidNonceError | InvalidAuthCRCError | InvalidTrailerError e) {
                data.shift(data.size());
                throw e;
            }
        }
    }

    @Override
    public void onOutboundMessage(Object message, Pipeline pipeline) throws Exception {
        if (!(message instanceof AuthLayerMessage)) return;
        AuthLayerMessage data = (AuthLayerMessage) message;
        BigInteger nonce = pipeline.getLastNonceSent();
        if (!(data instanceof CRCAuthLayerMessage)) nonce = nonce.add(BigInteger.ONE);
        pipeline.send(data.serialize(nonce, (message instanceof KeyRequest) ? 1 : pipeline.getCommID(), pipeline.getDerivedKeys() != null ? pipeline.getDerivedKeys().getOutgoingKey() : null));
        pipeline.setLastNonceSent(nonce);
    }
}
