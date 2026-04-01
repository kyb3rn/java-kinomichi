package utils.io.menus;

public class HookInterruptException extends RuntimeException {

    // ─── Properties ─── //

    private final MenuResponse menuResponse;

    // ─── Constructors ─── //

    public HookInterruptException(MenuResponse menuResponse) {
        this.menuResponse = menuResponse;
    }

    // ─── Getters ─── //

    public MenuResponse getMenuResponse() {
        return this.menuResponse;
    }

}
