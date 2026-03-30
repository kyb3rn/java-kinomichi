package app.menus.clubs;

import app.models.managers.ClubDataManager;
import app.models.managers.DataManagers;
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
