package app.models.managers;

public class DeletingReferencedDataManagerDataException extends DataManagerException {

    // ─── Constructors ─── //

    public DeletingReferencedDataManagerDataException() {
        super();
    }

    public DeletingReferencedDataManagerDataException(String message) {
        super(message);
    }

    public DeletingReferencedDataManagerDataException(Throwable cause) {
        super(cause);
    }

    public DeletingReferencedDataManagerDataException(String message, Throwable cause) {
        super(message, cause);
    }

}
