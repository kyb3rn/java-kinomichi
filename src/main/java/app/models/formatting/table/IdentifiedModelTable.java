package app.models.formatting.table;

import app.models.IdentifiedModel;
import app.models.formatting.ModelPrimaryKeyTextFormattingPreset;
import utils.io.helpers.tables.ModelTableDisplay;
import utils.io.helpers.tables.TableDisplayFormattingOptions;

public abstract class IdentifiedModelTable<T extends IdentifiedModel> extends ModelTable<T> {

    public IdentifiedModelTable(T model) throws ModelTableException {
        super(model);
    }

    // ─── Utility methods ─── //

    @ModelTableDisplay(name = "#", format = @TableDisplayFormattingOptions(preset = ModelPrimaryKeyTextFormattingPreset.class), order = 1)
    public int getId() {
        return this.getModel().getId();
    }

}
