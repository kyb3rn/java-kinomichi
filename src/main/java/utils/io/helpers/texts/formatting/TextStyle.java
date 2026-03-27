package utils.io.helpers.texts.formatting;

public enum TextStyle {
    BOLD("1"),
    DIM("2"),
    ITALIC("3"),
    UNDERLINE("4"),
    BLINK("5"),
    REVERSE("7"),
    HIDDEN("8"),
    STRIKETHROUGH("9");

    /** Properties **/

    private final String code;

    /** Constructors **/

    TextStyle(String code) {
        this.code = code;
    }

    /** Getters **/

    public String getCode() {
        return this.code;
    }
}
