package utils.io.helpers.texts.aligning;

import utils.io.helpers.Functions;

public class TextAligner {

    /** Special methods **/

    public static String getAlignedText(int lineLength, String text, TextAlignement alignement) {
        if (text == null) {
            text = "";
        }

        if (lineLength < Functions.visibleLength(text)) {
            throw new IllegalArgumentException("La longueur de la ligne doit être plus grande que celle du texte reçu");
        }

        return switch (alignement) {
            case LEFT, RIGHT -> getSideAlignedText(lineLength, text, alignement == TextAlignement.LEFT ? "-" : "");
            case CENTER -> getCenteredText(lineLength, text);
        };
    }

    private static String getSideAlignedText(int lineLength, String text, String flag) {
        return ("%" + flag + (lineLength + text.length() - Functions.visibleLength(text)) + "s").formatted(text);
    }

    public static String getLeftAlignedText(int lineLength, String text) {
        return getAlignedText(lineLength, text, TextAlignement.LEFT);
    }

    public static String getRightAlignedText(int lineLength, String text) {
        return getAlignedText(lineLength, text, TextAlignement.RIGHT);
    }

    public static String getCenteredText(int lineLength, String text) {
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

}
