package app.controllers;

import app.events.Event;
import app.rooting.Request;
import app.views.MainView;

public class MainController extends Controller {

    // ─── Utility methods ─── //

    public Event index(Request request) {
        MainView mainView = new MainView();
        return mainView.render();
    }

}
