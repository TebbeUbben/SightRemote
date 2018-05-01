package sugar.free.sightparser.exceptions;

import org.spongycastle.util.encoders.Hex;

import lombok.Getter;

public class InvalidNonceException extends Exception {

    private static final long serialVersionUID = 1L;

    @Getter
    private byte[] received;
    @Getter
    private byte[] expected;

    public InvalidNonceException(byte[] received, byte[] expected) {
        this.received = received;
        this.expected = expected;
    }

    @Override
    public String getMessage() {
        return "Received: " + Hex.toHexString(received) + " Expected: " + Hex.toHexString(expected);
    }
}
