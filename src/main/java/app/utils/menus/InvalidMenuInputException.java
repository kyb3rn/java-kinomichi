package app.utils.menus;

import utils.io.menus.MenuException;

public class InvalidMenuInputException extends MenuException {

    // ─── Constructors ─── //

    public InvalidMenuInputException() {
        super();
    }

    public InvalidMenuInputException(String message) {
        super(message);
    }

    public InvalidMenuInputException(Throwable cause) {
        super(cause);
    }

    public InvalidMenuInputException(String message, Throwable cause) {
        super(message, cause);
    }

}
