package app.models.formatting.table;

import app.models.Club;
import app.models.ModelException;
import app.models.formatting.ModelKeyTextFormattingPreset;
import utils.io.tables.ModelTableDisplay;
import utils.io.tables.TableDisplayFormattingOptions;
import utils.io.text_formatting.TextAlignment;
import utils.io.text_formatting.TextStyle;

public class ClubModelTable extends IdentifiedModelTable<Club> {

    // ─── Constructors ─── //

    public ClubModelTable(Club club) throws ModelTableException {
        super(club);
    }

    // ─── Utility methods ─── //

    @ModelTableDisplay(name = "Nom", format = @TableDisplayFormattingOptions(styles = {TextStyle.ITALIC}), order = 2)
    public String getName() {
        return this.getModel().getName();
    }

    @ModelTableDisplay(name = "#& (adresse)", format = @TableDisplayFormattingOptions(preset = ModelKeyTextFormattingPreset.class, alignment = TextAlignment.CENTER), order = 3)
    public String getAddressId() {
        try {
            return String.valueOf(this.getModel().getAddressId());
        } catch (ModelException e) {
            return ModelTable.getNullFormattedText().toString();
        }
    }

    @ModelTableDisplay(name = "Lien Google Maps", order = 4)
    public String getGoogleMapsLink() {
        return this.getModel().getGoogleMapsLink();
    }

}
