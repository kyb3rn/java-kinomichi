package utils.io.commands;

public class CommandResponseException extends CommandException {

    // ─── Properties ─── //

    private final Object response;

    // ─── Constructors ─── //

    public CommandResponseException(Object response) {
        this.response = response;
    }

    // ─── Getters ─── //

    public Object getResponse() {
        return this.response;
    }

}
