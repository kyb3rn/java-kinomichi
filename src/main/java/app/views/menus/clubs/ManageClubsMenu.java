package app.views.menus.clubs;

import app.models.ModelException;
import app.models.managers.ClubDataManager;
import app.models.managers.DataManagerException;
import app.models.managers.DataManagers;
import utils.io.helpers.Functions;
import utils.io.menus.StandardMenu;
import utils.io.menus.UnloadableMenuException;

public class ManageClubsMenu extends StandardMenu {

    // ─── Constructors ─── //

    public ManageClubsMenu() {
        try {
            ClubDataManager clubDataManager = DataManagers.get(ClubDataManager.class);

            String unsavedIcon = clubDataManager.hasUnsavedChanges() ? " (!)" : "";

            this.setTitle("Kinomichi - Gestion des clubs (%d)%s".formatted(clubDataManager.count(), unsavedIcon));

            this.addOption("Liste des clubs", "clubs.list");
            this.addOption("Ajouter un club", "clubs.add");
        } catch (DataManagerException | ModelException e) {
            System.out.println(Functions.styleAsErrorMessage("Impossible d'initialiser et récupérer le manager 'ClubDataManager'."));
            throw new UnloadableMenuException();
        }
    }

}
