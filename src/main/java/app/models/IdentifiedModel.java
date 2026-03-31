package app.models;

import app.models.formatting.ModelPrimaryKeyTextFormattingPreset;
import utils.io.helpers.tables.TableDisplay;
import utils.io.helpers.tables.TableDisplayFormattingOptions;
import utils.io.helpers.texts.formatting.TextAlignement;

public abstract class IdentifiedModel extends Model {

    // ─── Properties ─── //

    private int id = -1;

    // ─── Getters ─── //

    @TableDisplay(name = "#", format = @TableDisplayFormattingOptions(preset = ModelPrimaryKeyTextFormattingPreset.class, alignment = TextAlignement.RIGHT), order = 1)
    public int getId() {
        return this.id;
    }

    // ─── Setters ─── //

    public void setId(int id) throws ModelException {
        if (id <= 0 && id != -1) {
            throw new ModelException("L'identifiant doit être un entier strictement positif (ou -1)");
        }

        this.id = id;
    }

}
