package app.views.main;

import app.events.CallUrlEvent;
import app.events.Event;
import app.models.managers.AffiliationDataManager;
import app.models.managers.CampDataManager;
import app.models.managers.ClubDataManager;
import app.models.managers.DataManager;
import app.models.managers.DataManagers;
import app.models.managers.PersonDataManager;
import app.views.View;
import utils.io.text_formatting.TextFormatter;
import utils.io.menus.MenuResponse;
import app.utils.menus.KinomichiStandardMenu;
import utils.io.menus.UnhandledMenuResponseType;

import java.util.List;

public class MainView extends View {

    @Override
    public Event render() {
        KinomichiStandardMenu mainMenu = new KinomichiStandardMenu("Kinomichi - Menu d'administration", null);
        mainMenu.setShowGoBackOption(false);

        this.addCampsOptions(mainMenu);
        this.addPersonsOption(mainMenu);
        this.addClubsOption(mainMenu);
        this.addAffiliationsOption(mainMenu);
        mainMenu.addOption("Explorer les données", new CallUrlEvent("/explore"));

        mainMenu.addSectionSeparationIndex();

        this.addBadlyInitializedDataManagersOption(mainMenu);
        this.addUnsavedDataManagersOption(mainMenu);

        mainMenu.setAfterDisplayHook(() -> {
            int campsCount = DataManagers.getCountOf(CampDataManager.class);
            if (campsCount == 0) {
                System.out.println(TextFormatter.yellow(TextFormatter.italic("Il n'y a pas encore de stage enregistré. ", TextFormatter.bold("Créez-en un !"))));
            }
            return null;
        });

        MenuResponse menuResponse = mainMenu.use();

        if (menuResponse.getResponse() instanceof Event event) {
            return event;
        } else {
            throw new UnhandledMenuResponseType();
        }
    }

    private void addUnsavedDataManagersOption(KinomichiStandardMenu mainMenu) {
        List<DataManager<?>> unsavedDataManagers = DataManagers.getUnsavedOnes();

        if (!unsavedDataManagers.isEmpty()) {
            mainMenu.addOption("Sauvegarder des données non enregistrées (%d)".formatted(unsavedDataManagers.size()), new CallUrlEvent("/data-managers/save"));
        }
    }

    private void addBadlyInitializedDataManagersOption(KinomichiStandardMenu mainMenu) {
        List<DataManager<?>> badlyInitializedDataManagers = DataManagers.getBadlyInitializedOnes();

        if (!badlyInitializedDataManagers.isEmpty()) {
            mainMenu.addOption("Re-initialisation de gestionnaires de données (%s)".formatted(badlyInitializedDataManagers.size()), new CallUrlEvent("/data-managers/reinit"));
        }
    }

    private void addCampsOptions(KinomichiStandardMenu mainMenu) {
        boolean campsInitialized = DataManagers.isInitialized(CampDataManager.class);

        if (!campsInitialized) {
            String campsOptionLabel = TextFormatter.strikethrough("Ajouter un stage") + " " + TextFormatter.red(TextFormatter.italic("(stages non chargés)"));
            mainMenu.addOption(campsOptionLabel, new CallUrlEvent("/"));
            mainMenu.addSectionSeparationIndex();
            return;
        }

        int campsCount = DataManagers.getCountOf(CampDataManager.class);
        if (campsCount > 0) {
            mainMenu.addOption("Sélectionner un stage à gérer", new CallUrlEvent("/camps/select"));
        }

        mainMenu.addOption("Ajouter un stage", new CallUrlEvent("/camps/add"));

        mainMenu.addSectionSeparationIndex();
    }

    private void addPersonsOption(KinomichiStandardMenu mainMenu) {
        String personsOptionLabel = "Gestion des personnes (%d)".formatted(DataManagers.getCountOf(PersonDataManager.class));

        boolean personsInitialized = DataManagers.isInitialized(PersonDataManager.class);

        if (!personsInitialized) {
            personsOptionLabel = TextFormatter.strikethrough(personsOptionLabel) + " " + TextFormatter.red(TextFormatter.italic("(personnes non chargées)"));
        }

        mainMenu.addOption(personsOptionLabel, new CallUrlEvent(personsInitialized ? "/persons/dashboard" : "/"));
    }

    private void addAffiliationsOption(KinomichiStandardMenu mainMenu) {
        String affiliationsOptionLabel = "Gestion des affiliations (%d)".formatted(DataManagers.getCountOf(AffiliationDataManager.class));

        boolean affiliationsInitialized = DataManagers.isInitialized(AffiliationDataManager.class);

        if (!affiliationsInitialized) {
            affiliationsOptionLabel = TextFormatter.strikethrough(affiliationsOptionLabel) + " " + TextFormatter.red(TextFormatter.italic("(affiliations non chargées)"));
        }

        mainMenu.addOption(affiliationsOptionLabel, new CallUrlEvent(affiliationsInitialized ? "/affiliations/dashboard" : "/"));
    }

    private void addClubsOption(KinomichiStandardMenu mainMenu) {
        String clubsOptionLabel = "Gestion des clubs (%d)".formatted(DataManagers.getCountOf(ClubDataManager.class));

        boolean clubsInitialized = DataManagers.isInitialized(ClubDataManager.class);

        if (!clubsInitialized) {
            clubsOptionLabel = TextFormatter.strikethrough(clubsOptionLabel) + " " + TextFormatter.red(TextFormatter.italic("(clubs non chargés)"));
        }

        mainMenu.addOption(clubsOptionLabel, new CallUrlEvent(clubsInitialized ? "/clubs/dashboard" : "/"));
    }

}
