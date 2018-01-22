package sugar.free.sightparser.error;

import lombok.Getter;

public class InvalidAppCRCError extends SightError {

    @Getter
    private short received;
    @Getter
    private short expected;

    public InvalidAppCRCError(short received, short expected) {
        this.received = received;
        this.expected = expected;
    }

    @Override
    public String getMessage() {
        return "Received: " + received + " Expected: " + expected;
    }
}
