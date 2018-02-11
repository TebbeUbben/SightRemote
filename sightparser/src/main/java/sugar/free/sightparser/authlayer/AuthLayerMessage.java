package sugar.free.sightparser.authlayer;

import android.annotation.SuppressLint;
import android.util.Log;

import org.spongycastle.util.encoders.Hex;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import lombok.Getter;
import sugar.free.sightparser.Message;
import sugar.free.sightparser.crypto.Cryptograph;
import sugar.free.sightparser.error.InvalidAuthCRCError;
import sugar.free.sightparser.error.InvalidAuthVersionError;
import sugar.free.sightparser.error.InvalidNonceError;
import sugar.free.sightparser.error.InvalidTrailerError;
import sugar.free.sightparser.error.SightError;
import sugar.free.sightparser.error.UnknownAuthMessageError;
import sugar.free.sightparser.pipeline.ByteBuf;

public abstract class AuthLayerMessage extends Message {

    static final byte[] MAGIC_HEADER = Hex.decode("88CCEEFF");
    static final byte VERSION = 0x20;

    @SuppressLint("UseSparseArrays")
    private static final Map<Byte, Class<? extends AuthLayerMessage>> MESSAGES = new HashMap<>();

    static {
        MESSAGES.put((byte) 0x09, ConnectionRequest.class);
        MESSAGES.put((byte) 0x0A, ConnectionResponse.class);
        MESSAGES.put((byte) 0x0C, KeyRequest.class);
        MESSAGES.put((byte) 0x11, KeyResponse.class);
        MESSAGES.put((byte) 0x12, VerifyDisplayRequest.class);
        MESSAGES.put((byte) 0x14, VerifyDisplayResponse.class);
        MESSAGES.put((byte) 0x0E, VerifyConfirmRequest.class);
        MESSAGES.put((byte) 0x1E, VerifyConfirmResponse.class);
        MESSAGES.put((byte) 0x17, SynRequest.class);
        MESSAGES.put((byte) 0x18, SynAckResponse.class);
        MESSAGES.put((byte) 0x06, ErrorMessage.class);
        MESSAGES.put((byte) 0x03, DataMessage.class);
    }

    @Getter
    private BigInteger nonce;
    @Getter
    private long commID;

    protected abstract byte getCommand();

    protected byte[] getData() {
        return new byte[0];
    }

    public ByteBuf serialize(BigInteger nonce, long commID, byte[] key) {
        byte[] data = getData();
        byte[] nonceBytes = processNonce(nonce);
        byte[] dataEncrypted = Cryptograph.encryptDataCTR(data, key, nonceBytes);
        int dataLength = dataEncrypted.length;
        int length = (short) (29 + dataLength);
        ByteBuf byteBuf = new ByteBuf(length + 8);
        byteBuf.putBytes(MAGIC_HEADER);
        byteBuf.putUInt16LE(length);
        byteBuf.putUInt16LE(~length);
        byteBuf.putByte(VERSION);
        byteBuf.putByte(getCommand());
        byteBuf.putUInt16LE(dataLength);
        byteBuf.putUInt32LE(commID);
        byteBuf.putBytes(nonceBytes);
        byteBuf.putBytes(dataEncrypted);
        byteBuf.putBytes(Cryptograph.produceCCMTag(byteBuf.getBytes(16, 13), data, byteBuf.getBytes(8, 21), key));
        return byteBuf;
    }

    public static AuthLayerMessage deserialize(ByteBuf data, BigInteger lastNonce, byte[] key) throws IllegalAccessException, InstantiationException, SightError {
        data.shift(4); //Preamble
        int packetLength = data.readUInt16LE();
        data.shift(2); //Packet length XOR
        byte[] crcContent = data.getBytes(packetLength - 10);
        byte[] header = data.getBytes(21);
        byte version = data.readByte();
        byte command = data.readByte();
        Class clazz = MESSAGES.get(command);
        if (clazz == null) throw new UnknownAuthMessageError(command);
        int dataLength = data.readUInt16LE();
        long commID = data.readUInt32LE();
        byte[] nonceTrailer = data.getBytes(13);
        byte[] nonce = data.readBytesLE(13);
        byte[] payload = data.readBytes(dataLength);
        byte[] trailer = data.readBytes(8);
        boolean crcPacket = CRCAuthLayerMessage.class.isAssignableFrom(clazz);
        BigInteger nonceInt = new BigInteger(nonce);
        if (version  != VERSION) {
            throw new InvalidAuthVersionError(version, VERSION);
        } else if (lastNonce != null && lastNonce.equals(BigInteger.ZERO) && nonceInt.compareTo(lastNonce) != 1) {
            throw new InvalidNonceError(nonce, processNonce(lastNonce.add(BigInteger.ONE)));
        } else {
            if (crcPacket) {
                byte[] crcBytes = new byte[2];
                byte[] rawData = new byte[dataLength - 2];
                System.arraycopy(payload, dataLength - 2, crcBytes, 0, 2);
                System.arraycopy(payload, 0, rawData, 0, dataLength - 2);
                payload = rawData;
                int crc = (crcBytes[0] & 0xFF | (crcBytes[1] & 0xFF)  << 8);
                int calculatedCRC = Cryptograph.calculateCRC(crcContent);
                if (crc != calculatedCRC) throw new InvalidAuthCRCError(crc, calculatedCRC);
            } else {
                payload = Cryptograph.encryptDataCTR(payload, key, nonceTrailer);
                byte[] calculatedTrailer = Cryptograph.produceCCMTag(nonceTrailer, payload, header, key);
                if (!Arrays.equals(trailer, calculatedTrailer)) throw new InvalidTrailerError(trailer, calculatedTrailer);
            }
            AuthLayerMessage message = (AuthLayerMessage) clazz.newInstance();
            message.nonce = nonceInt;
            message.commID = commID;
            ByteBuf byteBuf = new ByteBuf(payload.length);
            byteBuf.putBytes(payload);
            message.parse(byteBuf);
            return message;
        }
    }

    protected void parse(ByteBuf byteBuf) {

    }

    static byte[] processNonce(BigInteger nonce) {
        byte[] bytes = nonce.toByteArray();
        ByteBuf byteBuf = new ByteBuf(13);
        byteBuf.putBytesLE(bytes);
        byteBuf.putBytes((byte) 0x00, 13 - bytes.length);
        return byteBuf.getBytes();
    }
}
