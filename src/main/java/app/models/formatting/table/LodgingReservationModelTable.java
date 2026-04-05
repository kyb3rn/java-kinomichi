package app.models.formatting.table;

import app.models.LodgingReservation;
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

public class LodgingReservationModelTable extends IdentifiedModelTable<LodgingReservation> {

    // ─── Constructors ─── //

    public LodgingReservationModelTable(LodgingReservation lodgingReservation) throws ModelTableException {
        super(lodgingReservation);
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

    @ModelTableDisplay(name = "#& (hébergement)", format = @TableDisplayFormattingOptions(preset = ModelKeyTextFormattingPreset.class, alignment = TextAlignment.CENTER), order = 4)
    public String getLodgingId() {
        try {
            return String.valueOf(this.getModel().getLodgingId());
        } catch (ModelException e) {
            return ModelTable.getNullFormattedText().toString();
        }
    }

    @ModelTableDisplay(name = "Label de l'hébergement", format = @TableDisplayFormattingOptions(styles = {TextStyle.ITALIC}), order = 5)
    public String getLodgingLabel() {
        if (this.getModel().getLodging() == null) {
            return ModelTable.getNullFormattedText().toString();
        }

        return this.getModel().getLodging().getLabel();
    }

    @ModelTableDisplay(name = "Horaire de l'hébergement", order = 6)
    public String getLodgingTimeSlot() {
        if (this.getModel().getLodging() == null || this.getModel().getLodging().getTimeSlot() == null) {
            return ModelTable.getNullFormattedText().toString();
        }

        return this.getModel().getLodging().getTimeSlot().toPrettyStringFormat();
    }

    @ModelTableDisplay(name = "Chambre individuelle", format = @TableDisplayFormattingOptions(alignment = TextAlignment.CENTER), order = 7)
    public String getSingleRoomOption() {
        return this.getModel().isSingleRoomOption() ? "Oui" : "Non";
    }

    @ModelTableDisplay(name = "Annulation", order = 8)
    public String getCancellationDatetime() {
        Instant cancellationDatetime = this.getModel().getCancellationDatetime();

        if (cancellationDatetime == null) {
            return ModelTable.getNullFormattedText().toString();
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH'h'mm").withZone(ZoneId.systemDefault());
        return formatter.format(cancellationDatetime);
    }

}
