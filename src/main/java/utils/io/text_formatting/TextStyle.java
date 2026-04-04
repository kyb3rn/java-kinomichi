package utils.io.text_formatting;

public enum TextStyle {
    BOLD("1", "22"),
    DIM("2", "22"),
    ITALIC("3", "23"),
    UNDERLINE("4", "24"),
    BLINK("5", "25"),
    REVERSE("7", "27"),
    HIDDEN("8", "28"),
    STRIKETHROUGH("9", "29");

    // ─── Properties ─── //

    private final String code;
    private final String resetCode;

    // ─── Constructors ─── //

    TextStyle(String code, String resetCode) {
        this.code = code;
        this.resetCode = resetCode;
    }

    // ─── Getters ─── //

    public String getCode() {
        return this.code;
    }

    public String getResetCode() {
        return this.resetCode;
    }
}
