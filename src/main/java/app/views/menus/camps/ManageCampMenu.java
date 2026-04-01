package app.views.menus.camps;

import app.AppState;
import app.middlewares.IsACampSelectedMiddleware;
import utils.io.helpers.texts.formatting.TextFormatter;
import utils.io.menus.StandardMenu;

public class ManageCampMenu extends StandardMenu {

    // ─── Constructors ─── //

    public ManageCampMenu() {
        this.middlewares.add(new IsACampSelectedMiddleware());

        int campId = AppState.selectedCampId;

        this.setTitle("Kinomichi - Gestion du stage " + TextFormatter.bold("#%s".formatted(campId)));
        this.addUnoptionedRow("À venir !");
    }

}
