package utils.io.menus;

public class MenuOption {

    // ─── Properties ─── //

    private String text;
    private MenuOptionOutcome outcome;

    // ─── Constructors ─── //

    public MenuOption(String text, MenuOptionOutcome outcome) {
        this.setText(text);
        this.setOutcome(outcome);
    }

    // ─── Getters ─── //

    public String getText() {
        return this.text;
    }

    public MenuOptionOutcome getOutcome() {
        return this.outcome;
    }

    // ─── Setters ─── //

    public void setText(String text) {
        if (text.isBlank()) {
            throw new IllegalArgumentException("Le texte d'une option dans un menu ne peut pas être vide");
        }

        this.text = text;
    }

    public void setOutcome(MenuOptionOutcome outcome) {
        this.outcome = outcome;
    }

}
