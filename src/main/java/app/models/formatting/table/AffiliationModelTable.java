package app.models.formatting.table;

import app.models.Affiliation;
import app.models.Club;
import app.models.ModelException;
import app.models.formatting.ModelKeyTextFormattingPreset;
import app.models.formatting.ModelPrimaryKeyTextFormattingPreset;
import utils.io.helpers.tables.ModelTableDisplay;
import utils.io.helpers.tables.TableDisplayFormattingOptions;
import utils.io.helpers.texts.formatting.TextAlignment;
import utils.io.helpers.texts.formatting.TextStyle;

public class AffiliationModelTable extends ModelTable<Affiliation> {

    // ─── Constructors ─── //

    public AffiliationModelTable(Affiliation affiliation) throws ModelTableException {
        super(affiliation);
    }

    // ─── Utility methods ─── //

    @ModelTableDisplay(name = "#& (personne)", format = @TableDisplayFormattingOptions(preset = ModelPrimaryKeyTextFormattingPreset.class), order = 1)
    public String getPersonId() {
        try {
            return String.valueOf(this.getModel().getPersonId());
        } catch (ModelException e) {
            return ModelTable.getNullFormattedText().toString();
        }
    }

    @ModelTableDisplay(name = "#& (club)", format = @TableDisplayFormattingOptions(preset = ModelKeyTextFormattingPreset.class, alignment = TextAlignment.CENTER), order = 2)
    public String getClubId() {
        try {
            return String.valueOf(this.getModel().getClubId());
        } catch (ModelException e) {
            return ModelTable.getNullFormattedText().toString();
        }
    }

    @ModelTableDisplay(name = "N° affiliation", format = @TableDisplayFormattingOptions(styles = {TextStyle.ITALIC}), order = 2)
    public String getAffiliationNumber() {
        return this.getModel().getAffiliationNumber();
    }

}
