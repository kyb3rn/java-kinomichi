package app.events;

public class EventException extends Exception {

    private final Event event;

    // ─── Constructors ─── //

    public EventException() {
        super();
        this.event = null;
    }

    public EventException(Event event) {
        super();
        this.event = event;
    }

    public EventException(String message) {
        super(message);
        this.event = null;
    }

    public EventException(Event event, String message) {
        super(message);
        this.event = event;
    }

    public EventException(Throwable cause) {
        super(cause);
        this.event = null;
    }

    public EventException(Event event, Throwable cause) {
        super(cause);
        this.event = event;
    }

    public EventException(String message, Throwable cause) {
        super(message, cause);
        this.event = null;
    }

    public EventException(Event event, String message, Throwable cause) {
        super(message, cause);
        this.event = event;
    }

    // ─── Getters ─── //

    public Event getEvent() {
        return this.event;
    }

}
