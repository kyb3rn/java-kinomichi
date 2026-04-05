package app.models;

import app.models.managers.DataManagerException;

public class NoResultForPrimaryKeyException extends DataManagerException {

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
