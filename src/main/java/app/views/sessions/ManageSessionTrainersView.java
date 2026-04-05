package app.views.sessions;

import app.events.CallUrlEvent;
import app.events.Event;
import app.views.View;
import utils.io.text_formatting.TextFormatter;
import utils.io.menus.MenuResponse;
import app.utils.menus.KinomichiStandardMenu;
import utils.io.menus.UnhandledMenuResponseType;

public class ManageSessionTrainersView extends View {

    // ─── Properties ─── //

    private final int campId;
    private final int sessionId;

    // ─── Constructors ─── //

    public ManageSessionTrainersView(int campId, int sessionId) {
        this.campId = campId;
        this.sessionId = sessionId;
    }

    // ─── Overrides & inheritance ─── //

    @Override
    public Event render() {
        KinomichiStandardMenu manageSessionTrainersMenu = new KinomichiStandardMenu(
                "Gestion des formateurs de la session " + TextFormatter.bold("#%d".formatted(this.sessionId)) + " du stage " + TextFormatter.bold("#%d".formatted(this.campId)),
                new CallUrlEvent("/camps/manage/%d/sessions".formatted(this.campId))
        );

        manageSessionTrainersMenu.addOption("Liste des formateurs", new CallUrlEvent("/camps/manage/%d/sessions/%d/trainers/list".formatted(this.campId, this.sessionId)));
        manageSessionTrainersMenu.addOption("Ajouter un formateur", new CallUrlEvent("/camps/manage/%d/sessions/%d/trainers/add".formatted(this.campId, this.sessionId)));
        manageSessionTrainersMenu.addOption("Retirer un formateur", new CallUrlEvent("/camps/manage/%d/sessions/%d/trainers/delete".formatted(this.campId, this.sessionId)));

        MenuResponse menuResponse = manageSessionTrainersMenu.use();

        if (menuResponse.getResponse() instanceof Event event) {
            return event;
        } else {
            throw new UnhandledMenuResponseType();
        }
    }

}
