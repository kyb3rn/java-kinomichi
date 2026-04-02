package utils.io.commands.exceptions;

public class BadCommandArgumentFormatException extends CommandArgumentsException {

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
