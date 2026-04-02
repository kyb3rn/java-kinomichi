package utils.io.commands.exceptions;

import utils.io.commands.Command;

public class UnhandledCommandException extends CommandException {

    private final Command command;

    public UnhandledCommandException(Command command) {
        this.command = command;
    }

    public UnhandledCommandException(Command command, String message) {
        super(message);
        this.command = command;
    }

    public UnhandledCommandException(Command command, Throwable cause) {
        super(cause);
        this.command = command;
    }

    public UnhandledCommandException(Command command, String message, Throwable cause) {
        super(message, cause);
        this.command = command;
    }

    public Command getCommand() {
        return command;
    }

}
