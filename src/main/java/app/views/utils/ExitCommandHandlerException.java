package app.views.utils;

public class ExitCommandHandlerException extends FormMenuException {

    // ─── Constructors ─── //

    public ExitCommandHandlerException() {
        super();
    }

    public ExitCommandHandlerException(String message) {
        super(message);
    }

    public ExitCommandHandlerException(Throwable cause) {
        super(cause);
    }

    public ExitCommandHandlerException(String message, Throwable cause) {
        super(message, cause);
    }

}
