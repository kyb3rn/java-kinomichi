package app.utils.menus;

public class GoBackException extends ExitInputPromptException {

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