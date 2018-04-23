package sugar.free.sightparser.exceptions;

import lombok.Getter;

public class UnknownServiceException extends Exception {

    private static final long serialVersionUID = 1L;

    @Getter
    private byte serviceID;

    public UnknownServiceException(byte serviceID) {
        this.serviceID = serviceID;
    }

    @Override
    public String getMessage() {
        return "ServiceID: " + serviceID;
    }
}
