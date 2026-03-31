package utils.io.menus;

import app.middlewares.Middleware;

import java.util.ArrayList;

public abstract class MenuStage {

    // ─── Properties ─── //

    protected ArrayList<Middleware> middlewares = new ArrayList<>();

    // ─── Utility methods ─── //

    public abstract MenuLeadTo use();

}
