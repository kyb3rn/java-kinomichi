package utils.io.commands.exceptions;

public abstract class CommandArgumentsException extends CommandException {

    public CommandArgumentsException() {
    }

    public CommandArgumentsException(String message) {
        super(message);
    }

    public CommandArgumentsException(Throwable cause) {
        super(cause);
    }

    public CommandArgumentsException(String message, Throwable cause) {
        super(message, cause);
    }

}
