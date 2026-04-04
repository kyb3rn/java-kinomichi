package app.utils.helpers;

import app.utils.ThrowingConsumer;
import app.utils.ThrowingConsumerException;
import utils.io.commands.Command;
import utils.io.commands.CommandHandler;
import utils.io.commands.CommandManager;
import utils.io.commands.exceptions.*;
import utils.io.commands.list.BackBackCommand;
import utils.io.commands.list.BackCommand;
import utils.io.commands.list.ExitCommand;
import utils.helpers.Functions;
import utils.io.menus.HookInterruptException;
import utils.io.menus.MenuResponse;

import java.util.Scanner;
import java.util.function.Function;
import java.util.function.Supplier;

public class KinomichiFunctions extends Functions {

    // ─── Utility methods ─── //

    public static void promptInputWithCommandHandling(Scanner scanner, CommandHandler commandHandler, ThrowingConsumer<String> inputConsumer) throws CommandResponseException {
        promptInputWithCommandHandling(scanner, commandHandler, inputConsumer, null, null);
    }

    public static void promptInputWithCommandHandling(Scanner scanner, CommandHandler commandHandler, ThrowingConsumer<String> inputConsumer, Supplier<MenuResponse> beforePrompt, Function<String, MenuResponse> afterEveryInput) throws CommandResponseException {
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

                if (commandHandler != null) {
                    try {
                        Command command = CommandManager.convertInput(input);
                        try {
                            Object commandResponse = commandHandler.handle(input, command);
                            if (commandResponse != null) {
                                throw new CommandResponseException(commandResponse);
                            }
                        } catch (UnhandledCommandException _) {
                            System.out.println(Functions.styleAsErrorMessage("Cette commande n'est pas prise en charge ici."));
                            continue;
                        }
                    } catch (NotACommandException _) {
                        // Not a command, fall through to input consumer
                    } catch (UnknownCommandException _) {
                        System.out.println(Functions.styleAsErrorMessage("Cette commande n'existe pas."));
                        continue;
                    } catch (CommandArgumentsException _) {
                        System.out.println(Functions.styleAsErrorMessage("Les arguments de cette commande sont invalides."));
                        continue;
                    } catch (UnimplementedCommandException e) {
                        System.out.println(Functions.styleAsErrorMessage("Cette commande n'est pas implémentée."));
                        continue;
                    } catch (CommandInstanciationException e) {
                        System.out.println(Functions.styleAsErrorMessage("Cette commande existe mais n'a pas pu être instanciée."));
                        continue;
                    }
                }

                try {
                    inputConsumer.accept(input);
                } catch (Exception e) {
                    throw new ThrowingConsumerException(e);
                }
                break;
            } catch (ThrowingConsumerException e) {
                if (e.getCause() != null) {
                    System.out.println(Functions.styleAsErrorMessage(e.getCause().getMessage()));
                } else {
                    System.out.println(Functions.styleAsErrorMessage("La gestion de l'input a rencontré un problème inattendu."));
                }
            }
        }
    }

    public static void promptFieldWithExitAndBackCommands(Scanner scanner, ThrowingConsumer<String> inputConsumer) throws CommandResponseException {
        promptInputWithCommandHandling(scanner, (_, command) -> {
            switch (command) {
                case ExitCommand exitCommand -> {
                    return exitCommand;
                }
                case BackCommand backCommand -> {
                    System.out.println();
                    return backCommand;
                }
                case BackBackCommand backBackCommand -> {
                    System.out.println();
                    return backBackCommand;
                }
                default -> throw new UnhandledCommandException(command);
            }
        }, inputConsumer);
    }

}
