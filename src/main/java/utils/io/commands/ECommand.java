package utils.io.commands;

public enum ECommand {

    QUIT("q"),
    BACK("b"),
    SORT("sort");

    private final String command;

    ECommand(String command) {
        this.command = command;
    }

    public String getCommand() {
        return this.command;
    }

    public static ECommand convert(String command) throws UnknownCommandException, NotACommandException {
        if (command == null) {
            throw new NotACommandException();
        }

        for (ECommand c : ECommand.values()) {
            if (c.command != null && c.command.equalsIgnoreCase(command)) {
                return c;
            }
        }

        throw new UnknownCommandException();
    }

}
