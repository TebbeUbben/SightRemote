package sugar.free.sightparser.error;

import lombok.Getter;

public class InvalidAuthVersionError extends SightError {

    @Getter
    private byte received;
    @Getter
    private byte expected;

    public InvalidAuthVersionError(byte received, byte expected) {
        this.received = received;
        this.expected = expected;
    }

    @Override
    public String getMessage() {
        return "Received: " + received + " Expected: " + expected;
    }
}
