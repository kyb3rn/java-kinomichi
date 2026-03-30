package utils.io.menus;

public class MenuOptionOutcomeLeading extends MenuOptionOutcome implements LeadableMenuOptionOutcome {

    // ─── Properties ─── //

    private MenuLeadTo menuLeadTo;

    // ─── Constructors ─── //

    public MenuOptionOutcomeLeading(MenuLeadTo menuLeadTo) {
        this.menuLeadTo = menuLeadTo;
    }

    // ─── Overrides & inheritance ─── //

    public MenuLeadTo getLeadingChoice() {
        return this.menuLeadTo;
    }

    public void setLeadingChoice(MenuLeadTo menuLeadTo) {
        this.menuLeadTo = menuLeadTo;
    }

}
