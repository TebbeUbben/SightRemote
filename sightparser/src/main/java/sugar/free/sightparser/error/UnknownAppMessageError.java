package sugar.free.sightparser.error;

import lombok.Getter;

public class UnknownAppMessageError extends SightError {

    @Getter
    private byte service;
    @Getter
    private short command;

    public UnknownAppMessageError(byte service, short command) {
        this.service = service;
        this.command = command;
    }

    @Override
    public String getMessage() {
        return "Service: " + service + " Command: " + command;
    }
}
