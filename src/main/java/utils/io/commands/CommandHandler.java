package utils.io.commands;

import utils.io.commands.exceptions.UnhandledCommandException;

@FunctionalInterface
public interface CommandHandler {

    /**
     * @return an object response to signal exit, or null to continue
     * @throws UnhandledCommandException to signal the command is not handled here
     */
    Object handle(String input, Command command) throws UnhandledCommandException;

}
