package app.models;

public class NoResultForPrimaryKeyException extends ModelException {

    // ─── Constructors ─── //

    public NoResultForPrimaryKeyException() {
        super();
    }

    public NoResultForPrimaryKeyException(String message) {
        super(message);
    }

    public NoResultForPrimaryKeyException(Throwable cause) {
        super(cause);
    }

    public NoResultForPrimaryKeyException(String message, Throwable cause) {
        super(message, cause);
    }

}
