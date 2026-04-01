package utils.io.menus;

import app.utils.menus.InvalidInputFormMenuException;
import utils.io.helpers.Functions;

import java.util.Scanner;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicReference;

public abstract class OrderedMenu extends Menu {

    // ─── Properties ─── //

    protected final TreeMap<Integer, String> unoptionedRows = new TreeMap<>();
    protected final TreeSet<Integer> sectionSeparationIndexes = new TreeSet<>();

    // ─── Utility methods ─── //

    public void addOption(String text) {
        this.addOption(text, null);
    }

    public void addOption(String text, Object response) {
        int order = this.options.size() + 1;
        this.options.add(new OrderedMenuOption(order, text, response));
    }

    private boolean isChoiceValid(String input) {
        for (MenuOption option : this.options) {
            if (option.getText().equals(input)) {
                return true;
            }
        }

        return false;
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

                int parsedChoice;
                try {
                    parsedChoice = Integer.parseInt(input);
                } catch (NumberFormatException e) {
                    throw new InvalidInputFormMenuException("L'entrée '%s' est invalide. Veuillez entrer un nombre entier strictement positif.".formatted(input), e);
                }

                int optionsSize = this.options.size();
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
                selectedOption.set((OrderedMenuOption) this.options.get(parsedChoice - 1));

                break;
            } catch (InvalidInputFormMenuException e) {
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
