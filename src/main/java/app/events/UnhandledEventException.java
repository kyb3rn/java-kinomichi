package app.events;

public class UnhandledEventException extends EventException {

    // ─── Constructors ─── //

    public UnhandledEventException(Event event) {
        if (event == null) {
            throw new IllegalArgumentException("Un event est requis pour former cette exception");
        }

        super(event);
    }

    public UnhandledEventException(Event event, String message) {
        if (event == null) {
            throw new IllegalArgumentException("Un event est requis pour former cette exception");
        }

        super(event, message);
    }

    public UnhandledEventException(Event event, Throwable cause) {
        if (event == null) {
            throw new IllegalArgumentException("Un event est requis pour former cette exception");
        }

        super(event, cause);
    }

    public UnhandledEventException(Event event, String message, Throwable cause) {
        if (event == null) {
            throw new IllegalArgumentException("Un event est requis pour former cette exception");
        }

        super(event, message, cause);
    }

}
