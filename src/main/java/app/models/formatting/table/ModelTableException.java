package app.models.formatting.table;

public class ModelTableException extends Exception {

    // ─── Constructors ─── //

    public ModelTableException() {
        super();
    }

    public ModelTableException(String message) {
        super(message);
    }

    public ModelTableException(Throwable cause) {
        super(cause);
    }

    public ModelTableException(String message, Throwable cause) {
        super(message, cause);
    }

}
