package utils.io.menus;

public class MenuOption {

    // ─── Properties ─── //

    private String text;
    private String leadTo;

    // ─── Constructors ─── //

    public MenuOption(String text, String leadTo) {
        this.setText(text);
        this.setLeadTo(leadTo);
    }

    // ─── Getters ─── //

    public String getText() {
        return this.text;
    }

    public String getLeadTo() {
        return this.leadTo;
    }

    // ─── Setters ─── //

    public void setText(String text) {
        if (text.isBlank()) {
            throw new IllegalArgumentException("Le texte d'une option dans un menu ne peut pas être vide");
        }

        this.text = text;
    }

    public void setLeadTo(String leadTo) {
        if (leadTo == null) {
            this.leadTo = null;
        } else {
            if (leadTo.isBlank()) {
                throw new IllegalArgumentException("Le point suivant d'une option dans un menu ne peut pas être vide");
            }

            this.leadTo = leadTo;
        }
    }

}
