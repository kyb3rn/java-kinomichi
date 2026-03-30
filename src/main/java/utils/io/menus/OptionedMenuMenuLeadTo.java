package utils.io.menus;

public class OptionedMenuMenuLeadTo extends MenuLeadTo {

    // ─── Properties ─── //

    private final int choice;

    // ─── Constructors ─── //

    public OptionedMenuMenuLeadTo(int choice) {
        super();
        this.choice = choice;
    }

    public OptionedMenuMenuLeadTo(int choice, String leadTo) {
        super(leadTo);
        this.choice = choice;
    }

    public OptionedMenuMenuLeadTo(int choice, MenuLeadTo menuLeadTo) {
        super(menuLeadTo);
        this.choice = choice;
    }

    // ─── Getters ─── //

    public int getChoice() {
        return this.choice;
    }

}
