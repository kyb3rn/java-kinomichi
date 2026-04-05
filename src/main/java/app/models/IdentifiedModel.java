package app.models;

import utils.helpers.validation.BelowBoundaryValidatorException;
import utils.helpers.validation.BlankOrNullValueValidatorException;
import utils.helpers.validation.ParsingValidatorException;
import utils.helpers.validation.Validators;

public abstract class IdentifiedModel extends Model {

    // ─── Properties ─── //

    private int id = -1;

    // ─── Getters ─── //

    public int getId() {
        return this.id;
    }

    // ─── Setters ─── //

    public void setId(int id) throws ModelException {
        this.id = verifyId(id);
    }

    // ─── Utility methods ─── //

    public static int verifyId(String id) throws ModelException {
        try {
            return Validators.validateStrictlyPositiveInt(id);
        } catch (BlankOrNullValueValidatorException e) {
            throw new ModelVerificationException("L'identifiant de ce modèle ne peut pas être vide ou nul");
        } catch (ParsingValidatorException e) {
            throw new ModelVerificationException("L'identifiant de ce modèle doit être un entier strictement positif (ou -1)");
        } catch (BelowBoundaryValidatorException e) {
            Object valueWhenCrated = e.getValueWhenCrated();
            if (valueWhenCrated instanceof Integer parsedId) {
                if (parsedId == -1) {
                    return parsedId;
                }
            }

            throw new ModelVerificationException("L'identifiant de ce modèle doit être un entier strictement positif (ou -1)");
        }
    }

    public static int verifyId(int id) throws ModelException {
        return verifyId(String.valueOf(id));
    }

}
