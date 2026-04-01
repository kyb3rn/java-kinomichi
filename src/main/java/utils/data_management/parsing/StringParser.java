package utils.data_management.parsing;

public class StringParser {

    // ─── Utility methods ─── //

    public static int toPositiveInt(String raw, String fieldName) throws StringParserException {
        try {
            int val = Integer.parseInt(raw.trim());
            if (val < 0) {
                throw new StringParserException(
                    "Attribut %s invalide (attendu: entier positif, obtenu: %s)".formatted(fieldName, val));
            }
            return val;
        } catch (NumberFormatException e) {
            throw new StringParserException(
                "Attribut %s invalide (attendu: entier positif, obtenu: %s)".formatted(fieldName, raw.trim()), e);
        }
    }

    public static char toChar(String raw, String fieldName) throws StringParserException {
        String trimmed = raw.trim();
        if (trimmed.length() != 1) {
            throw new StringParserException(
                "Attribut %s invalide (attendu: un seul caractère, obtenu: %s)".formatted(fieldName, trimmed));
        }
        return trimmed.charAt(0);
    }

}
