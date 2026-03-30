package utils.io.menus;

public class MenuOptionOutcomeAction extends MenuOptionOutcome {

    // ─── Properties ─── //

    private final Runnable action;

    // ─── Constructors ─── //

    public MenuOptionOutcomeAction() {
        this.action = null;
    }

    public MenuOptionOutcomeAction(Runnable action) {
        this.action = action;
    }

    // ─── Utility methods ─── //

    public void execute() {
        if (this.action != null) {
            this.action.run();
        }
    }

}
