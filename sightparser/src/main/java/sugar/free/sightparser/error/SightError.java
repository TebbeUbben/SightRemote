package sugar.free.sightparser.error;

public abstract class SightError extends Exception {

    public SightError() {
        super();
    }

    public SightError(String message) {
        super(message);
    }
}
