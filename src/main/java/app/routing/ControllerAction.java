package app.routing;

import app.events.Event;

@FunctionalInterface
public interface ControllerAction {

    Event execute(Request request);

}
