package utils.io.menus;

public class MenuOption {

    // ─── Properties ─── //

    private String text;
    private Object response;

    // ─── Constructors ─── //

    public MenuOption(String text, Object response) {
        this.setText(text);
        this.response = response;
    }

    // ─── Getters ─── //

    public String getText() {
        return this.text;
    }

    public Object getResponse() {
        return this.response;
    }

    // ─── Setters ─── //

    public void setText(String text) {
        if (text.isBlank()) {
            throw new IllegalArgumentException("Le texte d'une option dans un menu ne peut pas être vide");
        }

        this.text = text;
    }

    public void setResponse(Object response) {
        this.response = response;
    }

}
