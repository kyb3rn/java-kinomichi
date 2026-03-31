package utils.io.commands;

public class CommandArgumentException extends CommandException {

    public CommandArgumentException() {
    }

    public CommandArgumentException(String message) {
        super(message);
    }

    public CommandArgumentException(Throwable cause) {
        super(cause);
    }

    public CommandArgumentException(String message, Throwable cause) {
        super(message, cause);
    }

}
