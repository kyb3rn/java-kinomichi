package app.utils.tarification;

public class TarificationException extends Exception {

    // ─── Constructors ─── //

    public TarificationException() {
        super();
    }

    public TarificationException(String message) {
        super(message);
    }

    public TarificationException(Throwable cause) {
        super(cause);
    }

    public TarificationException(String message, Throwable cause) {
        super(message, cause);
    }

}
