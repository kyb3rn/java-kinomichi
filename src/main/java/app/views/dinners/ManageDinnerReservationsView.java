package app.views.dinners;

import app.events.CallUrlEvent;
import app.events.Event;
import app.views.View;
import utils.io.text_formatting.TextFormatter;
import utils.io.menus.MenuResponse;
import app.utils.menus.KinomichiStandardMenu;
import utils.io.menus.UnhandledMenuResponseType;

public class ManageDinnerReservationsView extends View {

    // ─── Properties ─── //

    private final int campId;
    private final int dinnerId;

    // ─── Constructors ─── //

    public ManageDinnerReservationsView(int campId, int dinnerId) {
        this.campId = campId;
        this.dinnerId = dinnerId;
    }

    // ─── Overrides & inheritance ─── //

    @Override
    public Event render() {
        KinomichiStandardMenu manageDinnerReservationsMenu = new KinomichiStandardMenu(
                "Gestion des réservations de repas " + TextFormatter.bold("#%d".formatted(this.dinnerId)) + " du stage " + TextFormatter.bold("#%d".formatted(this.campId)),
                new CallUrlEvent("/camps/manage/%d/dinners".formatted(this.campId))
        );

        manageDinnerReservationsMenu.addOption("Liste des réservations", new CallUrlEvent("/camps/manage/%d/dinners/%d/reservations/list".formatted(this.campId, this.dinnerId)));
        manageDinnerReservationsMenu.addOption("Ajouter une réservation", new CallUrlEvent("/camps/manage/%d/dinners/%d/reservations/add".formatted(this.campId, this.dinnerId)));
        manageDinnerReservationsMenu.addOption("Supprimer une réservation", new CallUrlEvent("/camps/manage/%d/dinners/%d/reservations/delete".formatted(this.campId, this.dinnerId)));

        MenuResponse menuResponse = manageDinnerReservationsMenu.use();

        if (menuResponse.getResponse() instanceof Event event) {
            return event;
        } else {
            throw new UnhandledMenuResponseType();
        }
    }

}
