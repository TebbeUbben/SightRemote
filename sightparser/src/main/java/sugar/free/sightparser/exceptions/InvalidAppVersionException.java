package sugar.free.sightparser.exceptions;

import lombok.Getter;

public class InvalidAppVersionException extends Exception {

    private static final long serialVersionUID = 1L;

    @Getter
    private byte received;
    @Getter
    private byte expected;

    public InvalidAppVersionException(byte received, byte expected) {
        this.received = received;
        this.expected = expected;
    }

    @Override
    public String getMessage() {
        return "Received: " + received + " Expected: " + expected;
    }
}
