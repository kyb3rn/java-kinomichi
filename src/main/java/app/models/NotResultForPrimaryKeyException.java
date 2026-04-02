package app.models;

public class NotResultForPrimaryKeyException extends ModelException {

    // ─── Constructors ─── //

    public NotResultForPrimaryKeyException() {
        super();
    }

    public NotResultForPrimaryKeyException(String message) {
        super(message);
    }

    public NotResultForPrimaryKeyException(Throwable cause) {
        super(cause);
    }

    public NotResultForPrimaryKeyException(String message, Throwable cause) {
        super(message, cause);
    }

}
