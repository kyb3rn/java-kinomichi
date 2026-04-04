package utils.helpers.validation;

public class ParsingValidatorException extends ValidatorException {

    // ─── Constructors ─── //

    public ParsingValidatorException() {
        super();
    }

    public ParsingValidatorException(String message) {
        super(message);
    }

    public ParsingValidatorException(Throwable cause) {
        super(cause);
    }

    public ParsingValidatorException(String message, Throwable cause) {
        super(message, cause);
    }

}
