package app.views.utils;

public class GoBackException extends ExitCommandHandlerException {

    // ─── Constructors ─── //

    public GoBackException() {
        super();
    }

    public GoBackException(String message) {
        super(message);
    }

    public GoBackException(Throwable cause) {
        super(cause);
    }

    public GoBackException(String message, Throwable cause) {
        super(message, cause);
    }

}