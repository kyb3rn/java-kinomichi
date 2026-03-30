package app.menus.countries;

import app.models.managers.CountryDataManager;
import app.models.managers.DataManagers;
import utils.io.menus.StandardMenu;

public class ManageCountriesMenu extends StandardMenu {

    // ─── Constructors ─── //

    public ManageCountriesMenu() {
        super("Kinomichi - Gestion des pays (%d)".formatted(DataManagers.getCountOf(CountryDataManager.class)));

        this.addOption("Liste des pays", "countries.list");
        this.addOption("Retour", "main");
    }

}
