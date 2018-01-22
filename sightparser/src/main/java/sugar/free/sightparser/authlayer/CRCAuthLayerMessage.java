package sugar.free.sightparser.authlayer;

import java.math.BigInteger;

import sugar.free.sightparser.crypto.Cryptograph;
import sugar.free.sightparser.pipeline.ByteBuf;

public abstract class CRCAuthLayerMessage extends AuthLayerMessage {

    @Override
    public ByteBuf serialize(BigInteger nonce, int commID, byte[] key) {
        byte[] data = getData();
        short dataLength = (short) (data.length + 2);
        short length = (short) (29 + dataLength);
        ByteBuf byteBuf = new ByteBuf(length + 8);
        byteBuf.putBytes(MAGIC_HEADER);
        byteBuf.putShortLE(length);
        byteBuf.putShortLE((short) ~length);
        byteBuf.putByte(VERSION);
        byteBuf.putByte(getCommand());
        byteBuf.putShortLE(dataLength);
        byteBuf.putIntLE(commID);
        byteBuf.putBytes(processNonce(nonce));
        byteBuf.putBytes(data);
        byteBuf.putShortLE((short) Cryptograph.calculateCRC(byteBuf.getBytes(8, length - 10)));
        byteBuf.putBytes((byte) 0x00, 8);
        return byteBuf;
    }
}
