package sugar.free.sightparser.error;

import lombok.Getter;

public class InvalidAppCRCError extends SightError {

    private static final long serialVersionUID = 1L;

    @Getter
    private int received;
    @Getter
    private int expected;

    public InvalidAppCRCError(int received, int expected) {
        this.received = received;
        this.expected = expected;
    }

    @Override
    public String getMessage() {
        return "Received: " + received + " Expected: " + expected;
    }
}
