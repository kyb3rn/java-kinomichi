package app.views.explore;

import app.events.CallUrlEvent;
import app.events.Event;
import app.models.managers.*;
import app.views.View;
import utils.io.helpers.texts.formatting.TextFormatter;
import utils.io.menus.MenuResponse;
import app.utils.menus.KinomichiStandardMenu;
import utils.io.menus.UnhandledMenuResponseType;

public class ExploreDataView extends View {

    // ─── Overrides & inheritance ─── //

    @Override
    public Event render() {
        KinomichiStandardMenu exploreMenu = new KinomichiStandardMenu("Explorer les données", new CallUrlEvent("/"));

        this.addDataManagerOption(exploreMenu, "Stages", CampDataManager.class, "/camps/list", "stages non chargés");
        this.addDataManagerOption(exploreMenu, "Personnes", PersonDataManager.class, "/persons/list", "personnes non chargées");
        this.addDataManagerOption(exploreMenu, "Affiliations", AffiliationDataManager.class, "/affiliations/list", "affiliés non chargés");
        this.addDataManagerOption(exploreMenu, "Clubs", ClubDataManager.class, "/clubs/list", "clubs non chargés");
        this.addDataManagerOption(exploreMenu, "Adresses", AddressDataManager.class, "/addresses/list", "adresses non chargées");
        this.addDataManagerOption(exploreMenu, "Pays", CountryDataManager.class, "/countries/list", "pays non chargés");

        MenuResponse menuResponse = exploreMenu.use();

        if (menuResponse.getResponse() instanceof Event event) {
            return event;
        } else {
            throw new UnhandledMenuResponseType();
        }
    }

    // ─── Utility methods ─── //

    private void addDataManagerOption(KinomichiStandardMenu menu, String label, Class<? extends DataManager<?>> managerClass, String route, String uninitializedErrorLabel) {
        String optionLabel = "%s (%d)".formatted(label, DataManagers.getCountOf(managerClass));
        boolean managerInitialized = DataManagers.isInitialized(managerClass);

        if (!managerInitialized) {
            optionLabel = TextFormatter.strikethrough(optionLabel) + " " + TextFormatter.red(TextFormatter.italic("(%s)".formatted(uninitializedErrorLabel)));
        }

        menu.addOption(optionLabel, new CallUrlEvent(managerInitialized ? route : "/explore"));
    }

}
