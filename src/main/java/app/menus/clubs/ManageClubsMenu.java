package app.menus.clubs;

import app.data_management.managers.ClubDataManager;
import app.data_management.managers.DataManagers;
import utils.io.menus.StandardMenu;

public class ManageClubsMenu extends StandardMenu {

    // ─── Constructors ─── //

    public ManageClubsMenu() {
        super("Kinomichi - Gestion des clubs (%d)".formatted(DataManagers.getCountOf(ClubDataManager.class)));

        this.addOption("Liste des clubs", "clubs.list");
        this.addOption("Ajouter un club", "clubs.add");
        this.addOption("Retour", "main");
    }

}
