package sugar.free.sightparser.error;

import lombok.Getter;

public class UnknownServiceError extends SightError {

    private static final long serialVersionUID = 1L;

    @Getter
    private byte serviceID;

    public UnknownServiceError(byte serviceID) {
        this.serviceID = serviceID;
    }

    @Override
    public String getMessage() {
        return "ServiceID: " + serviceID;
    }
}
