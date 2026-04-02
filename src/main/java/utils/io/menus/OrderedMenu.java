package utils.io.menus;

import app.utils.menus.InvalidMenuInputException;
import utils.io.commands.*;
import utils.io.helpers.Functions;

import java.util.Scanner;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicReference;

public abstract class OrderedMenu extends Menu {

    // ─── Properties ─── //

    protected final TreeMap<Integer, String> unoptionedRows = new TreeMap<>();
    protected final TreeSet<Integer> sectionSeparationIndexes = new TreeSet<>();
    private CommandHandler commandHandler;

    // ─── Setters ─── //

    public void setCommandHandler(CommandHandler commandHandler) {
        this.commandHandler = commandHandler;
    }

    // ─── Utility methods ─── //

    public void addOption(String text) {
        this.addOption(text, null);
    }

    public void addOption(String text, Object response) {
        int order = this.options.size() + 1;
        this.options.add(new OrderedMenuOption(order, text, response));
    }

    @Override
    protected MenuResponse askingValidInputLoopHandle(AtomicReference<String> validInput, AtomicReference<OrderedMenuOption> selectedOption) {
        Scanner scanner = new Scanner(System.in);

        while (true) {
            MenuResponse hookResponse = this.beforeInput();
            if (hookResponse != null) {
                this.beforeUseExit();
                return hookResponse;
            }

            System.out.print("> ");
            try {
                String input = scanner.nextLine();

                hookResponse = this.afterEveryInput(input);
                if (hookResponse != null) {
                    this.beforeUseExit();
                    return hookResponse;
                }

                if (this.commandHandler != null) {
                    try {
                        Command command = CommandManager.convertInput(input);
                        try {
                            Object commandResponse = this.commandHandler.handle(input, command);
                            if (commandResponse != null) {
                                return new MenuResponse(commandResponse);
                            }
                            continue;
                        } catch (UnhandledCommandException _) {
                            System.out.println(Functions.styleAsErrorMessage("Cette commande n'est pas prise en charge ici."));
                            continue;
                        }
                    } catch (NotACommandException _) {
                        // Not a command, fall through to numeric parsing
                    } catch (UnknownCommandException _) {
                        System.out.println(Functions.styleAsErrorMessage("Cette commande n'existe pas."));
                        continue;
                    } catch (CommandArgumentException _) {
                        System.out.println(Functions.styleAsErrorMessage("Les arguments de cette commande sont invalides."));
                        continue;
                    } catch (UnimplementedCommandException _) {
                        System.out.println(Functions.styleAsErrorMessage("Cette commande n'est pas implémentée."));
                        continue;
                    }
                }

                if (this.options.isEmpty()) {
                    throw new InvalidMenuInputException("L'entrée '%s' n'est pas une commande.".formatted(input));
                }

                int parsedChoice;
                try {
                    parsedChoice = Integer.parseInt(input);
                } catch (NumberFormatException e) {
                    throw new InvalidMenuInputException("L'entrée '%s' est invalide. Veuillez entrer un nombre entier strictement positif.".formatted(input), e);
                }

                int optionsSize = this.options.size();
                if (parsedChoice < 1 || parsedChoice > optionsSize) {
                    if (optionsSize == 1) {
                        throw new InvalidMenuInputException("Le choix '%s' est invalide. Seul le choix 1 est valide.".formatted(input));
                    } else if (optionsSize == 2) {
                        throw new InvalidMenuInputException("Le choix '%s' est invalide. Veuillez entrer soit 1, soit 2.".formatted(input));
                    } else {
                        throw new InvalidMenuInputException("Le choix '%s' est invalide. Veuillez choisir une option entre 1 et %s (inclus).".formatted(input, optionsSize));
                    }
                }

                validInput.set(input);
                selectedOption.set((OrderedMenuOption) this.options.get(parsedChoice - 1));

                break;
            } catch (InvalidMenuInputException e) {
                System.out.println(Functions.styleAsErrorMessage(e.getMessage()));
            }
        }

        return null;
    }

    public void addSectionSeparationIndex() {
        sectionSeparationIndexes.add(this.options.size() + this.unoptionedRows.size());
    }

    public void addUnoptionedRow(String row) {
        this.unoptionedRows.put(this.options.size(), row);
    }

}
