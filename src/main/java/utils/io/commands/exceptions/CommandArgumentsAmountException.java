package utils.io.commands.exceptions;

public class CommandArgumentsAmountException extends CommandArgumentsException {

    public CommandArgumentsAmountException() {
    }

    public CommandArgumentsAmountException(String message) {
        super(message);
    }

    public CommandArgumentsAmountException(Throwable cause) {
        super(cause);
    }

    public CommandArgumentsAmountException(String message, Throwable cause) {
        super(message, cause);
    }

}
