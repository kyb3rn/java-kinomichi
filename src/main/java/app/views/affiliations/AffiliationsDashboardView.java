package app.views.affiliations;

import app.events.CallUrlEvent;
import app.events.Event;
import app.views.View;
import utils.io.menus.MenuResponse;
import app.utils.menus.KinomichiStandardMenu;
import utils.io.menus.UnhandledMenuResponseType;

public class AffiliationsDashboardView extends View {

    // ─── Properties ─── //

    private final int affiliationCount;
    private final boolean hasUnsavedChanges;

    // ─── Constructors ─── //

    public AffiliationsDashboardView(int affiliationCount, boolean hasUnsavedChanges) {
        this.affiliationCount = affiliationCount;
        this.hasUnsavedChanges = hasUnsavedChanges;
    }

    // ─── Overrides & inheritance ─── //

    @Override
    public Event render() {
        String unsavedIcon = this.hasUnsavedChanges ? " (!)" : "";

        KinomichiStandardMenu manageAffiliationsMenu = new KinomichiStandardMenu("Kinomichi - Gestion des affiliations (%d)%s".formatted(this.affiliationCount, unsavedIcon), new CallUrlEvent("/"));

        manageAffiliationsMenu.addOption("Liste des affiliations", new CallUrlEvent("/affiliations/list"));
        manageAffiliationsMenu.addOption("Ajouter une affiliation", new CallUrlEvent("/affiliations/add"));
        manageAffiliationsMenu.addOption("Modifier une affiliation", new CallUrlEvent("/affiliations/modify/select"));
        manageAffiliationsMenu.addOption("Supprimer une affiliation", new CallUrlEvent("/affiliations/delete/select"));

        MenuResponse menuResponse = manageAffiliationsMenu.use();

        if (menuResponse.getResponse() instanceof Event event) {
            return event;
        } else {
            throw new UnhandledMenuResponseType();
        }
    }

}
