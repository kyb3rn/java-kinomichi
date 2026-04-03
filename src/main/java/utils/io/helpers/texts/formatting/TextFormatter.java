package utils.io.helpers.texts.formatting;

import java.util.*;

import utils.io.helpers.Functions;

public class TextFormatter {

    // ─── Utility methods ─── //

    // Text colors

    public static FormattedText black(Object... parts) {
        return format(TextColor.BLACK, parts);
    }

    public static FormattedText red(Object... parts) {
        return format(TextColor.RED, parts);
    }

    public static FormattedText green(Object... parts) {
        return format(TextColor.GREEN, parts);
    }

    public static FormattedText yellow(Object... parts) {
        return format(TextColor.YELLOW, parts);
    }

    public static FormattedText blue(Object... parts) {
        return format(TextColor.BLUE, parts);
    }

    public static FormattedText magenta(Object... parts) {
        return format(TextColor.MAGENTA, parts);
    }

    public static FormattedText cyan(Object... parts) {
        return format(TextColor.CYAN, parts);
    }

    public static FormattedText white(Object... parts) {
        return format(TextColor.WHITE, parts);
    }

    public static FormattedText brightBlack(Object... parts) {
        return format(TextColor.BRIGHT_BLACK, parts);
    }

    public static FormattedText brightRed(Object... parts) {
        return format(TextColor.BRIGHT_RED, parts);
    }

    public static FormattedText brightGreen(Object... parts) {
        return format(TextColor.BRIGHT_GREEN, parts);
    }

    public static FormattedText brightYellow(Object... parts) {
        return format(TextColor.BRIGHT_YELLOW, parts);
    }

    public static FormattedText brightBlue(Object... parts) {
        return format(TextColor.BRIGHT_BLUE, parts);
    }

    public static FormattedText brightMagenta(Object... parts) {
        return format(TextColor.BRIGHT_MAGENTA, parts);
    }

    public static FormattedText brightCyan(Object... parts) {
        return format(TextColor.BRIGHT_CYAN, parts);
    }

    public static FormattedText brightWhite(Object... parts) {
        return format(TextColor.BRIGHT_WHITE, parts);
    }

    // Background colors

    public static FormattedText bgBlack(Object... parts) {
        return format(TextBackgroundColor.BLACK, parts);
    }

    public static FormattedText bgRed(Object... parts) {
        return format(TextBackgroundColor.RED, parts);
    }

    public static FormattedText bgGreen(Object... parts) {
        return format(TextBackgroundColor.GREEN, parts);
    }

    public static FormattedText bgYellow(Object... parts) {
        return format(TextBackgroundColor.YELLOW, parts);
    }

    public static FormattedText bgBlue(Object... parts) {
        return format(TextBackgroundColor.BLUE, parts);
    }

    public static FormattedText bgMagenta(Object... parts) {
        return format(TextBackgroundColor.MAGENTA, parts);
    }

    public static FormattedText bgCyan(Object... parts) {
        return format(TextBackgroundColor.CYAN, parts);
    }

    public static FormattedText bgWhite(Object... parts) {
        return format(TextBackgroundColor.WHITE, parts);
    }

    public static FormattedText bgBrightBlack(Object... parts) {
        return format(TextBackgroundColor.BRIGHT_BLACK, parts);
    }

    public static FormattedText bgBrightRed(Object... parts) {
        return format(TextBackgroundColor.BRIGHT_RED, parts);
    }

    public static FormattedText bgBrightGreen(Object... parts) {
        return format(TextBackgroundColor.BRIGHT_GREEN, parts);
    }

    public static FormattedText bgBrightYellow(Object... parts) {
        return format(TextBackgroundColor.BRIGHT_YELLOW, parts);
    }

    public static FormattedText bgBrightBlue(Object... parts) {
        return format(TextBackgroundColor.BRIGHT_BLUE, parts);
    }

    public static FormattedText bgBrightMagenta(Object... parts) {
        return format(TextBackgroundColor.BRIGHT_MAGENTA, parts);
    }

    public static FormattedText bgBrightCyan(Object... parts) {
        return format(TextBackgroundColor.BRIGHT_CYAN, parts);
    }

    public static FormattedText bgBrightWhite(Object... parts) {
        return format(TextBackgroundColor.BRIGHT_WHITE, parts);
    }

    // Styles

    public static FormattedText bold(Object... parts) {
        return format(TextStyle.BOLD, parts);
    }

    public static FormattedText italic(Object... parts) {
        return format(TextStyle.ITALIC, parts);
    }

    public static FormattedText underline(Object... parts) {
        return format(TextStyle.UNDERLINE, parts);
    }

    public static FormattedText dim(Object... parts) {
        return format(TextStyle.DIM, parts);
    }

    public static FormattedText blink(Object... parts) {
        return format(TextStyle.BLINK, parts);
    }

    public static FormattedText reverse(Object... parts) {
        return format(TextStyle.REVERSE, parts);
    }

    public static FormattedText hidden(Object... parts) {
        return format(TextStyle.HIDDEN, parts);
    }

    public static FormattedText strikethrough(Object... parts) {
        return format(TextStyle.STRIKETHROUGH, parts);
    }

    // Alignment

    public static FormattedText align(int lineLength, Object text, TextAlignment alignment) {
        return switch (alignment) {
            case NONE -> (text instanceof FormattedText formattedText) ? formattedText : new FormattedText(toSegments(text));
            case LEFT -> alignLeft(lineLength, text);
            case RIGHT -> alignRight(lineLength, text);
            case CENTER -> center(lineLength, text);
        };
    }

    public static FormattedText alignLeft(int lineLength, Object text) {
        ArrayList<FormattedText.Segment> segments = toSegments(text);
        int visibleTextLength = visibleLengthOf(segments);
        validateLineLength(lineLength, visibleTextLength);

        int rightPadding = lineLength - visibleTextLength;

        if (rightPadding > 0) {
            segments.add(new FormattedText.Segment(" ".repeat(rightPadding), new TextFormattingOptions()));
        }

        return new FormattedText(segments);
    }

    public static FormattedText alignRight(int lineLength, Object text) {
        ArrayList<FormattedText.Segment> segments = toSegments(text);
        int visibleTextLength = visibleLengthOf(segments);
        validateLineLength(lineLength, visibleTextLength);

        int leftPadding = lineLength - visibleTextLength;

        if (leftPadding > 0) {
            segments.addFirst(new FormattedText.Segment(" ".repeat(leftPadding), new TextFormattingOptions()));
        }

        return new FormattedText(segments);
    }

    public static FormattedText center(int lineLength, Object text) {
        ArrayList<FormattedText.Segment> segments = toSegments(text);
        int visibleTextLength = visibleLengthOf(segments);
        validateLineLength(lineLength, visibleTextLength);

        int lineLengthBeforeMidPoint = lineLength / 2;
        int writingStartPosition = lineLengthBeforeMidPoint - visibleTextLength / 2;

        if (writingStartPosition > 0) {
            segments.addFirst(new FormattedText.Segment(" ".repeat(writingStartPosition), new TextFormattingOptions()));
        }

        int rightPadding = lineLength - visibleTextLength - writingStartPosition;

        if (rightPadding > 0) {
            segments.add(new FormattedText.Segment(" ".repeat(rightPadding), new TextFormattingOptions()));
        }

        return new FormattedText(segments);
    }

    private static ArrayList<FormattedText.Segment> toSegments(Object object) {
        if (object instanceof FormattedText formattedText) {
            return new ArrayList<>(formattedText.getSegments());
        }

        String text = (object != null) ? object.toString() : "";

        ArrayList<FormattedText.Segment> segments = new ArrayList<>();
        segments.add(new FormattedText.Segment(text, new TextFormattingOptions()));

        return segments;
    }

    private static int visibleLengthOf(Collection<FormattedText.Segment> segments) {
        return segments.stream().mapToInt(segment -> Functions.visibleLength(segment.rawText())).sum();
    }

    private static void validateLineLength(int lineLength, int visibleTextLength) {
        if (lineLength < visibleTextLength) {
            throw new IllegalArgumentException("La longueur de la ligne doit être plus grande que celle du texte reçu");
        }
    }

    private static FormattedText format(TextColor color, Object... parts) {
        return format(new TextFormattingOptions().setColor(color), parts);
    }

    private static FormattedText format(TextBackgroundColor backgroundColor, Object... parts) {
        return format(new TextFormattingOptions().setBackgroundColor(backgroundColor), parts);
    }

    private static FormattedText format(TextStyle style, Object... parts) {
        return format(new TextFormattingOptions().addStyle(style), parts);
    }

    public static FormattedText format(TextFormattingOptions options, Object... parts) {
        List<FormattedText.Segment> segments = new ArrayList<>();

        if (parts == null) {
            return new FormattedText(segments);
        }

        for (Object part : parts) {
            if (part instanceof FormattedText formattedText) {
                FormattedText mergedFormattedText = formattedText.withMergedFormatting(options);
                segments.addAll(mergedFormattedText.getSegments());
            } else {
                String text = (part != null) ? part.toString() : "";
                segments.add(new FormattedText.Segment(text, new TextFormattingOptions(options)));
            }
        }

        return new FormattedText(segments);
    }

}
