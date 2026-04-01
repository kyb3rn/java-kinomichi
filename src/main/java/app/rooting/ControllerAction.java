package app.rooting;

import app.events.Event;

@FunctionalInterface
public interface ControllerAction {

    Event execute(Request request);

}
