package utils.helpers.validation;

public class StrictlyEmptyOrNullValueValidatorException extends ValidatorException {

    // ─── Constructors ─── //

    public StrictlyEmptyOrNullValueValidatorException() {
        super();
    }

    public StrictlyEmptyOrNullValueValidatorException(String message) {
        super(message);
    }

    public StrictlyEmptyOrNullValueValidatorException(Throwable cause) {
        super(cause);
    }

    public StrictlyEmptyOrNullValueValidatorException(String message, Throwable cause) {
        super(message, cause);
    }

}
