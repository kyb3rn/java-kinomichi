package utils.helpers.validation;

import java.time.DateTimeException;
import java.time.Instant;
import java.util.regex.Pattern;

public class Validators {

    public static final Pattern REGEX_EMAIL_PATTERN = Pattern.compile("^[\\\\w+-]+(\\\\.[\\\\w+-]+)*@[\\\\w-]+(\\\\.[\\\\w-]+)*\\\\.[a-zA-Z]{2,}$");

    public static String validateNotNullOrEmpty(String value) throws BlankOrNullValueValidatorException {
        return validateNotNullOrEmpty(value, true);
    }

    public static String validateNotNullOrEmpty(String value, boolean strip) throws BlankOrNullValueValidatorException {
        if (value == null || value.isBlank()) {
            throw new BlankOrNullValueValidatorException();
        }

        return strip ? value.strip() : value;
    }

    public static int validateInt(String value) throws BlankOrNullValueValidatorException, ParsingValidatorException {
        if (value == null || value.isBlank()) {
            throw new BlankOrNullValueValidatorException();
        }

        value = value.strip();

        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new ParsingValidatorException(e);
        }
    }

    public static int validatePositiveInt(String value) throws BlankOrNullValueValidatorException, ParsingValidatorException, BelowBoundaryValidatorException {
        int i = validateInt(value);

        if (i < 0) {
            throw new BelowBoundaryValidatorException(i);
        }

        return i;
    }

    public static int validateStrictlyPositiveInt(String value) throws BlankOrNullValueValidatorException, ParsingValidatorException, BelowBoundaryValidatorException {
        int i = validatePositiveInt(value);

        if (i == 0) {
            throw new BelowBoundaryValidatorException(i);
        }

        return i;
    }

    public static Integer validateInteger(String value) throws BlankOrNullValueValidatorException, ParsingValidatorException {
        if (value == null) {
            return null;
        }

        return validateInt(value);
    }

    public static Integer validatePositiveInteger(String value) throws BlankOrNullValueValidatorException, ParsingValidatorException, BelowBoundaryValidatorException {
        if (value == null) {
            return null;
        }

        return validatePositiveInt(value);
    }

    public static Integer validateStrictlyPositiveInteger(String value) throws BlankOrNullValueValidatorException, ParsingValidatorException, BelowBoundaryValidatorException {
        if (value == null) {
            return null;
        }

        return validateStrictlyPositiveInt(value);
    }

    public static String validateEmail(String value) throws BlankOrNullValueValidatorException, PatternMatchingValidatorException {
        if (value == null || value.isBlank()) {
            throw new BlankOrNullValueValidatorException();
        }

        value = value.strip();

        if (REGEX_EMAIL_PATTERN.matcher(value).matches()) {
            throw new PatternMatchingValidatorException();
        }

        return value;
    }

    public static double validateDouble(String value) throws BlankOrNullValueValidatorException, ParsingValidatorException {
        return validateDouble(value, false);
    }

    public static double validateDouble(String value, boolean lax) throws BlankOrNullValueValidatorException, ParsingValidatorException {
        if (value == null || value.isBlank()) {
            throw new BlankOrNullValueValidatorException();
        }

        value = value.strip();

        if (lax) {
            value = value.replace(',', '.');
        }

        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            throw new ParsingValidatorException(e);
        }
    }

    public static Instant validateInstant(String value) throws BlankOrNullValueValidatorException, ParsingValidatorException {
        if (value == null || value.isBlank()) {
            throw new BlankOrNullValueValidatorException();
        }

        value = value.strip();

        Instant valueInstant;
        try {
            valueInstant = Instant.parse(value);
        } catch (DateTimeException e) {
            throw new ParsingValidatorException(e);
        }

        return valueInstant;
    }

}
