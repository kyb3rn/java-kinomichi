package app.views.lodgings;

import app.events.CallUrlEvent;
import app.events.Event;
import app.views.View;
import utils.io.text_formatting.TextFormatter;
import utils.io.menus.MenuResponse;
import app.utils.menus.KinomichiStandardMenu;
import utils.io.menus.UnhandledMenuResponseType;

public class ManageLodgingsView extends View {

    // ─── Properties ─── //

    private final int campId;

    // ─── Constructors ─── //

    public ManageLodgingsView(int campId) {
        this.campId = campId;
    }

    // ─── Overrides & inheritance ─── //

    @Override
    public Event render() {
        KinomichiStandardMenu manageLodgingsMenu = new KinomichiStandardMenu("Kinomichi - Gestion des hébergements du stage " + TextFormatter.bold("#%s".formatted(this.campId)), new CallUrlEvent("/camps/manage/" + this.campId));
        manageLodgingsMenu.addOption("Liste des hébergements", new CallUrlEvent("/camps/manage/%d/lodgings/list".formatted(this.campId)));
        manageLodgingsMenu.addOption("Ajouter un hébergement", new CallUrlEvent("/camps/manage/%d/lodgings/add".formatted(this.campId)));
        manageLodgingsMenu.addOption("Modifier un hébergement", new CallUrlEvent("/camps/manage/%d/lodgings/modify/select".formatted(this.campId)));
        manageLodgingsMenu.addOption("Supprimer un hébergement", new CallUrlEvent("/camps/manage/%d/lodgings/delete/select".formatted(this.campId)));
        manageLodgingsMenu.addSectionSeparationIndex();
        manageLodgingsMenu.addOption("Gestion des réservations d'un hébergement", new CallUrlEvent("/camps/manage/%d/lodgings/reservations/select".formatted(this.campId)));

        MenuResponse menuResponse = manageLodgingsMenu.use();

        if (menuResponse.getResponse() instanceof Event event) {
            return event;
        } else {
            throw new UnhandledMenuResponseType();
        }
    }

}
