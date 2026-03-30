package utils.io.menus;

public class MenuOptionOutcomeLeadingAction extends MenuOptionOutcomeAction implements LeadableMenuOptionOutcome {

    // ─── Properties ─── //

    private MenuLeadTo menuLeadTo;

    // ─── Constructors ─── //

    public MenuOptionOutcomeLeadingAction(MenuLeadTo menuLeadTo) {
        super();
        this.menuLeadTo = menuLeadTo;
    }

    public MenuOptionOutcomeLeadingAction(Runnable action, MenuLeadTo menuLeadTo) {
        super(action);
        this.menuLeadTo = menuLeadTo;
    }

    // ─── Getters ─── //

    public MenuLeadTo getLeadingChoice() {
        return this.menuLeadTo;
    }

    // ─── Setters ─── //

    public void setLeadingChoice(MenuLeadTo menuLeadTo) {
        this.menuLeadTo = menuLeadTo;
    }

}
