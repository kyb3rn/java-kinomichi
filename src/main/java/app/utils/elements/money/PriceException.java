package app.utils.elements.money;

public class PriceException extends MoneyAmountException {

    // ─── Constructors ─── //

    public PriceException() {
        super();
    }

    public PriceException(String message) {
        super(message);
    }

    public PriceException(Throwable cause) {
        super(cause);
    }

    public PriceException(String message, Throwable cause) {
        super(message, cause);
    }

}
