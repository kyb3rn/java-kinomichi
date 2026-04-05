package app.models.formatting.table;

import app.models.DinnerReservation;
import app.models.ModelException;
import app.models.Person;
import app.models.formatting.ModelKeyTextFormattingPreset;
import utils.io.tables.ModelTableDisplay;
import utils.io.tables.TableDisplayFormattingOptions;
import utils.io.text_formatting.TextAlignment;
import utils.io.text_formatting.TextStyle;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class DinnerReservationModelTable extends IdentifiedModelTable<DinnerReservation> {

    // ─── Constructors ─── //

    public DinnerReservationModelTable(DinnerReservation dinnerReservation) throws ModelTableException {
        super(dinnerReservation);
    }

    // ─── Utility methods ─── //

    @ModelTableDisplay(name = "#& (personne)", format = @TableDisplayFormattingOptions(preset = ModelKeyTextFormattingPreset.class, alignment = TextAlignment.CENTER), order = 2)
    public String getPersonId() {
        try {
            return String.valueOf(this.getModel().getPersonId());
        } catch (ModelException e) {
            return ModelTable.getNullFormattedText().toString();
        }
    }

    @ModelTableDisplay(name = "Nom complet de la personne", format = @TableDisplayFormattingOptions(styles = {TextStyle.ITALIC}), order = 3)
    public String getPersonName() {
        Person person = this.getModel().getPerson();

        if (person == null) {
            return ModelTable.getNullFormattedText().toString();
        }

        try {
            return person.getFullName();
        } catch (Exception e) {
            return ModelTable.getNullFormattedText().toString();
        }
    }

    @ModelTableDisplay(name = "#& (repas)", format = @TableDisplayFormattingOptions(preset = ModelKeyTextFormattingPreset.class, alignment = TextAlignment.CENTER), order = 4)
    public String getDinnerId() {
        try {
            return String.valueOf(this.getModel().getDinnerId());
        } catch (ModelException e) {
            return ModelTable.getNullFormattedText().toString();
        }
    }

    @ModelTableDisplay(name = "Label du repas", format = @TableDisplayFormattingOptions(styles = {TextStyle.ITALIC}), order = 5)
    public String getDinnerLabel() {
        if (this.getModel().getDinner() == null) {
            return ModelTable.getNullFormattedText().toString();
        }

        return this.getModel().getDinner().getLabel();
    }

    @ModelTableDisplay(name = "Horaire du repas", order = 6)
    public String getDinnerTimeSlot() {
        if (this.getModel().getDinner() == null || this.getModel().getDinner().getTimeSlot() == null) {
            return ModelTable.getNullFormattedText().toString();
        }

        return this.getModel().getDinner().getTimeSlot().toPrettyStringFormat();
    }

    @ModelTableDisplay(name = "Annulation", order = 7)
    public String getCancellationDatetime() {
        Instant cancellationDatetime = this.getModel().getCancellationDatetime();

        if (cancellationDatetime == null) {
            return ModelTable.getNullFormattedText().toString();
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH'h'mm").withZone(ZoneId.systemDefault());
        return formatter.format(cancellationDatetime);
    }

}
