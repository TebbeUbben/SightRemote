package sugar.free.sightparser.exceptions;

import lombok.Getter;

public class InvalidAuthCRCException extends Exception {

    private static final long serialVersionUID = 1L;

    @Getter
    private int received;
    @Getter
    private int expected;

    public InvalidAuthCRCException(int received, int expected) {
        this.received = received;
        this.expected = expected;
    }

    @Override
    public String getMessage() {
        return "Received: " + received + " Expected: " + expected;
    }
}
