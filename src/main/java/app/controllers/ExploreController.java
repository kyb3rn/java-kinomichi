package app.controllers;

import app.events.Event;
import app.routing.Request;
import app.views.explore.ExploreDataView;

public class ExploreController extends Controller {

    // ─── Utility methods ─── //

    public Event index(Request request) {
        ExploreDataView exploreDataView = new ExploreDataView();
        return exploreDataView.render();
    }

}
