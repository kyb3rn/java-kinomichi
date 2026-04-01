package app.views;

import app.events.CallUrlEvent;
import app.events.Event;
import app.models.managers.CampDataManager;
import app.models.managers.ClubDataManager;
import app.models.managers.DataManager;
import app.models.managers.DataManagers;
import utils.io.helpers.texts.formatting.TextFormatter;
import utils.io.menus.MenuResponse;
import utils.io.menus.StandardMenu;
import utils.io.menus.UnhandledMenuResponseType;

import java.util.List;

public class MainView extends View {

    @Override
    public Event render() {
        StandardMenu mainMenu = new StandardMenu("Kinomichi - Menu d'administration");
        mainMenu.setShowGoBackOption(false);

        this.addCampsOption(mainMenu);
        this.addClubsOption(mainMenu);
        mainMenu.addOption("Explorer les données", new CallUrlEvent("/explore"));

        mainMenu.addSectionSeparationIndex();

        this.addBadlyInitializedDataManagersOption(mainMenu);
        this.addUnsavedDataManagersOption(mainMenu);

        MenuResponse menuResponse = mainMenu.use();

        if (menuResponse.getResponse() instanceof Event event) {
            return event;
        } else {
            throw new UnhandledMenuResponseType();
        }
    }

    private void addUnsavedDataManagersOption(StandardMenu mainMenu) {
        List<DataManager<?>> unsavedDataManagers = DataManagers.getUnsavedOnes();

        if (!unsavedDataManagers.isEmpty()) {
            mainMenu.addOption("Sauvegarder des données non enregistrées (%d)".formatted(unsavedDataManagers.size()), new CallUrlEvent("/data-managers/save"));
        }
    }

    private void addBadlyInitializedDataManagersOption(StandardMenu mainMenu) {
        List<DataManager<?>> badlyInitializedDataManagers = DataManagers.getBadlyInitializedOnes();

        if (!badlyInitializedDataManagers.isEmpty()) {
            mainMenu.addOption("Re-initialisation de gestionnaires de données (%s)".formatted(badlyInitializedDataManagers.size()), new CallUrlEvent("/data-managers/reinit"));
        }
    }

    private void addCampsOption(StandardMenu mainMenu) {
        String campsOptionLabel = "Gestion des stages (%d)".formatted(DataManagers.getCountOf(CampDataManager.class));

        boolean campsInitialized = DataManagers.isInitialized(CampDataManager.class);

        if (!campsInitialized) {
            campsOptionLabel = TextFormatter.strikethrough(campsOptionLabel) + " " + TextFormatter.red(TextFormatter.italic("(stages non chargés)"));
        }

        mainMenu.addOption(campsOptionLabel, new CallUrlEvent(campsInitialized ? "/camps" : "/"));
    }

    private void addClubsOption(StandardMenu mainMenu) {
        String clubsOptionLabel = "Gestion des clubs (%d)".formatted(DataManagers.getCountOf(ClubDataManager.class));

        boolean clubsInitialized = DataManagers.isInitialized(ClubDataManager.class);

        if (!clubsInitialized) {
            clubsOptionLabel = TextFormatter.strikethrough(clubsOptionLabel) + " " + TextFormatter.red(TextFormatter.italic("(clubs non chargés)"));
        }

        mainMenu.addOption(clubsOptionLabel, new CallUrlEvent(clubsInitialized ? "/clubs" : "/"));
    }

}
