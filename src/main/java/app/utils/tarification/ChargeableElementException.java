package app.utils.tarification;

public class ChargeableElementException extends ChargeableException {

    // ─── Constructors ─── //

    public ChargeableElementException() {
        super();
    }

    public ChargeableElementException(String message) {
        super(message);
    }

    public ChargeableElementException(Throwable cause) {
        super(cause);
    }

    public ChargeableElementException(String message, Throwable cause) {
        super(message, cause);
    }

}
