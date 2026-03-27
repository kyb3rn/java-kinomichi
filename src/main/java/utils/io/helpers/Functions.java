package utils.io.helpers;

import utils.io.helpers.texts.formatting.TextFormatter;

public class Functions {

    /** Special methods **/

    public static String toSnakeCase(String value) {
        if (value == null || value.isBlank()) {
            return value;
        }

        StringBuilder result = new StringBuilder();
        result.append(Character.toLowerCase(value.charAt(0)));

        for (int i = 1; i < value.length(); i++) {
            char c = value.charAt(i);

            if (Character.isUpperCase(c)) {
                char prev = value.charAt(i - 1);
                boolean nextIsLower = i + 1 < value.length() && Character.isLowerCase(value.charAt(i + 1));

                if (Character.isLowerCase(prev) || nextIsLower) {
                    result.append('_');
                }
                result.append(Character.toLowerCase(c));
            } else {
                result.append(c);
            }
        }

        return result.toString();
    }

    public static int visibleLength(String text) {
        return text.replaceAll("\033\\[[0-9;]*m", "").length();
    }

    public static String styleAsErrorMessage(String text) {
        return TextFormatter.red(TextFormatter.italic(text));
    }

}
