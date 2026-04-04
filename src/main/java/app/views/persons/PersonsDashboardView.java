package app.views.persons;

import app.events.CallUrlEvent;
import app.events.Event;
import app.views.View;
import utils.io.menus.MenuResponse;
import app.utils.menus.KinomichiStandardMenu;
import utils.io.menus.UnhandledMenuResponseType;

public class PersonsDashboardView extends View {

    // ─── Properties ─── //

    private final int personCount;
    private final boolean hasUnsavedChanges;

    // ─── Constructors ─── //

    public PersonsDashboardView(int personCount, boolean hasUnsavedChanges) {
        this.personCount = personCount;
        this.hasUnsavedChanges = hasUnsavedChanges;
    }

    // ─── Overrides & inheritance ─── //

    @Override
    public Event render() {
        String unsavedIcon = this.hasUnsavedChanges ? " (!)" : "";

        KinomichiStandardMenu managePersonsMenu = new KinomichiStandardMenu("Kinomichi - Gestion des personnes (%d)%s".formatted(this.personCount, unsavedIcon), new CallUrlEvent("/"));

        managePersonsMenu.addOption("Liste des personnes", new CallUrlEvent("/persons/list"));
        managePersonsMenu.addOption("Ajouter une personne", new CallUrlEvent("/persons/add"));
        managePersonsMenu.addOption("Modifier une personne", new CallUrlEvent("/persons/modify/select"));

        MenuResponse menuResponse = managePersonsMenu.use();

        if (menuResponse.getResponse() instanceof Event event) {
            return event;
        } else {
            throw new UnhandledMenuResponseType();
        }
    }

}
