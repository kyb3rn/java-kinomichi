package app.models.formatting.table;

import app.models.Camp;
import app.models.ModelException;
import app.models.formatting.ModelKeyTextFormattingPreset;
import utils.io.tables.ModelTableDisplay;
import utils.io.tables.TableDisplayFormattingOptions;
import utils.io.text_formatting.TextAlignment;
import utils.io.text_formatting.TextStyle;
import app.utils.elements.time.TimeSlot;

public class CampModelTable extends IdentifiedModelTable<Camp> {

    // ─── Constructors ─── //

    public CampModelTable(Camp camp) throws ModelTableException {
        super(camp);
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

    @ModelTableDisplay(name = "Début et fin", order = 4)
    public String getTimeSlot() {
        TimeSlot timeSlot = this.getModel().getTimeSlot();
        return timeSlot != null ? timeSlot.toString() : ModelTable.getNullFormattedText().toString();
    }

}
