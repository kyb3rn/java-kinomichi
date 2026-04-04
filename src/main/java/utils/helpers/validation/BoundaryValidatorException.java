package utils.helpers.validation;

public abstract class BoundaryValidatorException extends ValidatorException {

    // ─── Constructors ─── //

    public BoundaryValidatorException() {
        super();
    }

    public BoundaryValidatorException(String message) {
        super(message);
    }

    public BoundaryValidatorException(Throwable cause) {
        super(cause);
    }

    public BoundaryValidatorException(String message, Throwable cause) {
        super(message, cause);
    }

}
