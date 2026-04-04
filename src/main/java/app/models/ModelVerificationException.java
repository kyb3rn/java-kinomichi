package app.models;

public class ModelVerificationException extends ModelException {

    // ─── Constructors ─── //

    public ModelVerificationException() {
        super();
    }

    public ModelVerificationException(String message) {
        super(message);
    }

    public ModelVerificationException(Throwable cause) {
        super(cause);
    }

    public ModelVerificationException(String message, Throwable cause) {
        super(message, cause);
    }

}
