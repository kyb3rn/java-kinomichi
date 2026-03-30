package app.menus;

import app.data_management.managers.ClubDataManager;
import app.data_management.managers.CountryDataManager;
import app.data_management.managers.DataManagers;
import utils.io.menus.StandardMenu;

public class MainMenu extends StandardMenu {

    // ─── Constructors ─── //

    public MainMenu() {
        super("Kinomichi - Menu d'administration");

        int countriesCount = DataManagers.getCountOf(CountryDataManager.class);
        int clubsCount = DataManagers.getCountOf(ClubDataManager.class);

        this.addOption("Parcourir les pays (%d)".formatted(countriesCount), "countries.manage");
        this.addOption("Gestion des clubs (%d)".formatted(clubsCount), "clubs.manage");
        this.addOption("Quitter", null);
    }

}
