package app.views.utils;

import app.models.ModelException;
import app.models.managers.DataManagerException;
import app.utils.ExitProgramException;
import app.utils.ThrowingStringAcceptor;
import utils.io.commands.*;
import utils.io.helpers.Functions;
import utils.io.menus.MenuStage;

import java.util.Scanner;
import java.util.function.Consumer;

public abstract class FormMenuStage extends MenuStage {

    // ─── Sub classes ─── //

    @FunctionalInterface
    protected interface CommandHandler {
        /**
         * @throws ExitProgramException to quit the program
         * @throws ExitCommandHandlerException to signal that the program wants to exit the command handler
         * @throws InvalidInputFormMenuException to signal that the command input is invalid
         * @throws UnhandledCommandException to signal the command is not handled here
         */
        void handle(String input, Command command) throws ExitProgramException, ExitCommandHandlerException, InvalidInputFormMenuException, UnhandledCommandException;
    }

    // ─── Utility methods ─── //

    protected void promptInput(Scanner scanner, CommandHandler commandHandler, ThrowingStringAcceptor inputHandler) throws ExitProgramException, ExitCommandHandlerException {
        this.promptInput(scanner, commandHandler, inputHandler, null, null);
    }

    protected void promptInput(Scanner scanner, CommandHandler commandHandler, ThrowingStringAcceptor inputHandler, Runnable beforePrompt, Consumer<String> afterEveryInput) throws ExitProgramException, ExitCommandHandlerException {
        while (true) {
            if (beforePrompt != null) {
                beforePrompt.run();
            }

            System.out.printf("> ");
            try {
                String input = scanner.nextLine();

                if (afterEveryInput != null) {
                    afterEveryInput.accept(input);
                }

                try {
                    Command command = CommandManager.convertInput(input);
                    commandHandler.handle(input, command);
                } catch (NotACommandException _) {
                    inputHandler.accept(input);
                    break;
                } catch (UnknownCommandException _) {
                    System.out.println(Functions.styleAsErrorMessage("Cette commande n'existe pas."));
                } catch (CommandArgumentException _) {
                    System.out.println(Functions.styleAsErrorMessage("Les arguments de cette commande sont invalides."));
                } catch (UnhandledCommandException _) {
                    System.out.println(Functions.styleAsErrorMessage("Cette commande n'est pas prise en charge ici."));
                }
            } catch (InvalidInputFormMenuException | ModelException | DataManagerException e) {
                System.out.println(Functions.styleAsErrorMessage(e.getMessage()));
            } catch (ExitProgramException | ExitCommandHandlerException e) {
                throw e;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    protected void promptField(Scanner scanner, ThrowingStringAcceptor inputHandler) throws ExitProgramException, ExitCommandHandlerException {
        this.promptInput(scanner, (_, command) -> {
            switch (command.getCommand()) {
                case QUIT -> throw new ExitProgramException();
                case BACK -> {
                    System.out.println();
                    throw new GoBackException();
                }
                default -> throw new UnhandledCommandException(command);
            }
        }, inputHandler);
    }

}
