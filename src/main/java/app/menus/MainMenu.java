package app.menus;

import app.models.managers.CampDataManager;
import app.models.managers.DataManagers;
import utils.io.helpers.texts.formatting.TextFormatter;
import utils.io.menus.StandardMenu;

public class MainMenu extends StandardMenu {

    // ─── Constructors ─── //

    public MainMenu() {
        super("Kinomichi - Menu d'administration");

        int campsCount = DataManagers.getCountOf(CampDataManager.class);

        if (campsCount > 0) {
            this.addOption("Sélectionner un stage (%s)".formatted(campsCount), (String) null);
            this.addOption("Ajouter un stage", (String) null);
            this.addOption("Modifier un stage", (String) null);
            this.addOption("Supprimer un stage", (String) null);
        } else {
            this.addOption("Ajouter un stage", (String) null);
        }

        this.addOption("Quitter");
    }

    // ─── Overrides & inheritance ─── //

    @Override
    public void afterDisplay() {
        int campsCount = DataManagers.getCountOf(CampDataManager.class);
        if (campsCount == 0) {
            System.out.printf(TextFormatter.yellow(TextFormatter.italic("Il n'y a pas encore de stage enregistré. ", TextFormatter.bold("Créez-en un !"))) + "%n%n");
        }
    }

}
