package utils.io.commands;

import utils.io.commands.list.BackCommand;
import utils.io.commands.list.ExitCommand;
import utils.io.commands.list.SortColumnCommand;

import java.util.ArrayList;
import java.util.HashMap;

public class CommandManager {

    private static final HashMap<ECommand, Class<? extends Command>> commands = new HashMap<>();

    public static Command convertInput(String input) throws NotACommandException, UnknownCommandException, CommandArgumentException, UnimplementedCommandException {
        if (input != null && !input.isBlank() && input.charAt(0) == '!') {
            String[] args = input.substring(1).split("\\s");
            String stringCommand = args[0];

            if (!stringCommand.isEmpty()) {
                ECommand eCommand = ECommand.convert(stringCommand);

                if (commands.containsKey(eCommand)) {
                    Class<? extends Command> commandClass = commands.get(eCommand);
                    try {
                        ArrayList<CommandArgument> commandArguments = new ArrayList<>();

                        for (int i = 1; i < args.length; i++) {
                            commandArguments.add(new CommandArgument(args[i]));
                        }

                        return commandClass.getDeclaredConstructor(ArrayList.class).newInstance(commandArguments);
                    } catch (ReflectiveOperationException reflectiveOperationException) {
                        throw new RuntimeException(reflectiveOperationException);
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
        commands.put(ECommand.SORT_COLUMN, SortColumnCommand.class);
    }

}
