package utils.helpers.validation;

public class AboveBoundaryValidatorException extends BoundaryValidatorException {

    // ─── Constructors ─── //

    public AboveBoundaryValidatorException() {
        super();
    }

    public AboveBoundaryValidatorException(String message) {
        super(message);
    }

    public AboveBoundaryValidatorException(Throwable cause) {
        super(cause);
    }

    public AboveBoundaryValidatorException(String message, Throwable cause) {
        super(message, cause);
    }

}
