package app.models.formatting.table;

import app.models.Person;
import utils.io.tables.ModelTableDisplay;
import utils.io.tables.TableDisplayFormattingOptions;
import utils.io.text_formatting.TextStyle;

public class PersonModelTable extends IdentifiedModelTable<Person> {

    // ─── Constructors ─── //

    public PersonModelTable(Person model) throws ModelTableException {
        super(model);
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

}
