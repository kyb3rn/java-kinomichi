package app.models.formatting.table;

import app.models.ModelException;
import app.models.Person;
import app.models.Session;
import app.models.SessionTrainer;
import app.models.formatting.ModelKeyTextFormattingPreset;
import utils.io.tables.ModelTableDisplay;
import utils.io.tables.TableDisplayFormattingOptions;
import utils.io.text_formatting.TextAlignment;
import utils.io.text_formatting.TextStyle;

public class SessionTrainerModelTable extends IdentifiedModelTable<SessionTrainer> {

    // ─── Constructors ─── //

    public SessionTrainerModelTable(SessionTrainer sessionTrainer) throws ModelTableException {
        super(sessionTrainer);
    }

    // ─── Utility methods ─── //

    @ModelTableDisplay(name = "#& (session)", format = @TableDisplayFormattingOptions(preset = ModelKeyTextFormattingPreset.class, alignment = TextAlignment.CENTER), order = 2)
    public String getSessionId() {
        try {
            return String.valueOf(this.getModel().getSessionId());
        } catch (ModelException e) {
            return ModelTable.getNullFormattedText().toString();
        }
    }

    @ModelTableDisplay(name = "Label de la session", format = @TableDisplayFormattingOptions(styles = {TextStyle.ITALIC}), order = 3)
    public String getSessionLabel() {
        Session session = this.getModel().getSession();

        if (session == null) {
            return ModelTable.getNullFormattedText().toString();
        }

        return session.getLabel();
    }

    @ModelTableDisplay(name = "#& (personne)", format = @TableDisplayFormattingOptions(preset = ModelKeyTextFormattingPreset.class, alignment = TextAlignment.CENTER), order = 4)
    public String getPersonId() {
        try {
            return String.valueOf(this.getModel().getPersonId());
        } catch (ModelException e) {
            return ModelTable.getNullFormattedText().toString();
        }
    }

    @ModelTableDisplay(name = "Nom complet du formateur", format = @TableDisplayFormattingOptions(styles = {TextStyle.ITALIC}), order = 5)
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
