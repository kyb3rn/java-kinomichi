package utils.io.menus;

public class MenuResponse {

    // ─── Properties ─── //

    private final Object response;

    // ─── Constructors ─── //

    public MenuResponse() {
        this.response = null;
    }

    public MenuResponse(MenuResponse menuResponse) {
        this.response = menuResponse.response;
    }

    public MenuResponse(Object response) {
        this.response = response;
    }

    // ─── Getters ─── //

    public Object getResponse() {
        return this.response;
    }

}
