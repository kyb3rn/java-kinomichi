package app.menus.countries;

import app.data_management.managers.CountryDataManager;
import app.data_management.managers.DataManagers;
import utils.io.menus.StandardMenu;

public class ManageCountriesMenu extends StandardMenu {

    // ─── Constructors ─── //

    public ManageCountriesMenu() {
        super("Kinomichi - Gestion des pays (%d)".formatted(DataManagers.getCountOf(CountryDataManager.class)));

        this.addOption("Liste des pays", "countries.list");
        this.addOption("Retour", "main");
    }

}
