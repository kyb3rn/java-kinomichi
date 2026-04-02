package app.views.clubs;

import app.events.CallUrlEvent;
import app.events.Event;
import app.views.View;
import utils.io.menus.MenuResponse;
import app.utils.menus.KinomichiStandardMenu;
import utils.io.menus.UnhandledMenuResponseType;

public class ClubsDashboardView extends View {

    // ─── Properties ─── //

    private final int clubCount;
    private final boolean hasUnsavedChanges;

    // ─── Constructors ─── //

    public ClubsDashboardView(int clubCount, boolean hasUnsavedChanges) {
        this.clubCount = clubCount;
        this.hasUnsavedChanges = hasUnsavedChanges;
    }

    // ─── Overrides & inheritance ─── //

    @Override
    public Event render() {
        String unsavedIcon = this.hasUnsavedChanges ? " (!)" : "";

        KinomichiStandardMenu manageClubsMenu = new KinomichiStandardMenu("Kinomichi - Gestion des clubs (%d)%s".formatted(this.clubCount, unsavedIcon), new CallUrlEvent("/"));

        manageClubsMenu.addOption("Liste des clubs", new CallUrlEvent("/clubs/list"));
        manageClubsMenu.addOption("Ajouter un club", new CallUrlEvent("/clubs/add"));

        MenuResponse menuResponse = manageClubsMenu.use();

        if (menuResponse.getResponse() instanceof Event event) {
            return event;
        } else {
            throw new UnhandledMenuResponseType();
        }
    }

}
