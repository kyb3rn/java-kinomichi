package app.menus.camps;

import app.models.managers.AddressDataManager;
import app.models.managers.ClubDataManager;
import app.models.managers.CountryDataManager;
import app.models.managers.DataManager;
import app.models.managers.DataManagers;
import utils.io.helpers.texts.formatting.TextFormatter;
import utils.io.menus.MenuLeadTo;
import utils.io.menus.StandardMenu;

import java.util.List;

public class ManageCampMenu extends StandardMenu {

    // ─── Constructors ─── //

    public ManageCampMenu() {
        super("Kinomichi - Menu d'administration");

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

        this.addOption("Retour", "main");
    }

    // ─── Utility methods ─── //

    private void addCountriesOption() {
        String countriesOptionLabel = "Parcourir les pays (%d)".formatted(DataManagers.getCountOf(CountryDataManager.class));

        boolean countriesInitialized = DataManagers.isInitialized(CountryDataManager.class);

        if (!countriesInitialized) {
            countriesOptionLabel = TextFormatter.strikethrough(countriesOptionLabel) + " " + TextFormatter.red(TextFormatter.italic("(pays non chargés)"));
        }

        this.addOption(countriesOptionLabel, countriesInitialized ? "countries.manage" : "camps.manage");
    }

    private void addAddressesOption() {
        String addressesOptionLabel = "Parcourir les adresses (%d)".formatted(DataManagers.getCountOf(AddressDataManager.class));

        boolean addressesInitialized = DataManagers.isInitialized(AddressDataManager.class);

        if (!addressesInitialized) {
            addressesOptionLabel = TextFormatter.strikethrough(addressesOptionLabel) + " " + TextFormatter.red(TextFormatter.italic("(adresses non chargées)"));
        }

        this.addOption(addressesOptionLabel, addressesInitialized ? "addresses.manage" : "camps.manage");
    }

    private void addClubsOption() {
        String clubsOptionLabel = "Gestion des clubs (%d)".formatted(DataManagers.getCountOf(ClubDataManager.class));

        boolean clubsInitialized = DataManagers.isInitialized(ClubDataManager.class);

        if (!clubsInitialized) {
            clubsOptionLabel = TextFormatter.strikethrough(clubsOptionLabel) + " " + TextFormatter.red(TextFormatter.italic("(clubs non chargés)"));
        }

        this.addOption(clubsOptionLabel, clubsInitialized ? "clubs.manage" : "camps.manage");
    }

}
