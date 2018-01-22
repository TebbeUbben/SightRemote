package sugar.free.sightparser.error;

import lombok.Getter;

public class UnknownServiceError extends SightError {

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
