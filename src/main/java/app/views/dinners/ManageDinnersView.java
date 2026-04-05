package app.views.dinners;

import app.events.CallUrlEvent;
import app.events.Event;
import app.views.View;
import utils.io.text_formatting.TextFormatter;
import utils.io.menus.MenuResponse;
import app.utils.menus.KinomichiStandardMenu;
import utils.io.menus.UnhandledMenuResponseType;

public class ManageDinnersView extends View {

    // ─── Properties ─── //

    private final int campId;

    // ─── Constructors ─── //

    public ManageDinnersView(int campId) {
        this.campId = campId;
    }

    // ─── Overrides & inheritance ─── //

    @Override
    public Event render() {
        KinomichiStandardMenu manageDinnersMenu = new KinomichiStandardMenu("Kinomichi - Gestion des repas du stage " + TextFormatter.bold("#%s".formatted(this.campId)), new CallUrlEvent("/camps/manage/" + this.campId));
        manageDinnersMenu.addOption("Liste des repas", new CallUrlEvent("/camps/manage/%d/dinners/list".formatted(this.campId)));
        manageDinnersMenu.addOption("Ajouter un repas", new CallUrlEvent("/camps/manage/%d/dinners/add".formatted(this.campId)));
        manageDinnersMenu.addOption("Modifier un repas", new CallUrlEvent("/camps/manage/%d/dinners/modify/select".formatted(this.campId)));
        manageDinnersMenu.addOption("Supprimer un repas", new CallUrlEvent("/camps/manage/%d/dinners/delete/select".formatted(this.campId)));
        manageDinnersMenu.addSectionSeparationIndex();
        manageDinnersMenu.addOption("Gestion des réservations d'un repas", new CallUrlEvent("/camps/manage/%d/dinners/reservations/select".formatted(this.campId)));

        MenuResponse menuResponse = manageDinnersMenu.use();

        if (menuResponse.getResponse() instanceof Event event) {
            return event;
        } else {
            throw new UnhandledMenuResponseType();
        }
    }

}
