package utils.io.menus;

import app.AppState;
import app.middlewares.Middleware;
import app.utils.ExitProgramException;
import app.views.utils.ExitCommandHandlerException;
import app.views.utils.FormMenuStage;
import app.views.utils.GoBackException;
import app.views.utils.InvalidInputFormMenuException;
import utils.io.commands.UnhandledCommandException;
import utils.io.helpers.Functions;

import java.util.ArrayList;
import java.util.Scanner;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public abstract class OptionedMenuStage extends FormMenuStage {

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
    public MenuLeadTo use() {
        for (Middleware middleware : this.middlewares) {
            MenuLeadTo menuLeadTo = middleware.verify();
            if (menuLeadTo != null) {
                System.out.println(Functions.styleAsErrorMessage("Ce menu n'est pas accessible."));
                return new OptionedMenuMenuLeadTo(-1, menuLeadTo);
            }
        }

        this.beforeDisplay();

        System.out.println();
        this.display();

        this.afterDisplay();

        int optionsSize = this.options.size();
        if (optionsSize > 0) {
            Scanner scanner = new Scanner(System.in);
            AtomicInteger choice = new AtomicInteger(-1);
            AtomicReference<MenuOption> selectedOption = new AtomicReference<>();
            AtomicReference<String> validInput = new AtomicReference<>();

            try {
                this.promptInput(scanner, (_, command) -> {
                    switch (command.getCommand()) {
                        case QUIT -> throw new ExitProgramException();
                        case BACK -> throw new GoBackException();
                        default -> throw new UnhandledCommandException(command);
                    }
                }, input -> {
                    int parsedChoice;
                    try {
                        parsedChoice = Integer.parseInt(input);
                    } catch (NumberFormatException e) {
                        throw new InvalidInputFormMenuException("L'entrée '%s' est invalide. Veuillez entrer un nombre entier strictement positif.".formatted(input), e);
                    }

                    if (parsedChoice < 1 || parsedChoice > optionsSize) {
                        if (optionsSize == 1) {
                            throw new InvalidInputFormMenuException("Le choix '%s' est invalide. Seul le choix 1 est valide.".formatted(input));
                        } else if (optionsSize == 2) {
                            throw new InvalidInputFormMenuException("Le choix '%s' est invalide. Veuillez entrer soit 1, soit 2.".formatted(input));
                        } else {
                            throw new InvalidInputFormMenuException("Le choix '%s' est invalide. Veuillez choisir une option entre 1 et %s (inclus).".formatted(input, optionsSize));
                        }
                    }

                    validInput.set(input);
                    choice.set(parsedChoice);
                    selectedOption.set(this.options.get(parsedChoice - 1));
                }, this::beforeInput, this::afterEveryInput);
            } catch (GoBackException _) {
                return AppState.navigationHistory.goBack();
            } catch (ExitProgramException _) {
                return null;
            } catch (ExitCommandHandlerException _) {
            }

            this.afterValidInput(validInput.get(), choice.get());

            if (selectedOption.get() != null) {
                if (selectedOption.get().getOutcome() instanceof MenuOptionOutcomeAction menuOptionOutcomeAction) {
                    menuOptionOutcomeAction.execute();
                }

                if (selectedOption.get().getOutcome() instanceof LeadableMenuOptionOutcome leadableMenuOptionOutcome) {
                    return new OptionedMenuMenuLeadTo(choice.get(), leadableMenuOptionOutcome.getLeadingChoice());
                }
            }

            return new OptionedMenuMenuLeadTo(choice.get());
        } else {
            System.out.println(Functions.styleAsErrorMessage("Aucune option attribuée à ce menu à choix"));
        }

        return new OptionedMenuMenuLeadTo(-1);
    }

}
