package utils.io.menus;

import app.middlewares.Middleware;
import utils.io.commands.*;
import utils.io.helpers.Functions;

import java.util.ArrayList;
import java.util.Scanner;
import java.util.TreeMap;
import java.util.TreeSet;

public abstract class OptionedMenuStage extends MenuStage {

    // ─── Properties ─── //

    protected final ArrayList<MenuOption> options = new ArrayList<>();
    protected final TreeMap<Integer, String> unoptionedRows = new TreeMap<>();
    protected final TreeSet<Integer> sectionSeparationIndexes = new TreeSet<>();

    // ─── Utility methods ─── //

    public void addOption(String text) {
        this.options.add(new MenuOption(text, null));
    }

    public void addOption(String text, String leadTo) {
        if (leadTo != null) {
            if (leadTo.isBlank()) {
                throw new IllegalArgumentException("Le point suivant d'une option dans un menu ne peut pas être vide");
            } else {
                leadTo = leadTo.strip();
            }
        }

        this.options.add(new MenuOption(text, new MenuOptionOutcomeLeading(new MenuLeadTo(leadTo))));
    }

    public void addOption(String text, Runnable action, String leadTo) {
        if (leadTo != null) {
            if (leadTo.isBlank()) {
                throw new IllegalArgumentException("Le point suivant d'une option dans un menu ne peut pas être vide");
            } else {
                leadTo = leadTo.strip();
            }
        }

        this.options.add(new MenuOption(text, new MenuOptionOutcomeLeadingAction(action, new MenuLeadTo(leadTo))));
    }

    public void addOption(String text, MenuOptionOutcome menuOptionOutcome) {
        this.options.add(new MenuOption(text, menuOptionOutcome));
    }

    private boolean isChoiceValid(String input) {
        for (MenuOption option : this.options) {
            if (option.getText().equals(input)) {
                return true;
            }
        }

        return false;
    }

    public abstract void display();

    public void beforeDisplay() {}

    public void afterDisplay() {}

    public void beforeInput() {}

    public void afterEveryInput(String input) {}

    public void afterValidInput(String input, int choice) {}

    protected void addSectionSeparationIndex() {
        sectionSeparationIndexes.add(this.options.size() + this.unoptionedRows.size());
    }

    protected void addUnoptionedRow(String row) {
        this.unoptionedRows.put(this.options.size(), row);
    }

    // ─── Overrides & inheritance ─── //

    @Override
    public OptionedMenuMenuLeadTo use() {
        for (Middleware middleware : this.middlewares) {
            MenuLeadTo menuLeadTo = middleware.verify();
            if (menuLeadTo != null) {
                System.out.printf(Functions.styleAsErrorMessage("Ce menu n'est pas accessible.%n%n"));
                return new OptionedMenuMenuLeadTo(-1, menuLeadTo);
            }
        }

        int optionsSize = this.options.size();

        this.beforeDisplay();

        this.display();

        this.afterDisplay();

        if (optionsSize > 0) {
            Scanner scanner = new Scanner(System.in);
            boolean isValidChoice = false;
            MenuOption selectedOption = null;
            String input;
            int choice = -1;

            do {
                this.beforeInput();

                System.out.print("> ");
                input = scanner.nextLine();
                System.out.println();

                this.afterEveryInput(input);

                try {
                    Command command = CommandManager.convertInput(input);
                    switch (command.getCommand()) {
                        case QUIT -> {
                            return null;
                        }
                        default -> throw new UnhandledCommandException(command);
                    }
                } catch (NotACommandException _) {
                    // Continue the process
                } catch (UnknownCommandException e) {
                    System.out.printf(Functions.styleAsErrorMessage("Cette commande n'existe pas.%n%n"));
                    continue;
                } catch (CommandArgumentException e) {
                    System.out.printf(Functions.styleAsErrorMessage("Les arguments de cette commande sont invalides.%n%n"));
                    continue;
                } catch (UnhandledCommandException e) {
                    System.out.printf(Functions.styleAsErrorMessage("Cette commande n'est pas prise en charge ici.%n%n"));
                    continue;
                }

                try {
                    choice = Integer.parseInt(input);
                } catch (NumberFormatException e) {
                    System.out.printf(Functions.styleAsErrorMessage("L'entrée '%s' est invalide. Veuillez entrer un nombre entier strictement positif.%n%n"), input, this.options.size());
                    continue;
                }

                isValidChoice = choice > 0 && choice <= optionsSize;

                if (!isValidChoice) {
                    if (optionsSize == 1) {
                        System.out.printf(Functions.styleAsErrorMessage("Le choix '%s' est invalide. Seul le choix 1 est valide.%n%n"), input);
                    } else if (optionsSize == 2) {
                        System.out.printf(Functions.styleAsErrorMessage("Le choix '%s' est invalide. Veuillez entrer soit 1, soit 2.%n%n"), input);
                    } else {
                        System.out.printf(Functions.styleAsErrorMessage("Le choix '%s' est invalide. Veuillez choisir une option entre 1 et %s (inclus).%n%n"), input, this.options.size());
                    }
                } else {
                    selectedOption = this.options.get(choice - 1);
                }
            } while (!isValidChoice);

            this.afterValidInput(input, choice);

            if (selectedOption.getOutcome() instanceof MenuOptionOutcomeAction menuOptionOutcomeAction) {
                menuOptionOutcomeAction.execute();
            }

            if (selectedOption.getOutcome() instanceof LeadableMenuOptionOutcome leadableMenuOptionOutcome) {
                return new OptionedMenuMenuLeadTo(choice, leadableMenuOptionOutcome.getLeadingChoice());
            }

            return new OptionedMenuMenuLeadTo(choice);
        } else {
            System.out.printf(Functions.styleAsErrorMessage("Aucune option attribuée à ce menu à choix%n%n"));
        }

        return new OptionedMenuMenuLeadTo(-1);
    }

}
