package app.utils.tarification;

public class ChargeableException extends TarificationException {

    // ─── Constructors ─── //

    public ChargeableException() {
        super();
    }

    public ChargeableException(String message) {
        super(message);
    }

    public ChargeableException(Throwable cause) {
        super(cause);
    }

    public ChargeableException(String message, Throwable cause) {
        super(message, cause);
    }

}
