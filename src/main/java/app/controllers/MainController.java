package app.controllers;

import app.events.Event;
import app.routing.Request;
import app.views.main.MainView;

public class MainController extends Controller {

    // ─── Utility methods ─── //

    public Event index(Request request) {
        MainView mainView = new MainView();
        return mainView.render();
    }

}
