package utils.io.commands;

import java.util.ArrayList;

public class CommandManager {

    public static Command convertInput(String input) throws NotACommandException, UnknownCommandException, CommandArgumentException {
        if (input != null && !input.isBlank() && input.charAt(0) == '!') {
            String[] args = input.substring(1).split("\\s");
            String stringCommand = args[0];

            if (!stringCommand.isEmpty()) {
                ECommand eCommand = ECommand.convert(stringCommand);
                ArrayList<CommandArgument> arguments = new ArrayList<>();

                for (int i = 1; i < args.length; i++) {
                    arguments.add(new CommandArgument(args[i]));
                }

                return new Command(eCommand, arguments);
            }
        }

        throw new NotACommandException();
    }

}
