package app.menus;

import app.models.managers.ClubDataManager;
import app.models.managers.CountryDataManager;
import app.models.managers.DataManager;
import app.models.managers.DataManagers;
import utils.io.helpers.texts.formatting.TextFormatter;
import utils.io.menus.MenuLeadTo;
import utils.io.menus.StandardMenu;

import java.util.List;

public class MainMenu extends StandardMenu {

    // ─── Constructors ─── //

    public MainMenu() {
        super("Kinomichi - Menu d'administration");

        this.addOption1();
        this.addOption2();

        List<DataManager<?>> badlyInitializedDataManagers = DataManagers.getBadlyInitializedOnes();

        if (!badlyInitializedDataManagers.isEmpty()) {
            this.addOption("Re-initialisation de gestionnaires de données (%s)".formatted(badlyInitializedDataManagers.size()), "data_managers.reinit");
        }

        List<DataManager<?>> unsavedDataManagers = DataManagers.getUnsavedOnes();

        if (!unsavedDataManagers.isEmpty()) {
            this.addOption("Sauvegarder des données non enregistrées (%d)".formatted(unsavedDataManagers.size()), "data_managers.save");
        }

        this.addOption("Quitter");
    }

    // ─── Utility methods ─── //

    private void addOption1() {
        String option1 = "Parcourir les pays (%d)".formatted(DataManagers.getCountOf(CountryDataManager.class));

        boolean countriesInitialized = DataManagers.isInitialized(CountryDataManager.class);

        if (!countriesInitialized) {
            option1 = TextFormatter.strikethrough(option1) + " " + TextFormatter.red(TextFormatter.italic("(pays non chargés)"));
        }

        this.addOption(option1, countriesInitialized ? "countries.manage" : "main");
    }

    private void addOption2() {
        String option2 = "Gestion des clubs (%d)".formatted(DataManagers.getCountOf(ClubDataManager.class));

        boolean clubsInitialized = DataManagers.isInitialized(ClubDataManager.class);

        if (!clubsInitialized) {
            option2 = TextFormatter.strikethrough(option2) + " " + TextFormatter.red(TextFormatter.italic("(clubs non chargés)"));
        }

        this.addOption(option2, clubsInitialized ? "clubs.manage" : "main");
    }

}
