package app.views;

public abstract class ModelView extends View {

    // ─── Properties ─── //

    private final String errorBackUrl;

    // ─── Constructors ─── //

    public ModelView(String errorBackUrl) {
        this.errorBackUrl = errorBackUrl;
    }

    // ─── Getters ─── //

    public String getErrorBackUrl() {
        return errorBackUrl;
    }

}
