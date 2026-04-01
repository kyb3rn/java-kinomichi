package utils.io.menus;

public class MenuException extends RuntimeException {

    public MenuException() {
    }

    public MenuException(String message) {
        super(message);
    }

    public MenuException(Throwable cause) {
        super(cause);
    }

    public MenuException(String message, Throwable cause) {
        super(message, cause);
    }

}
