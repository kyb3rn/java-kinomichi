package app.models.formatting.table;

import app.models.ModelException;
import app.models.Session;
import app.models.formatting.ModelKeyTextFormattingPreset;
import app.utils.elements.time.TimeSlot;
import utils.io.tables.ModelTableDisplay;
import utils.io.tables.TableDisplayFormattingOptions;
import utils.io.text_formatting.TextAlignment;
import utils.io.text_formatting.TextStyle;

public class SessionModelTable extends IdentifiedModelTable<Session> {

    // ─── Constructors ─��─ //

    public SessionModelTable(Session session) throws ModelTableException {
        super(session);
    }

    // ─── Utility methods ─── //

    @ModelTableDisplay(name = "#& (stage)", format = @TableDisplayFormattingOptions(preset = ModelKeyTextFormattingPreset.class, alignment = TextAlignment.CENTER), order = 2)
    public String getCampId() {
        try {
            return String.valueOf(this.getModel().getCampId());
        } catch (ModelException e) {
            return ModelTable.getNullFormattedText().toString();
        }
    }

    @ModelTableDisplay(name = "Label", format = @TableDisplayFormattingOptions(styles = {TextStyle.ITALIC}), order = 3)
    public String getLabel() {
        return this.getModel().getLabel();
    }

    @ModelTableDisplay(name = "Créneau horaire", order = 4)
    public String getTimeSlot() {
        TimeSlot timeSlot = this.getModel().getTimeSlot();
        return timeSlot != null ? timeSlot.toString() : ModelTable.getNullFormattedText().toString();
    }

}
