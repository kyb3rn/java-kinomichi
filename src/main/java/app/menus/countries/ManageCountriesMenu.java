package app.menus.countries;

import app.models.managers.CountryDataManager;
import app.models.managers.DataManagers;
import app.models.managers.LoadDataManagerDataException;
import utils.io.menus.StandardMenu;

public class ManageCountriesMenu extends StandardMenu {

    // ─── Constructors ─── //

    public ManageCountriesMenu() {
        try {
            CountryDataManager countryDataManager = DataManagers.initAndGet(CountryDataManager.class);

            String unsavedIcon = countryDataManager.hasUnsavedChanges() ? " (!)" : "";

            this.setTitle("Kinomichi - Gestion des pays (%d)%s".formatted(countryDataManager.count(), unsavedIcon));

            this.addOption("Liste des pays", "countries.list");
            this.addOption("Retour", "main");
        } catch (LoadDataManagerDataException e) {
            throw new RuntimeException(e);
        }
    }

}
