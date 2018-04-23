package sugar.free.sightparser.errors;

public class NotAuthorizedError extends Exception {

    public NotAuthorizedError() {
        super();
    }

    public NotAuthorizedError(String message) {
        super(message);
    }
}
