package app.models.formatting.table;

import app.models.Address;
import app.models.Country;
import app.models.ModelException;
import app.models.formatting.ModelKeyTextFormattingPreset;
import utils.io.helpers.tables.ModelTableDisplay;
import utils.io.helpers.tables.TableDisplayFormattingOptions;
import utils.io.helpers.texts.formatting.TextAlignment;

public class AddressModelTable extends IdentifiedModelTable<Address> {

    // ─── Constructors ─── //

    public AddressModelTable(Address address) throws ModelTableException {
        super(address);
    }

    // ─── Utility methods ─── //

    @ModelTableDisplay(name = "#& (pays)", format = @TableDisplayFormattingOptions(preset = ModelKeyTextFormattingPreset.class, alignment = TextAlignment.CENTER), order = 2)
    public String getCountryIso3() {
        try {
            return this.getModel().getCountryIso3();
        } catch (ModelException e) {
            return ModelTable.getNullFormattedText().toString();
        }
    }

    @ModelTableDisplay(name = "Code postal", order = 3)
    public String getZipCode() {
        return String.valueOf(this.getModel().getZipCode());
    }

    @ModelTableDisplay(name = "Ville", order = 4)
    public String getCity() {
        return this.getModel().getCity();
    }

    @ModelTableDisplay(name = "Rue", order = 5)
    public String getStreet() {
        return this.getModel().getStreet();
    }

    @ModelTableDisplay(name = "Numéro", order = 6)
    public String getNumber() {
        return this.getModel().getNumber();
    }

    @ModelTableDisplay(name = "Boîte", order = 7)
    public String getBoxNumber() {
        Integer boxNumber = this.getModel().getBoxNumber();
        return boxNumber != null ? String.valueOf(boxNumber) : ModelTable.getNullFormattedText().toString();
    }

}
