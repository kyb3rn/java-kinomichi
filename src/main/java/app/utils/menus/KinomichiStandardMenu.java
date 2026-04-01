package app.utils.menus;

import app.middlewares.Middleware;
import utils.io.helpers.Functions;
import utils.io.menus.MenuResponse;
import utils.io.menus.StandardMenu;

public class KinomichiStandardMenu extends StandardMenu {

    @Override
    public MenuResponse beforeUse() {
        for (Middleware middleware : this.middlewares) {
            MenuResponse menuResponse = middleware.verify();
            if (menuResponse != null) {
                System.out.println(Functions.styleAsErrorMessage("Ce menu n'est pas accessible."));
                return menuResponse;
            }
        }

        return null;
    }

}
