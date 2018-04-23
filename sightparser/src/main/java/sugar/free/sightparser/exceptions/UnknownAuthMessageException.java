package sugar.free.sightparser.exceptions;

import lombok.Getter;

public class UnknownAuthMessageException extends Exception {

    private static final long serialVersionUID = 1L;

    @Getter
    private byte command;

    public UnknownAuthMessageException(byte command) {
        this.command = command;
    }

    @Override
    public String getMessage() {
        return "Command: " + command;
    }
}
