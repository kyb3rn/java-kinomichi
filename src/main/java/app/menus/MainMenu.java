package app.menus;

import app.AppState;
import app.models.managers.AddressDataManager;
import app.models.managers.CampDataManager;
import app.models.managers.ClubDataManager;
import app.models.managers.CountryDataManager;
import app.models.managers.DataManager;
import app.models.managers.DataManagers;
import utils.io.helpers.texts.formatting.TextFormatter;
import utils.io.menus.StandardMenu;

import java.util.List;

public class MainMenu extends StandardMenu {

    // ─── Constructors ─── //

    public MainMenu() {
        super("Kinomichi - Menu d'administration");

        this.addCampsOption();
        this.addCountriesOption();
        this.addAddressesOption();
        this.addClubsOption();

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

    private void addCampsOption() {
        String campsOptionLabel = "Gestion des stages (%d)".formatted(DataManagers.getCountOf(CampDataManager.class));

        boolean campsInitialized = DataManagers.isInitialized(CampDataManager.class);

        if (!campsInitialized) {
            campsOptionLabel = TextFormatter.strikethrough(campsOptionLabel) + " " + TextFormatter.red(TextFormatter.italic("(stages non chargés)"));
        }

        this.addOption(campsOptionLabel, campsInitialized ? "camps.manage" : "main");
    }

    private void addCountriesOption() {
        String countriesOptionLabel = "Parcourir les pays (%d)".formatted(DataManagers.getCountOf(CountryDataManager.class));

        boolean countriesInitialized = DataManagers.isInitialized(CountryDataManager.class);

        if (!countriesInitialized) {
            countriesOptionLabel = TextFormatter.strikethrough(countriesOptionLabel) + " " + TextFormatter.red(TextFormatter.italic("(pays non chargés)"));
        }

        this.addOption(countriesOptionLabel, countriesInitialized ? "countries.manage" : "main");
    }

    private void addAddressesOption() {
        String addressesOptionLabel = "Parcourir les adresses (%d)".formatted(DataManagers.getCountOf(AddressDataManager.class));

        boolean addressesInitialized = DataManagers.isInitialized(AddressDataManager.class);

        if (!addressesInitialized) {
            addressesOptionLabel = TextFormatter.strikethrough(addressesOptionLabel) + " " + TextFormatter.red(TextFormatter.italic("(adresses non chargées)"));
        }

        this.addOption(addressesOptionLabel, addressesInitialized ? "addresses.manage" : "main");
    }

    private void addClubsOption() {
        String clubsOptionLabel = "Gestion des clubs (%d)".formatted(DataManagers.getCountOf(ClubDataManager.class));

        boolean clubsInitialized = DataManagers.isInitialized(ClubDataManager.class);

        if (!clubsInitialized) {
            clubsOptionLabel = TextFormatter.strikethrough(clubsOptionLabel) + " " + TextFormatter.red(TextFormatter.italic("(clubs non chargés)"));
        }

        this.addOption(clubsOptionLabel, clubsInitialized ? "clubs.manage" : "main");
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
