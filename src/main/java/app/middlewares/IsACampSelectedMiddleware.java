package app.middlewares;

import app.AppState;
import utils.io.menus.MenuLeadTo;

public class IsACampSelectedMiddleware extends Middleware {

    @Override
    public MenuLeadTo verify() {
        return AppState.selectedCampId != -1 ? null : new MenuLeadTo("main");
    }

}
