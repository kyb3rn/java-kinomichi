package app.views.menus.camps;

import app.AppState;
import app.models.managers.CampDataManager;
import app.models.managers.DataManagers;
import utils.io.helpers.texts.formatting.TextFormatter;
import utils.io.menus.StandardMenu;

public class ManageCampsMenu extends StandardMenu {

    // ─── Constructors ─── //

    public ManageCampsMenu() {
        int campsCount = DataManagers.getCountOf(CampDataManager.class);

        this.setTitle("Kinomichi - Gestion des stages (%d)".formatted(campsCount));

        if (campsCount > 0) {
            if (AppState.selectedCampId == -1) {
                this.addOption("Sélectionner un stage", "camps.select");
            } else {
                this.addOption("Gérer le stage " + TextFormatter.bold("#" + AppState.selectedCampId), "camps.manage.camp");
                this.addOption("Sélectionner un autre stage", "camps.select");
            }

            this.addSectionSeparationIndex();

            this.addOption("Lister les stages", "camps.list");
        }

        this.addOption("Ajouter un stage", "camps.add");

        if (campsCount > 0) {
            this.addOption("Modifier un stage", (String) null);
            this.addOption("Supprimer un stage", (String) null);
        }
    }

}
