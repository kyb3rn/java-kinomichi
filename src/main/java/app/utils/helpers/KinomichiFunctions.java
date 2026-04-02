package app.utils.helpers;

import app.utils.ThrowingConsumer;
import app.utils.ThrowingConsumerException;
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

    public static void promptInput(Scanner scanner, CommandHandler commandHandler, ThrowingConsumer<String> inputConsumer) throws CommandResponseException {
        promptInput(scanner, commandHandler, inputConsumer, null, null);
    }

    public static void promptInput(Scanner scanner, CommandHandler commandHandler, ThrowingConsumer<String> inputConsumer, Supplier<MenuResponse> beforePrompt, Function<String, MenuResponse> afterEveryInput) throws CommandResponseException {
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
                    try {
                        Object commandResponse = commandHandler.handle(input, command);
                        if (commandResponse != null) {
                            throw new CommandResponseException(commandResponse);
                        }
                    } catch (UnhandledCommandException _) {
                        System.out.println(Functions.styleAsErrorMessage("Cette commande n'est pas prise en charge ici."));
                    }
                } catch (NotACommandException _) {
                    try {
                        inputConsumer.accept(input);
                    } catch (Exception e) {
                        throw new ThrowingConsumerException(e);
                    }
                    break;
                } catch (UnknownCommandException _) {
                    System.out.println(Functions.styleAsErrorMessage("Cette commande n'existe pas."));
                } catch (CommandArgumentException _) {
                    System.out.println(Functions.styleAsErrorMessage("Les arguments de cette commande sont invalides."));
                } catch (UnimplementedCommandException e) {
                    System.out.println(Functions.styleAsErrorMessage("Cette commande n'est pas implémentée."));
                }
            } catch (ThrowingConsumerException e) {
                if (e.getCause() != null) {
                    System.out.println(Functions.styleAsErrorMessage(e.getCause().getMessage()));
                } else {
                    System.out.println(Functions.styleAsErrorMessage("La gestion de l'input a rencontré un problème inattendu."));
                }
            }
        }
    }

    public static void promptField(Scanner scanner, ThrowingConsumer<String> inputConsumer) throws CommandResponseException {
        promptInput(scanner, (_, command) -> {
            switch (command) {
                case ExitCommand exitCommand -> {
                    return exitCommand;
                }
                case BackCommand backCommand -> {
                    System.out.println();
                    return backCommand;
                }
                default -> throw new UnhandledCommandException(command);
            }
        }, inputConsumer);
    }

}
