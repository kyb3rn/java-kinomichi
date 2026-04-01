package app.utils.menus;

public class InvalidInputFormMenuException extends FormMenuException {

    // ─── Constructors ─── //

    public InvalidInputFormMenuException() {
        super();
    }

    public InvalidInputFormMenuException(String message) {
        super(message);
    }

    public InvalidInputFormMenuException(Throwable cause) {
        super(cause);
    }

    public InvalidInputFormMenuException(String message, Throwable cause) {
        super(message, cause);
    }

}
