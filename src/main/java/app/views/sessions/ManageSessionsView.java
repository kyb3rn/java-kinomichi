package app.views.sessions;

import app.events.CallUrlEvent;
import app.events.Event;
import app.views.View;
import utils.io.text_formatting.TextFormatter;
import utils.io.menus.MenuResponse;
import app.utils.menus.KinomichiStandardMenu;
import utils.io.menus.UnhandledMenuResponseType;

public class ManageSessionsView extends View {

    // ─── Properties ─── //

    private final int campId;

    // ─── Constructors ─── //

    public ManageSessionsView(int campId) {
        this.campId = campId;
    }

    // ─── Overrides & inheritance ─── //

    @Override
    public Event render() {
        KinomichiStandardMenu manageSessionsMenu = new KinomichiStandardMenu("Kinomichi - Gestion des sessions du stage " + TextFormatter.bold("#%s".formatted(this.campId)), new CallUrlEvent("/camps/manage/" + this.campId));
        manageSessionsMenu.addOption("Liste des sessions", new CallUrlEvent("/camps/manage/%d/sessions/list".formatted(this.campId)));
        manageSessionsMenu.addOption("Ajouter une session", new CallUrlEvent("/camps/manage/%d/sessions/add".formatted(this.campId)));
        manageSessionsMenu.addOption("Modifier une session", new CallUrlEvent("/camps/manage/%d/sessions/modify/select".formatted(this.campId)));
        manageSessionsMenu.addOption("Supprimer une session", new CallUrlEvent("/camps/manage/%d/sessions/delete/select".formatted(this.campId)));
        manageSessionsMenu.addSectionSeparationIndex();
        manageSessionsMenu.addOption("Gestion des formateurs d'une session", new CallUrlEvent("/camps/manage/%d/sessions/trainers/select".formatted(this.campId)));
        manageSessionsMenu.addOption("Gestion des inscriptions à une session", new CallUrlEvent("/camps/manage/%d/sessions/registrations/select".formatted(this.campId)));

        MenuResponse menuResponse = manageSessionsMenu.use();

        if (menuResponse.getResponse() instanceof Event event) {
            return event;
        } else {
            throw new UnhandledMenuResponseType();
        }
    }

}
