package app.views.invitations;

import app.events.CallUrlEvent;
import app.events.Event;
import app.views.View;
import utils.io.text_formatting.TextFormatter;
import utils.io.menus.MenuResponse;
import app.utils.menus.KinomichiStandardMenu;
import utils.io.menus.UnhandledMenuResponseType;

public class ManageInvitationsView extends View {

    // ─── Properties ─── //

    private final int campId;

    // ─── Constructors ─── //

    public ManageInvitationsView(int campId) {
        this.campId = campId;
    }

    // ─── Overrides & inheritance ─── //

    @Override
    public Event render() {
        KinomichiStandardMenu manageInvitationsMenu = new KinomichiStandardMenu("Kinomichi - Gestion des invitations du stage " + TextFormatter.bold("#%s".formatted(this.campId)), new CallUrlEvent("/camps/manage/" + this.campId));
        manageInvitationsMenu.addOption("Liste des invitations", new CallUrlEvent("/camps/manage/%d/invitations/list".formatted(this.campId)));
        manageInvitationsMenu.addOption("Ajouter une invitation", new CallUrlEvent("/camps/manage/%d/invitations/add".formatted(this.campId)));
        manageInvitationsMenu.addOption("Supprimer une invitation", new CallUrlEvent("/camps/manage/%d/invitations/delete".formatted(this.campId)));

        MenuResponse menuResponse = manageInvitationsMenu.use();

        if (menuResponse.getResponse() instanceof Event event) {
            return event;
        } else {
            throw new UnhandledMenuResponseType();
        }
    }

}
