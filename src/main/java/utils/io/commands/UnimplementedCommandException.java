package utils.io.commands;

public class UnimplementedCommandException extends CommandException {

    private final ECommand eCommand;

    public UnimplementedCommandException(ECommand eCommand) {
        this.eCommand = eCommand;
    }

    public UnimplementedCommandException(ECommand eCommand, String message) {
        super(message);
        this.eCommand = eCommand;
    }

    public UnimplementedCommandException(ECommand eCommand, Throwable cause) {
        super(cause);
        this.eCommand = eCommand;
    }

    public UnimplementedCommandException(ECommand eCommand, String message, Throwable cause) {
        super(message, cause);
        this.eCommand = eCommand;
    }

    public ECommand geteCommand() {
        return eCommand;
    }

}
