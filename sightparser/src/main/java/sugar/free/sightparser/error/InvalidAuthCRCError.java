package sugar.free.sightparser.error;

import lombok.Getter;

public class InvalidAuthCRCError extends SightError {

    private static final long serialVersionUID = 1L;

    @Getter
    private int received;
    @Getter
    private int expected;

    public InvalidAuthCRCError(int received, int expected) {
        this.received = received;
        this.expected = expected;
    }

    @Override
    public String getMessage() {
        return "Received: " + received + " Expected: " + expected;
    }
}
