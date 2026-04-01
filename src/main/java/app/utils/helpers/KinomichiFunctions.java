package app.utils.helpers;

import app.models.ModelException;
import app.models.managers.DataManagerException;
import app.utils.ExitProgramException;
import app.utils.ThrowingStringAcceptor;
import app.utils.menus.ExitInputPromptException;
import app.utils.menus.GoBackException;
import app.utils.menus.InvalidInputFormMenuException;
import utils.io.commands.*;
import utils.io.commands.list.BackCommand;
import utils.io.commands.list.ExitCommand;
import utils.io.helpers.Functions;

import utils.io.menus.HookInterruptException;
import utils.io.menus.MenuResponse;

import java.util.Scanner;
import java.util.function.Function;
import java.util.function.Supplier;

public class KinomichiFunctions extends Functions {

    // ─── Utility methods ─── //

    public static void promptInput(Scanner scanner, CommandHandler commandHandler, ThrowingStringAcceptor inputHandler) throws ExitProgramException, ExitInputPromptException {
        promptInput(scanner, commandHandler, inputHandler, null, null);
    }

    public static void promptInput(Scanner scanner, CommandHandler commandHandler, ThrowingStringAcceptor inputHandler, Supplier<MenuResponse> beforePrompt, Function<String, MenuResponse> afterEveryInput) throws ExitProgramException, ExitInputPromptException {
        scanner = scanner != null ? scanner : new Scanner(System.in);

        while (true) {
            if (beforePrompt != null) {
                MenuResponse hookResponse = beforePrompt.get();
                if (hookResponse != null) {
                    throw new HookInterruptException(hookResponse);
                }
            }

            System.out.print("> ");
            try {
                String input = scanner.nextLine();

                if (afterEveryInput != null) {
                    MenuResponse hookResponse = afterEveryInput.apply(input);
                    if (hookResponse != null) {
                        throw new HookInterruptException(hookResponse);
                    }
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
            } catch (ExitProgramException | ExitInputPromptException | HookInterruptException e) {
                throw e;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static void promptField(Scanner scanner, ThrowingStringAcceptor inputHandler) throws ExitProgramException, ExitInputPromptException {
        promptInput(scanner, (_, command) -> {
            switch (command) {
                case ExitCommand _ -> throw new ExitProgramException();
                case BackCommand _ -> {
                    System.out.println();
                    throw new GoBackException();
                }
                default -> throw new UnhandledCommandException(command);
            }
        }, inputHandler);
    }

}
