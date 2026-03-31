package app.menus.countries;

import app.models.Country;
import app.models.ModelException;
import app.models.formatting.ModelTableFormatter;
import app.models.managers.CountryDataManager;
import app.models.managers.DataManagerException;
import app.models.managers.DataManagers;
import app.models.managers.LoadDataManagerDataException;
import utils.io.commands.*;
import utils.io.helpers.Functions;
import utils.io.helpers.texts.formatting.TextFormatter;
import utils.io.menus.MenuLeadTo;
import utils.io.menus.MenuStage;

import java.util.Comparator;
import java.util.List;
import java.util.Scanner;

public class ListCountriesMenu extends MenuStage {

    // ─── Overrides & inheritance ─── //

    @Override
    public MenuLeadTo use() {
        CountryDataManager countryDataManager;
        try {
            countryDataManager = DataManagers.get(CountryDataManager.class);
        } catch (DataManagerException | ModelException e) {
            System.out.printf(Functions.styleAsErrorMessage("%nLes pays n'ont pas pu être chargés dans l'application.%n%n"));
            return new MenuLeadTo("main");
        }

        int columnCount = ModelTableFormatter.getColumnCount(Country.class);
        int sortColumnIndex = 0;
        Scanner scanner = new Scanner(System.in);
        boolean quitLoop = false;

        do {
            Comparator<Country> comparator = ModelTableFormatter.comparatorForColumn(Country.class, sortColumnIndex);
            List<Country> sorted = countryDataManager.getCountries().values().stream().sorted(comparator).toList();

            ModelTableFormatter.forList(sorted).display();

            if (countryDataManager.hasUnsavedChanges()) {
                System.out.printf("%s%n%n", TextFormatter.italic(TextFormatter.yellow(TextFormatter.bold("ATTENTION !"), " Des modifications dans cette liste n'ont pas encore été sauvegardées. Rendez-vous dans le menu principal pour résoudre ce problème.")));
            }

            System.out.print("> ");
            String input = scanner.nextLine().strip();
            System.out.println();

            try {
                Command command = CommandManager.convertInput(input);

                switch (command.getCommand()) {
                    case BACK -> quitLoop = true;
                    case QUIT -> {
                        return null;
                    }
                    case SORT -> {
                        int columnChoice;
                        try {
                            columnChoice = Integer.parseInt(command.getArguments().getFirst().getValue());
                        } catch (NumberFormatException e) {
                            System.out.printf(Functions.styleAsErrorMessage("L'entrée '%s' est invalide. Veuillez entrer un nombre entier strictement positif.%n%n"), input);
                            continue;
                        }

                        if (columnChoice < 1 || columnChoice > columnCount) {
                            System.out.printf(Functions.styleAsErrorMessage("La colonne '%d' est invalide. Veuillez entrer un nombre entre 1 et %d.%n%n"), columnChoice, columnCount);
                            continue;
                        }

                        sortColumnIndex = columnChoice - 1;
                    }
                    default -> System.out.printf(Functions.styleAsErrorMessage("Cette commande n'est pas prise en charge ici.%n%n"));
                }
            } catch (NotACommandException _) {
                System.out.printf(Functions.styleAsErrorMessage("L'entrée '%s' n'est pas une commande.%n%n".formatted(input)));
            } catch (UnknownCommandException e) {
                System.out.printf(Functions.styleAsErrorMessage("Cette commande n'existe pas.%n%n"));
            } catch (CommandArgumentException e) {
                System.out.printf(Functions.styleAsErrorMessage("Les arguments de cette commande sont invalides.%n%n"));
            }
        } while (!quitLoop);

        return new MenuLeadTo("countries.manage");
    }

}
