package app.models.formatting.table;

import app.models.Country;
import app.models.formatting.ModelPrimaryKeyTextFormattingPreset;
import utils.io.helpers.tables.ModelTableDisplay;
import utils.io.helpers.tables.TableDisplayFormattingOptions;
import utils.io.helpers.texts.formatting.TextAlignment;
import utils.io.helpers.texts.formatting.TextStyle;

public class CountryModelTable extends ModelTable<Country> {

    // ─── Constructors ─── //

    public CountryModelTable(Country country) throws ModelTableException {
        super(country);
    }

    // ─── Utility methods ─── //

    @ModelTableDisplay(name = "ISO 3", format = @TableDisplayFormattingOptions(preset = ModelPrimaryKeyTextFormattingPreset.class, alignment = TextAlignment.CENTER), order = 1)
    public String getIso3() {
        return this.getModel().getIso3();
    }

    @ModelTableDisplay(name = "ISO 2", format = @TableDisplayFormattingOptions(alignment = TextAlignment.CENTER), order = 2)
    public String getIso2() {
        return this.getModel().getIso2();
    }

    @ModelTableDisplay(name = "Nom", format = @TableDisplayFormattingOptions(styles = {TextStyle.ITALIC}), order = 3)
    public String getName() {
        return this.getModel().getName();
    }

}
