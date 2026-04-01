package utils.io.menus;

public class UnloadableMenuException extends MenuException {

    public UnloadableMenuException() {
    }

    public UnloadableMenuException(String message) {
        super(message);
    }

    public UnloadableMenuException(Throwable cause) {
        super(cause);
    }

    public UnloadableMenuException(String message, Throwable cause) {
        super(message, cause);
    }

}
