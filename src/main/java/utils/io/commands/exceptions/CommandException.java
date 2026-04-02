package utils.io.commands.exceptions;

public class CommandException extends Exception {

    // ─── Constructors ─── //

    public CommandException() {
    }

    public CommandException(String message) {
        super(message);
    }

    public CommandException(Throwable cause) {
        super(cause);
    }

    public CommandException(String message, Throwable cause) {
        super(message, cause);
    }

}
