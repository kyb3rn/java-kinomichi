package utils.io.menus;

public class UnhandledMenuResponseType extends MenuException{

    public UnhandledMenuResponseType() {
    }

    public UnhandledMenuResponseType(String message) {
        super(message);
    }

    public UnhandledMenuResponseType(Throwable cause) {
        super(cause);
    }

    public UnhandledMenuResponseType(String message, Throwable cause) {
        super(message, cause);
    }


}
