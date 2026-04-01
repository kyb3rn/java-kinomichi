package app.middlewares;

import app.AppState;
import utils.io.menus.MenuResponse;

public class IsACampSelectedMiddleware extends Middleware {

    @Override
    public MenuResponse verify() {
        return AppState.selectedCampId != -1 ? null : new MenuResponse("main");
    }

}
