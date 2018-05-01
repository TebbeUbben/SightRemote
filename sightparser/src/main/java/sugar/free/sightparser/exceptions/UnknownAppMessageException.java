package sugar.free.sightparser.exceptions;

import lombok.Getter;

public class UnknownAppMessageException extends Exception {

    private static final long serialVersionUID = 1L;

    @Getter
    private byte service;
    @Getter
    private short command;

    public UnknownAppMessageException(byte service, short command) {
        this.service = service;
        this.command = command;
    }

    @Override
    public String getMessage() {
        return "Service: " + service + " Command: " + command;
    }
}
