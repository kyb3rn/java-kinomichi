package utils.io.helpers.texts.formatting;

public enum TextBackgroundColor {
    NONE(null),
    BLACK("40"),
    RED("41"),
    GREEN("42"),
    YELLOW("43"),
    BLUE("44"),
    MAGENTA("45"),
    CYAN("46"),
    WHITE("47"),
    BRIGHT_BLACK("100"),
    BRIGHT_RED("101"),
    BRIGHT_GREEN("102"),
    BRIGHT_YELLOW("103"),
    BRIGHT_BLUE("104"),
    BRIGHT_MAGENTA("105"),
    BRIGHT_CYAN("106"),
    BRIGHT_WHITE("107");

    // ─── Properties ─── //

    public static final String RESET_CODE = "49";

    private final String code;

    // ─── Constructors ─── //

    TextBackgroundColor(String code) {
        this.code = code;
    }

    // ─── Getters ─── //

    public String getCode() {
        return this.code;
    }

}
