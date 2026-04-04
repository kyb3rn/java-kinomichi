package app.views.camps;

import app.events.CallUrlEvent;
import app.events.Event;
import app.views.View;
import utils.io.text_formatting.TextFormatter;
import utils.io.menus.MenuResponse;
import app.utils.menus.KinomichiStandardMenu;
import utils.io.menus.UnhandledMenuResponseType;

public class ManageCampView extends View {

    // ─── Properties ─── //

    private final int campId;

    // ─── Constructors ─── //

    public ManageCampView(int campId) {
        this.campId = campId;
    }

    // ─── Overrides & inheritance ─── //

    @Override
    public Event render() {
        KinomichiStandardMenu manageCampMenu = new KinomichiStandardMenu("Kinomichi - Gestion du stage " + TextFormatter.bold("#%s".formatted(this.campId)), new CallUrlEvent("/"));
        manageCampMenu.addUnoptionedRow("À venir !");

        MenuResponse menuResponse = manageCampMenu.use();

        if (menuResponse.getResponse() instanceof Event event) {
            return event;
        } else {
            throw new UnhandledMenuResponseType();
        }
    }

}
