package utils.io.commands.exceptions;

public class CommandInstanciationException extends CommandException {

    public CommandInstanciationException() {
    }

    public CommandInstanciationException(String message) {
        super(message);
    }

    public CommandInstanciationException(Throwable cause) {
        super(cause);
    }

    public CommandInstanciationException(String message, Throwable cause) {
        super(message, cause);
    }

}
