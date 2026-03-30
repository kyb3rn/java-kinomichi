package utils.io.helpers.texts.formatting;

import utils.io.helpers.Functions;

public class TextFormatter {

    // ─── Properties ─── //

    private static final String ESCAPE = "\033[";
    private static final String RESET = ESCAPE + "0m";

    // ─── Utility methods ─── //

    // Text colors

    public static String black(String text) {
        return ESCAPE + TextColor.BLACK.getCode() + "m" + text + RESET;
    }

    public static String red(String text) {
        return ESCAPE + TextColor.RED.getCode() + "m" + text + RESET;
    }

    public static String green(String text) {
        return ESCAPE + TextColor.GREEN.getCode() + "m" + text + RESET;
    }

    public static String yellow(String text) {
        return ESCAPE + TextColor.YELLOW.getCode() + "m" + text + RESET;
    }

    public static String blue(String text) {
        return ESCAPE + TextColor.BLUE.getCode() + "m" + text + RESET;
    }

    public static String magenta(String text) {
        return ESCAPE + TextColor.MAGENTA.getCode() + "m" + text + RESET;
    }

    public static String cyan(String text) {
        return ESCAPE + TextColor.CYAN.getCode() + "m" + text + RESET;
    }

    public static String white(String text) {
        return ESCAPE + TextColor.WHITE.getCode() + "m" + text + RESET;
    }

    public static String brightBlack(String text) {
        return ESCAPE + TextColor.BRIGHT_BLACK.getCode() + "m" + text + RESET;
    }

    public static String brightRed(String text) {
        return ESCAPE + TextColor.BRIGHT_RED.getCode() + "m" + text + RESET;
    }

    public static String brightGreen(String text) {
        return ESCAPE + TextColor.BRIGHT_GREEN.getCode() + "m" + text + RESET;
    }

    public static String brightYellow(String text) {
        return ESCAPE + TextColor.BRIGHT_YELLOW.getCode() + "m" + text + RESET;
    }

    public static String brightBlue(String text) {
        return ESCAPE + TextColor.BRIGHT_BLUE.getCode() + "m" + text + RESET;
    }

    public static String brightMagenta(String text) {
        return ESCAPE + TextColor.BRIGHT_MAGENTA.getCode() + "m" + text + RESET;
    }

    public static String brightCyan(String text) {
        return ESCAPE + TextColor.BRIGHT_CYAN.getCode() + "m" + text + RESET;
    }

    public static String brightWhite(String text) {
        return ESCAPE + TextColor.BRIGHT_WHITE.getCode() + "m" + text + RESET;
    }

    // Background colors

    public static String bgBlack(String text) {
        return ESCAPE + TextBackgroundColor.BLACK.getCode() + "m" + text + RESET;
    }

    public static String bgRed(String text) {
        return ESCAPE + TextBackgroundColor.RED.getCode() + "m" + text + RESET;
    }

    public static String bgGreen(String text) {
        return ESCAPE + TextBackgroundColor.GREEN.getCode() + "m" + text + RESET;
    }

    public static String bgYellow(String text) {
        return ESCAPE + TextBackgroundColor.YELLOW.getCode() + "m" + text + RESET;
    }

    public static String bgBlue(String text) {
        return ESCAPE + TextBackgroundColor.BLUE.getCode() + "m" + text + RESET;
    }

    public static String bgMagenta(String text) {
        return ESCAPE + TextBackgroundColor.MAGENTA.getCode() + "m" + text + RESET;
    }

    public static String bgCyan(String text) {
        return ESCAPE + TextBackgroundColor.CYAN.getCode() + "m" + text + RESET;
    }

    public static String bgWhite(String text) {
        return ESCAPE + TextBackgroundColor.WHITE.getCode() + "m" + text + RESET;
    }

    public static String bgBrightBlack(String text) {
        return ESCAPE + TextBackgroundColor.BRIGHT_BLACK.getCode() + "m" + text + RESET;
    }

    public static String bgBrightRed(String text) {
        return ESCAPE + TextBackgroundColor.BRIGHT_RED.getCode() + "m" + text + RESET;
    }

    public static String bgBrightGreen(String text) {
        return ESCAPE + TextBackgroundColor.BRIGHT_GREEN.getCode() + "m" + text + RESET;
    }

    public static String bgBrightYellow(String text) {
        return ESCAPE + TextBackgroundColor.BRIGHT_YELLOW.getCode() + "m" + text + RESET;
    }

    public static String bgBrightBlue(String text) {
        return ESCAPE + TextBackgroundColor.BRIGHT_BLUE.getCode() + "m" + text + RESET;
    }

    public static String bgBrightMagenta(String text) {
        return ESCAPE + TextBackgroundColor.BRIGHT_MAGENTA.getCode() + "m" + text + RESET;
    }

    public static String bgBrightCyan(String text) {
        return ESCAPE + TextBackgroundColor.BRIGHT_CYAN.getCode() + "m" + text + RESET;
    }

    public static String bgBrightWhite(String text) {
        return ESCAPE + TextBackgroundColor.BRIGHT_WHITE.getCode() + "m" + text + RESET;
    }

    // Styles

    public static String bold(String text) {
        return ESCAPE + TextStyle.BOLD.getCode() + "m" + text + RESET;
    }

    public static String italic(String text) {
        return ESCAPE + TextStyle.ITALIC.getCode() + "m" + text + RESET;
    }

    public static String underline(String text) {
        return ESCAPE + TextStyle.UNDERLINE.getCode() + "m" + text + RESET;
    }

    public static String dim(String text) {
        return ESCAPE + TextStyle.DIM.getCode() + "m" + text + RESET;
    }

    public static String blink(String text) {
        return ESCAPE + TextStyle.BLINK.getCode() + "m" + text + RESET;
    }

    public static String reverse(String text) {
        return ESCAPE + TextStyle.REVERSE.getCode() + "m" + text + RESET;
    }

    public static String hidden(String text) {
        return ESCAPE + TextStyle.HIDDEN.getCode() + "m" + text + RESET;
    }

    public static String strikethrough(String text) {
        return ESCAPE + TextStyle.STRIKETHROUGH.getCode() + "m" + text + RESET;
    }

    // Formatting via options

    public static String format(String text, TextFormattingOptions options) {
        String escapeSequence = options.buildEscapeSequence();

        if (!escapeSequence.isEmpty()) {
            text = escapeSequence + text + RESET;
        }

        if (options.getMinWidth() > 0) {
            text = align(options.getMinWidth(), text, options.getAlignment());
        }

        return text;
    }

    // Alignment

    public static String align(int lineLength, String text, TextAlignement alignement) {
        if (text == null) {
            text = "";
        }

        if (lineLength < Functions.visibleLength(text)) {
            throw new IllegalArgumentException("La longueur de la ligne doit être plus grande que celle du texte reçu");
        }

        return switch (alignement) {
            case LEFT, RIGHT -> alignSide(lineLength, text, alignement == TextAlignement.LEFT ? "-" : "");
            case CENTER -> center(lineLength, text);
        };
    }

    public static String alignLeft(int lineLength, String text) {
        return align(lineLength, text, TextAlignement.LEFT);
    }

    public static String alignRight(int lineLength, String text) {
        return align(lineLength, text, TextAlignement.RIGHT);
    }

    public static String center(int lineLength, String text) {
        int visibleTextLength;

        if (text == null) {
            text = "";
            visibleTextLength = 0;
        } else {
            visibleTextLength = Functions.visibleLength(text);
        }

        if (lineLength < visibleTextLength) {
            throw new IllegalArgumentException("La longueur de la ligne doit être plus grande que celle du texte reçu");
        }

        int lineLengthBeforeMidPoint = lineLength / 2;
        int writingStartPosition = lineLengthBeforeMidPoint - visibleTextLength / 2;

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(" ".repeat(writingStartPosition));
        stringBuilder.append(text);
        stringBuilder.append(" ".repeat(lineLength - visibleTextLength - writingStartPosition));

        return stringBuilder.toString();
    }

    private static String alignSide(int lineLength, String text, String flag) {
        return ("%" + flag + (lineLength + text.length() - Functions.visibleLength(text)) + "s").formatted(text);
    }
}
