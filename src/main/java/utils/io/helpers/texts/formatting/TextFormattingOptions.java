package utils.io.helpers.texts.formatting;

import java.util.*;
import java.util.stream.*;

public class TextFormattingOptions {

    /** Properties **/

    private TextColor color;
    private TextBackgroundColor backgroundColor;
    private final EnumSet<TextStyle> styles;

    /** Constructors **/

    public TextFormattingOptions() {
        this.color = null;
        this.backgroundColor = null;
        this.styles = EnumSet.noneOf(TextStyle.class);
    }

    /** Getters **/

    public TextColor getColor() {
        return this.color;
    }

    public TextBackgroundColor getBackgroundColor() {
        return this.backgroundColor;
    }

    public EnumSet<TextStyle> getStyles() {
        return this.styles;
    }

    /** Setters **/

    public TextFormattingOptions setColor(TextColor color) {
        this.color = color;
        return this;
    }

    public TextFormattingOptions setBackgroundColor(TextBackgroundColor backgroundColor) {
        this.backgroundColor = backgroundColor;
        return this;
    }

    /** Special methods **/

    public TextFormattingOptions addStyle(TextStyle style) {
        this.styles.add(style);
        return this;
    }

    public TextFormattingOptions removeStyle(TextStyle style) {
        this.styles.remove(style);
        return this;
    }

    public String buildEscapeSequence() {
        List<String> codes = new ArrayList<>();

        if (this.color != null) {
            codes.add(this.color.getCode());
        }

        if (this.backgroundColor != null) {
            codes.add(this.backgroundColor.getCode());
        }

        for (TextStyle style : this.styles) {
            codes.add(style.getCode());
        }

        if (codes.isEmpty()) {
            return "";
        }

        return "\033[" + codes.stream().collect(Collectors.joining(";")) + "m";
    }
}
