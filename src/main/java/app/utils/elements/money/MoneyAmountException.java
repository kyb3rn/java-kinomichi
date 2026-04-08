package app.utils.elements.money;

public class MoneyAmountException extends Exception {

    // ─── Constructors ─── //

    public MoneyAmountException() {
        super();
    }

    public MoneyAmountException(String message) {
        super(message);
    }

    public MoneyAmountException(Throwable cause) {
        super(cause);
    }

    public MoneyAmountException(String message, Throwable cause) {
        super(message, cause);
    }

}
