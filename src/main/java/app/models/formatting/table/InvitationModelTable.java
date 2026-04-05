package app.models.formatting.table;

import app.models.Camp;
import app.models.Invitation;
import app.models.ModelException;
import app.models.Person;
import app.models.formatting.ModelKeyTextFormattingPreset;
import utils.io.tables.ModelTableDisplay;
import utils.io.tables.TableDisplayFormattingOptions;
import utils.io.text_formatting.TextAlignment;
import utils.io.text_formatting.TextStyle;

public class InvitationModelTable extends IdentifiedModelTable<Invitation> {

    // ─── Constructors ─── //

    public InvitationModelTable(Invitation invitation) throws ModelTableException {
        super(invitation);
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

    @ModelTableDisplay(name = "Nom du stage", format = @TableDisplayFormattingOptions(styles = {TextStyle.ITALIC}), order = 3)
    public String getCampName() {
        Camp camp = this.getModel().getCamp();

        if (camp == null) {
            return ModelTable.getNullFormattedText().toString();
        }

        try {
            return camp.getName();
        } catch (Exception e) {
            return ModelTable.getNullFormattedText().toString();
        }
    }

    @ModelTableDisplay(name = "#& (personne)", format = @TableDisplayFormattingOptions(preset = ModelKeyTextFormattingPreset.class, alignment = TextAlignment.CENTER), order = 4)
    public String getPersonId() {
        try {
            return String.valueOf(this.getModel().getPersonId());
        } catch (ModelException e) {
            return ModelTable.getNullFormattedText().toString();
        }
    }

    @ModelTableDisplay(name = "Nom complet de la personne", format = @TableDisplayFormattingOptions(styles = {TextStyle.ITALIC}), order = 5)
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

}
