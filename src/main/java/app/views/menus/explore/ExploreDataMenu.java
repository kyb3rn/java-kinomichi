package app.views.menus.explore;

import app.models.managers.*;
import utils.io.helpers.texts.formatting.TextFormatter;
import utils.io.menus.StandardMenu;

public class ExploreDataMenu extends StandardMenu {

    // ─── Constructors ─── //

    public ExploreDataMenu() {
        super("Explorer les données");

        this.addDataManagerOption("Stages", CampDataManager.class, "camps.list", "stages non chargés");
        this.addDataManagerOption("Personnes", PersonDataManager.class, "persons.list", "personnes non chargées");
        this.addDataManagerOption("Affiliés", AffiliatedDataManager.class, "affiliateds.list", "affiliés non chargés");
        this.addDataManagerOption("Clubs", ClubDataManager.class, "clubs.list", "clubs non chargés");
        this.addDataManagerOption("Adresses", AddressDataManager.class, "addresses.list", "adresses non chargées");
        this.addDataManagerOption("Pays", CountryDataManager.class, "countries.list", "pays non chargés");
    }

    // ─── Utility methods ─── //

    private void addDataManagerOption(String label, Class<? extends DataManager<?>> managerClass, String route, String uninitializedErrorLabel) {
        String optionLabel = "%s (%d)".formatted(label, DataManagers.getCountOf(managerClass));
        boolean managerInitialized = DataManagers.isInitialized(managerClass);

        if (!managerInitialized) {
            optionLabel = TextFormatter.strikethrough(optionLabel) + " " + TextFormatter.red(TextFormatter.italic("(%s)".formatted(uninitializedErrorLabel)));
        }

        this.addOption(optionLabel, managerInitialized ? route : "explore");
    }

}
