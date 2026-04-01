package utils.io.commands;

public class BadCommandArgumentFormatException extends CommandArgumentException {

    public BadCommandArgumentFormatException() {
    }

    public BadCommandArgumentFormatException(String message) {
        super(message);
    }

    public BadCommandArgumentFormatException(Throwable cause) {
        super(cause);
    }

    public BadCommandArgumentFormatException(String message, Throwable cause) {
        super(message, cause);
    }

}
