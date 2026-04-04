package utils.helpers.validation;

public class BlankOrNullValueValidatorException extends ValidatorException {

    // ─── Constructors ─── //

    public BlankOrNullValueValidatorException() {
        super();
    }

    public BlankOrNullValueValidatorException(String message) {
        super(message);
    }

    public BlankOrNullValueValidatorException(Throwable cause) {
        super(cause);
    }

    public BlankOrNullValueValidatorException(String message, Throwable cause) {
        super(message, cause);
    }

}
