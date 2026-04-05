package app.views.sessions;

import app.events.CallUrlEvent;
import app.events.Event;
import app.views.View;
import utils.io.text_formatting.TextFormatter;
import utils.io.menus.MenuResponse;
import app.utils.menus.KinomichiStandardMenu;
import utils.io.menus.UnhandledMenuResponseType;

public class ManageSessionRegistrationsView extends View {

    // ─── Properties ─── //

    private final int campId;
    private final int sessionId;

    // ─── Constructors ─── //

    public ManageSessionRegistrationsView(int campId, int sessionId) {
        this.campId = campId;
        this.sessionId = sessionId;
    }

    // ─── Overrides & inheritance ─── //

    @Override
    public Event render() {
        KinomichiStandardMenu manageSessionRegistrationsMenu = new KinomichiStandardMenu(
                "Gestion des inscriptions à la session " + TextFormatter.bold("#%d".formatted(this.sessionId)) + " du stage " + TextFormatter.bold("#%d".formatted(this.campId)),
                new CallUrlEvent("/camps/manage/%d/sessions".formatted(this.campId))
        );

        manageSessionRegistrationsMenu.addOption("Liste des inscriptions", new CallUrlEvent("/camps/manage/%d/sessions/%d/registrations/list".formatted(this.campId, this.sessionId)));
        manageSessionRegistrationsMenu.addOption("Ajouter une inscription", new CallUrlEvent("/camps/manage/%d/sessions/%d/registrations/add".formatted(this.campId, this.sessionId)));
        manageSessionRegistrationsMenu.addOption("Supprimer une inscription", new CallUrlEvent("/camps/manage/%d/sessions/%d/registrations/delete".formatted(this.campId, this.sessionId)));

        MenuResponse menuResponse = manageSessionRegistrationsMenu.use();

        if (menuResponse.getResponse() instanceof Event event) {
            return event;
        } else {
            throw new UnhandledMenuResponseType();
        }
    }

}
