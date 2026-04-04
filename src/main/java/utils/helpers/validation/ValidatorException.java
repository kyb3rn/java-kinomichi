package utils.helpers.validation;

public class ValidatorException extends Exception {

    // ─── Constructors ─── //

    public ValidatorException() {
        super();
    }

    public ValidatorException(String message) {
        super(message);
    }

    public ValidatorException(Throwable cause) {
        super(cause);
    }

    public ValidatorException(String message, Throwable cause) {
        super(message, cause);
    }

}
