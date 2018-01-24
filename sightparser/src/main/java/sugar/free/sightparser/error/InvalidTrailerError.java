package sugar.free.sightparser.error;

import org.spongycastle.util.encoders.Hex;

import lombok.Getter;

public class InvalidTrailerError extends SightError {

    private static final long serialVersionUID = 1L;

    @Getter
    private byte[] received;
    @Getter
    private byte[] calculated;

    public InvalidTrailerError(byte[] received, byte[] calculated) {
        this.received = received;
        this.calculated = calculated;
    }

    @Override
    public String getMessage() {
        return "Received: " + Hex.toHexString(received) + " Expected: " + Hex.toHexString(calculated);
    }
}
