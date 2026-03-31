package utils.io.commands;

import java.util.ArrayList;

public class Command {

    private final ECommand command;
    private final ArrayList<CommandArgument> arguments;

    public Command(ECommand command, ArrayList<CommandArgument> arguments) {
        this.command = command;
        this.arguments = arguments;
    }

    public Command(ECommand eCommand) {
        this.command = eCommand;
        this.arguments = new ArrayList<>();
    }

    public ECommand getCommand() {
        return command;
    }

    public ArrayList<CommandArgument> getArguments() {
        return arguments;
    }

    public void addArgument(CommandArgument argument) {
        this.arguments.add(argument);
    }

}
