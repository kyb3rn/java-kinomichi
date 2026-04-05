package app.views.lodgings;

import app.events.CallUrlEvent;
import app.events.Event;
import app.views.View;
import utils.io.text_formatting.TextFormatter;
import utils.io.menus.MenuResponse;
import app.utils.menus.KinomichiStandardMenu;
import utils.io.menus.UnhandledMenuResponseType;

public class ManageLodgingReservationsView extends View {

    // ─── Properties ─── //

    private final int campId;
    private final int lodgingId;

    // ─── Constructors ─── //

    public ManageLodgingReservationsView(int campId, int lodgingId) {
        this.campId = campId;
        this.lodgingId = lodgingId;
    }

    // ─── Overrides & inheritance ─── //

    @Override
    public Event render() {
        KinomichiStandardMenu manageLodgingReservationsMenu = new KinomichiStandardMenu(
                "Gestion des réservations de l'hébergement " + TextFormatter.bold("#%d".formatted(this.lodgingId)) + " du stage " + TextFormatter.bold("#%d".formatted(this.campId)),
                new CallUrlEvent("/camps/manage/%d/lodgings".formatted(this.campId))
        );

        manageLodgingReservationsMenu.addOption("Liste des réservations", new CallUrlEvent("/camps/manage/%d/lodgings/%d/reservations/list".formatted(this.campId, this.lodgingId)));
        manageLodgingReservationsMenu.addOption("Ajouter une réservation", new CallUrlEvent("/camps/manage/%d/lodgings/%d/reservations/add".formatted(this.campId, this.lodgingId)));
        manageLodgingReservationsMenu.addOption("Supprimer une réservation", new CallUrlEvent("/camps/manage/%d/lodgings/%d/reservations/delete".formatted(this.campId, this.lodgingId)));

        MenuResponse menuResponse = manageLodgingReservationsMenu.use();

        if (menuResponse.getResponse() instanceof Event event) {
            return event;
        } else {
            throw new UnhandledMenuResponseType();
        }
    }

}
