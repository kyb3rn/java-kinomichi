package utils.io.commands.list;

import utils.io.commands.Command;
import utils.io.commands.CommandArgument;
import utils.io.commands.exceptions.CommandArgumentsException;
import utils.io.commands.exceptions.TooManyCommandArgumentsException;

import java.util.ArrayList;

public class ExitCommand extends Command {

    public ExitCommand(ArrayList<CommandArgument> arguments) throws CommandArgumentsException {
        if (!arguments.isEmpty()) {
            throw new TooManyCommandArgumentsException();
        }
    }

    @Override
    protected void addArgument(CommandArgument argument) throws CommandArgumentsException {
        throw new TooManyCommandArgumentsException();
    }

    @Override
    protected void addArguments(ArrayList<CommandArgument> arguments) throws CommandArgumentsException {
        throw new TooManyCommandArgumentsException();
    }

}
