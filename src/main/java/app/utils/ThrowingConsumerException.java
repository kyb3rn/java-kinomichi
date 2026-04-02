package app.utils;

public class ThrowingConsumerException extends Exception {

    // ─── Constructors ─── //

    public ThrowingConsumerException() {
        super();
    }

    public ThrowingConsumerException(String message) {
        super(message);
    }

    public ThrowingConsumerException(Throwable cause) {
        super(cause);
    }

    public ThrowingConsumerException(String message, Throwable cause) {
        super(message, cause);
    }

}
