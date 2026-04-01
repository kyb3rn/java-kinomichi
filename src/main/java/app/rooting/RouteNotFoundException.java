package app.rooting;

public class RouteNotFoundException extends Exception {

    // ─── Constructors ─── //

    public RouteNotFoundException() {
        super();
    }

    public RouteNotFoundException(String message) {
        super(message);
    }

    public RouteNotFoundException(Throwable cause) {
        super(cause);
    }

    public RouteNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

}
