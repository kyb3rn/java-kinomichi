package app.models;

import utils.helpers.validation.BlankOrNullValueValidatorException;
import utils.helpers.validation.Validators;

public abstract class Model {

    // ─── Utility methods ─── //

    /**
     * Required because models can be instanced with empty constructor
     */
    public abstract boolean isValid();

    protected static String verifyNotNullOrEmpty(String value, String errorMessage) throws ModelException {
        try {
            return Validators.validateNotNullOrBlank(value);
        } catch (BlankOrNullValueValidatorException e) {
            throw new ModelException(errorMessage, e);
        }
    }

}
