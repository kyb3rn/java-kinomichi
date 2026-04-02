package utils.io.helpers.texts.formatting;

import java.util.*;
import java.util.stream.*;
import java.util.LinkedHashSet;

public class TextFormattingOptions {

    // ─── Properties ─── //

    private TextColor color;
    private TextBackgroundColor backgroundColor;
    private final EnumSet<TextStyle> styles;
    private TextAlignment alignment;
    private int minWidth;

    // ─── Constructors ─── //

    public TextFormattingOptions() {
        this.color = null;
        this.backgroundColor = null;
        this.styles = EnumSet.noneOf(TextStyle.class);
        this.alignment = TextAlignment.LEFT;
        this.minWidth = 0;
    }

    public TextFormattingOptions(TextFormattingOptions other) {
        this.color = other.color;
        this.backgroundColor = other.backgroundColor;
        this.styles = EnumSet.noneOf(TextStyle.class);
        this.styles.addAll(other.styles);
        this.alignment = other.alignment;
        this.minWidth = other.minWidth;
    }

    // ─── Getters ─── //

    public TextColor getColor() {
        return this.color;
    }

    public TextBackgroundColor getBackgroundColor() {
        return this.backgroundColor;
    }

    public EnumSet<TextStyle> getStyles() {
        return this.styles;
    }

    public TextAlignment getAlignment() {
        return this.alignment;
    }

    public int getMinWidth() {
        return this.minWidth;
    }

    // ─── Setters ─── //

    public TextFormattingOptions setColor(TextColor color) {
        this.color = color;
        return this;
    }

    public TextFormattingOptions setBackgroundColor(TextBackgroundColor backgroundColor) {
        this.backgroundColor = backgroundColor;
        return this;
    }

    public TextFormattingOptions setAlignment(TextAlignment alignment) {
        this.alignment = alignment;
        return this;
    }

    public TextFormattingOptions setMinWidth(int minWidth) {
        if (minWidth < 0) {
            throw new IllegalArgumentException("La longueur minimale d'un texte doit être un entier positif");
        }

        this.minWidth = minWidth;
        return this;
    }

    // ─── Utility methods ─── //

    public TextFormattingOptions addStyle(TextStyle style) {
        this.styles.add(style);
        return this;
    }

    public TextFormattingOptions removeStyle(TextStyle style) {
        this.styles.remove(style);
        return this;
    }

    public TextFormattingOptions mergeWith(TextFormattingOptions outer) {
        if ((this.color == null || this.color == TextColor.NONE) && outer.color != null && outer.color != TextColor.NONE) {
            this.color = outer.color;
        }

        if ((this.backgroundColor == null || this.backgroundColor == TextBackgroundColor.NONE) && outer.backgroundColor != null && outer.backgroundColor != TextBackgroundColor.NONE) {
            this.backgroundColor = outer.backgroundColor;
        }

        this.styles.addAll(outer.styles);

        return this;
    }

    public String buildEscapeSequence() {
        List<String> codes = new ArrayList<>();

        if (this.color != null && this.color != TextColor.NONE) {
            codes.add(this.color.getCode());
        }

        if (this.backgroundColor != null && this.backgroundColor != TextBackgroundColor.NONE) {
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

    public String buildResetSequence() {
        Set<String> resetCodes = new LinkedHashSet<>();

        if (this.color != null && this.color != TextColor.NONE) {
            resetCodes.add(TextColor.RESET_CODE);
        }

        if (this.backgroundColor != null && this.backgroundColor != TextBackgroundColor.NONE) {
            resetCodes.add(TextBackgroundColor.RESET_CODE);
        }

        for (TextStyle style : this.styles) {
            resetCodes.add(style.getResetCode());
        }

        if (resetCodes.isEmpty()) {
            return "";
        }

        return "\033[" + String.join(";", resetCodes) + "m";
    }
}
