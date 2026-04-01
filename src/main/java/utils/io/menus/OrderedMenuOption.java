package utils.io.menus;

public class OrderedMenuOption extends MenuOption {

    // ─── Properties ─── //

    private final int order;

    // ─── Constructors ─── //

    public OrderedMenuOption(int order, String text, Object response) {
        super(text, response);
        this.order = order;
    }

    // ─── Getters ─── //

    public int getOrder() {
        return order;
    }

}
