package app.menus.clubs;

import app.models.managers.ClubDataManager;
import app.models.managers.DataManagers;
import app.models.managers.LoadDataManagerDataException;
import utils.io.menus.StandardMenu;

public class ManageClubsMenu extends StandardMenu {

    // ─── Constructors ─── //

    public ManageClubsMenu() {
        try {
            ClubDataManager clubDataManager = DataManagers.initAndGet(ClubDataManager.class);

            String unsavedIcon = clubDataManager.hasUnsavedChanges() ? " (!)" : "";

            this.setTitle("Kinomichi - Gestion des clubs (%d)%s".formatted(clubDataManager.count(), unsavedIcon));

            this.addOption("Liste des clubs", "clubs.list");
            this.addOption("Ajouter un club", "clubs.add");
            this.addOption("Retour", "main");
        } catch (LoadDataManagerDataException e) {
            throw new RuntimeException(e);
        }
    }

}
