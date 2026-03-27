package utils.io.menus;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public abstract class OptionedMenuStage extends MenuStage {

    /** Properties **/

    protected final List<MenuOption> options = new ArrayList<>();

    /** Special methods **/

    public void addOption(MenuOption option) {
        this.options.add(option);
    }

    public void addOption(String text, String leadTo) {
        MenuOption option = new MenuOption(text, leadTo);
        this.options.add(option);
    }

    private boolean isChoiceValid(String input) {
        for (MenuOption option : this.options) {
            if (option.getText().equals(input)) {
                return true;
            }
        }

        return false;
    }

    /** Overrides & inheritance **/

    public abstract void display();

    @Override
    public String use() {
        int optionsSize = this.options.size();

        this.display();

        if (optionsSize > 0) {
            Scanner scanner = new Scanner(System.in);
            boolean isValidChoice = false;
            MenuOption selectedOption = null;

            do {
                System.out.print("> ");
                String input = scanner.nextLine();
                System.out.println();

                int choice;
                try {
                    choice = Integer.parseInt(input);
                } catch (NumberFormatException e) {
                    System.out.printf("%nL'entrée '%s' est invalide. Veuillez entrer un nombre entier entre 1 et %s (inclus).%n%n", input, this.options.size());
                    continue;
                }

                isValidChoice = choice > 0 && choice <= optionsSize;

                if (!isValidChoice) {
                    if (optionsSize == 1) {
                        System.out.printf("%nLe choix '%s' est invalide. Seul le choix 1 est valide.%n%n", input);
                    } else if (optionsSize == 2) {
                        System.out.printf("%nLe choix '%s' est invalide. Veuillez entrer soit 1, soit 2.%n%n", input);
                    } else {
                        System.out.printf("%nLe choix '%s' est invalide. Veuillez choisir une option entre 1 et %s (inclus).%n%n", input, this.options.size());
                    }
                } else {
                    selectedOption = this.options.get(choice - 1);
                }
            } while (!isValidChoice);

            return selectedOption.getLeadTo();
        } else {
            System.out.printf("%nAucune option attribuée à ce menu à choix%n%n");
        }

        return null;
    }

}
