package sugar.free.sightparser.error;

import lombok.Getter;

public class InvalidAppVersionError extends SightError {

    @Getter
    private byte received;
    @Getter
    private byte expected;

    public InvalidAppVersionError(byte received, byte expected) {
        this.received = received;
        this.expected = expected;
    }

    @Override
    public String getMessage() {
        return "Received: " + received + " Expected: " + expected;
    }
}
