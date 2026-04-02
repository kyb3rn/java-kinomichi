package app.events;

public class FormResultEvent<T> extends Event {

    // ─── Properties ─── //

    private final T result;

    // ─── Constructors ─── //

    public FormResultEvent(T result) {
        this.result = result;
    }

    // ─── Getters ─── //

    public T getResult() {
        return this.result;
    }

}
