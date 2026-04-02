package utils.io.commands;

import utils.io.commands.exceptions.CommandArgumentsException;

import java.util.ArrayList;

public abstract class Command {

    private final ArrayList<CommandArgument> arguments = new ArrayList<>();

    public Command(ArrayList<CommandArgument> arguments) throws CommandArgumentsException {
        this.arguments.addAll(arguments);
    }

    public Command() {
    }

    public ArrayList<CommandArgument> getArguments() {
        return arguments;
    }

    protected abstract void addArgument(CommandArgument argument) throws CommandArgumentsException;

    protected abstract void addArguments(ArrayList<CommandArgument> arguments) throws CommandArgumentsException;

}
