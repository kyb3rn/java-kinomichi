package utils.io.commands.list;

import utils.io.commands.Command;
import utils.io.commands.CommandArgument;
import utils.io.commands.CommandArgumentException;
import utils.io.commands.TooManyCommandArgumentsException;

import java.util.ArrayList;
import java.util.List;

public class BackCommand extends Command {

    public BackCommand(ArrayList<CommandArgument> arguments) throws CommandArgumentException {
        if (!arguments.isEmpty()) {
            throw new TooManyCommandArgumentsException();
        }
    }

    @Override
    protected void addArgument(CommandArgument argument) throws CommandArgumentException {
        throw new TooManyCommandArgumentsException();
    }

    @Override
    protected void addArguments(ArrayList<CommandArgument> arguments) throws CommandArgumentException {
        throw new TooManyCommandArgumentsException();
    }

}
