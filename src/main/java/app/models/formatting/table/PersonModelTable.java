package app.models.formatting.table;

import app.models.Affiliation;
import app.models.ModelException;
import app.models.Person;
import app.models.formatting.ModelKeyTextFormattingPreset;
import app.models.managers.AffiliationDataManager;
import app.models.managers.DataManagerException;
import app.models.managers.DataManagers;
import utils.io.helpers.tables.ModelTableDisplay;
import utils.io.helpers.tables.TableDisplayFormattingOptions;
import utils.io.helpers.texts.formatting.FormattedText;
import utils.io.helpers.texts.formatting.TextFormatter;
import utils.io.helpers.texts.formatting.TextStyle;

public class PersonModelTable extends IdentifiedModelTable<Person> {

    private Affiliation affiliation;

    // ─── Constructors ─── //

    public PersonModelTable(Person person) throws ModelTableException {
        super(person);

        try {
            AffiliationDataManager affiliationDataManager = DataManagers.get(AffiliationDataManager.class);
            this.affiliation = affiliationDataManager.getAffiliation(person.getId());
        } catch (DataManagerException | ModelException _) {
        }
    }

    // ─── Utility methods ─── //

    @ModelTableDisplay(name = "Prénom", format = @TableDisplayFormattingOptions(styles = {TextStyle.ITALIC}), order = 2)
    public String getFirstName() {
        return this.getModel().getFirstName();
    }

    @ModelTableDisplay(name = "Nom", format = @TableDisplayFormattingOptions(styles = {TextStyle.ITALIC}), order = 3)
    public String getLastName() {
        return this.getModel().getLastName();
    }

    @ModelTableDisplay(name = "Téléphone", order = 4)
    public String getPhone() {
        return this.getModel().getPhone();
    }

    @ModelTableDisplay(name = "Email", order = 5)
    public String getEmail() {
        return this.getModel().getEmail();
    }

    @ModelTableDisplay(name = "Numéro d'affiliation", order = 6)
    public String getAffiliated() {
        return this.affiliation != null ? this.affiliation.getAffiliationNumber() : ModelTable.getNullFormattedText().toString();
    }

    @ModelTableDisplay(name = "#& (club)", order = 7)
    public String getClubId() {
        try {
            return this.affiliation != null ? String.valueOf(this.affiliation.getClubId()) : ModelTable.getNullFormattedText().toString();
        } catch (ModelException e) {
            return ModelTable.getNullFormattedText().toString();
        }
    }

}
