package utils.helpers.validation;

public class BelowBoundaryValidatorException extends BoundaryValidatorException {

    // ─── Properties ─── //

    private Object valueWhenCrated = null;

    // ─── Constructors ─── //

    public BelowBoundaryValidatorException() {
        super();
    }

    public BelowBoundaryValidatorException(String message) {
        super(message);
    }

    public BelowBoundaryValidatorException(Throwable cause) {
        super(cause);
    }

    public BelowBoundaryValidatorException(String message, Throwable cause) {
        super(message, cause);
    }

    public BelowBoundaryValidatorException(Object valueWhenCrated) {
        this.valueWhenCrated = valueWhenCrated;
    }

    // ─── Getters ─── //

    public Object getValueWhenCrated() {
        return this.valueWhenCrated;
    }

}
