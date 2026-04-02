package utils.io.commands.exceptions;

public class TooFewCommandArgumentsException extends CommandArgumentsAmountException {

    public TooFewCommandArgumentsException() {
    }

    public TooFewCommandArgumentsException(String message) {
        super(message);
    }

    public TooFewCommandArgumentsException(Throwable cause) {
        super(cause);
    }

    public TooFewCommandArgumentsException(String message, Throwable cause) {
        super(message, cause);
    }

}
