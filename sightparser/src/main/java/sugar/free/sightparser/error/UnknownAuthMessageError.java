package sugar.free.sightparser.error;

import lombok.Getter;

public class UnknownAuthMessageError extends SightError {

    private static final long serialVersionUID = 1L;

    @Getter
    private byte command;

    public UnknownAuthMessageError(byte command) {
        this.command = command;
    }

    @Override
    public String getMessage() {
        return "Command: " + command;
    }
}
