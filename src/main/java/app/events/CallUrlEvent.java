package app.events;

public class CallUrlEvent extends Event {

    // ─── Properties ─── //

    private final String url;

    // ─── Constructors ─── //

    public CallUrlEvent(String url) {
        this.url = url;
    }

    // ─── Getters ─── //

    public String getUrl() {
        return this.url;
    }

}
