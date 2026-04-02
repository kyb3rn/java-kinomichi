package utils.io.commands;

import utils.io.commands.exceptions.*;
import utils.io.commands.list.BackBackCommand;
import utils.io.commands.list.BackCommand;
import utils.io.commands.list.ExitCommand;
import utils.io.commands.list.SortColumnCommand;

import java.util.ArrayList;
import java.util.HashMap;

public class CommandManager {

    private static final HashMap<ECommand, Class<? extends Command>> commands = new HashMap<>();

    public static Command convertInput(String input) throws NotACommandException, UnknownCommandException, CommandArgumentsException, UnimplementedCommandException, CommandInstanciationException {
        if (input != null && !input.isBlank() && input.charAt(0) == '!') {
            String[] args = input.substring(1).split("\\s");
            String stringCommand = args[0];

            if (!stringCommand.isEmpty()) {
                ECommand eCommand = ECommand.convert(stringCommand);

                if (commands.containsKey(eCommand)) {
                    Class<? extends Command> commandClass = commands.get(eCommand);
                    ArrayList<CommandArgument> commandArguments = new ArrayList<>();

                    for (int i = 1; i < args.length; i++) {
                        commandArguments.add(new CommandArgument(args[i]));
                    }
                    try {
                        return commandClass.getDeclaredConstructor(ArrayList.class).newInstance(commandArguments);
                    } catch (Exception e) {
                        if (e.getCause() instanceof CommandArgumentsException commandArgumentsException) {
                            throw commandArgumentsException;
                        }

                        throw new CommandInstanciationException(e);
                    }
                } else {
                    throw new UnimplementedCommandException(eCommand);
                }
            }
        }

        throw new NotACommandException();
    }

    public static void loadCommands() {
        commands.put(ECommand.EXIT, ExitCommand.class);
        commands.put(ECommand.BACK, BackCommand.class);
        commands.put(ECommand.BACK_BACK, BackBackCommand.class);
        commands.put(ECommand.SORT_COLUMN, SortColumnCommand.class);
    }

}
