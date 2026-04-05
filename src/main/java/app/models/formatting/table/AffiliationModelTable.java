package app.models.formatting.table;

import app.models.*;
import app.models.formatting.ModelKeyTextFormattingPreset;
import app.models.formatting.ModelPrimaryKeyTextFormattingPreset;
import app.utils.elements.time.TimeSlot;
import utils.io.tables.ModelTableDisplay;
import utils.io.tables.TableDisplayFormattingOptions;
import utils.io.text_formatting.TextAlignment;
import utils.io.text_formatting.TextStyle;

public class AffiliationModelTable extends IdentifiedModelTable<Affiliation> {

    // ─── Constructors ─── //

    public AffiliationModelTable(Affiliation affiliation) throws ModelTableException {
        super(affiliation);
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

    @ModelTableDisplay(name = "#& (club)", format = @TableDisplayFormattingOptions(preset = ModelKeyTextFormattingPreset.class, alignment = TextAlignment.CENTER), order = 4)
    public String getClubId() {
        try {
            return String.valueOf(this.getModel().getClubId());
        } catch (ModelException e) {
            return ModelTable.getNullFormattedText().toString();
        }
    }

    @ModelTableDisplay(name = "Nom du club", format = @TableDisplayFormattingOptions(styles = {TextStyle.ITALIC}), order = 5)
    public String getClubName() {
        Club club = this.getModel().getClub();

        if (club == null) {
            return ModelTable.getNullFormattedText().toString();
        }

        try {
            return club.getName();
        } catch (Exception e) {
            return ModelTable.getNullFormattedText().toString();
        }
    }

    @ModelTableDisplay(name = "N° affiliation", format = @TableDisplayFormattingOptions(styles = {TextStyle.ITALIC}), order = 6)
    public String getAffiliationNumber() {
        return this.getModel().getAffiliationNumber();
    }

    @ModelTableDisplay(name = "Période de validité", order = 7)
    public String getValidityPeriod() {
        TimeSlot validityPeriod = this.getModel().getValidityPeriod();
        return validityPeriod != null ? validityPeriod.toString() : ModelTable.getNullFormattedText().toString();
    }

}
