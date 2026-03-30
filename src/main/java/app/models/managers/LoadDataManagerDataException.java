package app.models.managers;

public class LoadDataManagerDataException extends DataManagerException {

    // ─── Constructors ─── //

    public LoadDataManagerDataException(String message) {
        super(message);
    }

    public LoadDataManagerDataException(Throwable cause) {
        super(cause);
    }

    public LoadDataManagerDataException(String message, Throwable cause) {
        super(message, cause);
    }

}
