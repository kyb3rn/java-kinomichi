package utils.io.menus;

public class MenuOptionOutcomeLeading extends MenuOptionOutcome implements LeadableMenuOptionOutcome {

    private MenuLeadTo menuLeadTo;

    public MenuOptionOutcomeLeading(MenuLeadTo menuLeadTo) {
        this.menuLeadTo = menuLeadTo;
    }

    public MenuLeadTo getLeadingChoice() {
        return menuLeadTo;
    }

    public void setLeadingChoice(MenuLeadTo menuLeadTo) {
        this.menuLeadTo = menuLeadTo;
    }

}
