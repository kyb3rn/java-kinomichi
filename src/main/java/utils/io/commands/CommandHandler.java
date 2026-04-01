package utils.io.commands;

import app.utils.ExitProgramException;
import app.utils.menus.ExitInputPromptException;
import app.utils.menus.InvalidInputFormMenuException;

@FunctionalInterface
public interface CommandHandler {
    /**
     * @throws ExitProgramException to quit the program
     * @throws ExitInputPromptException to signal that the program wants to exit the command handler
     * @throws InvalidInputFormMenuException to signal that the command input is invalid
     * @throws UnhandledCommandException to signal the command is not handled here
     */
    void handle(String input, Command command) throws ExitProgramException, ExitInputPromptException, InvalidInputFormMenuException, UnhandledCommandException;
}
