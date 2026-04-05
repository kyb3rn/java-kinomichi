package app.views.explore;

import app.events.CallUrlEvent;
import app.events.Event;
import app.models.managers.*;
import app.views.View;
import utils.io.text_formatting.TextFormatter;
import utils.io.menus.MenuResponse;
import app.utils.menus.KinomichiStandardMenu;
import utils.io.menus.UnhandledMenuResponseType;

public class ExploreDataView extends View {

    // ─── Overrides & inheritance ─── //

    @Override
    public Event render() {
        KinomichiStandardMenu exploreMenu = new KinomichiStandardMenu("Explorer les données", new CallUrlEvent("/"));

        this.addDataManagerOption(exploreMenu, "Stages", CampDataManager.class, "/explore/camps", "stages non chargés");
        this.addDataManagerOption(exploreMenu, "Personnes", PersonDataManager.class, "/explore/persons", "personnes non chargées");
        this.addDataManagerOption(exploreMenu, "Affiliations", AffiliationDataManager.class, "/explore/affiliations", "affiliés non chargés");
        this.addDataManagerOption(exploreMenu, "Repas", DinnerDataManager.class, "/explore/dinners", "repas non chargés");
        this.addDataManagerOption(exploreMenu, "Hébergements", LodgingDataManager.class, "/explore/lodgings", "hébergements non chargés");
        this.addDataManagerOption(exploreMenu, "Sessions", SessionDataManager.class, "/explore/sessions", "sessions non chargées");
        this.addDataManagerOption(exploreMenu, "Formateurs de session", SessionTrainerDataManager.class, "/explore/session-trainers", "formateurs de session non chargés");
        this.addDataManagerOption(exploreMenu, "Inscriptions aux sessions", SessionRegistrationDataManager.class, "/explore/session-registrations", "inscriptions aux sessions non chargées");
        this.addDataManagerOption(exploreMenu, "Invitations", InvitationDataManager.class, "/explore/invitations", "invitations non chargées");
        this.addDataManagerOption(exploreMenu, "Réservations de repas", DinnerReservationDataManager.class, "/explore/dinner-reservations", "réservations de repas non chargées");
        this.addDataManagerOption(exploreMenu, "Réservations d'hébergement", LodgingReservationDataManager.class, "/explore/lodging-reservations", "réservations d'hébergement non chargées");
        this.addDataManagerOption(exploreMenu, "Clubs", ClubDataManager.class, "/explore/clubs", "clubs non chargés");
        this.addDataManagerOption(exploreMenu, "Adresses", AddressDataManager.class, "/explore/addresses", "adresses non chargées");
        this.addDataManagerOption(exploreMenu, "Pays", CountryDataManager.class, "/explore/countries", "pays non chargés");

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
