package app.menus.camps;

import app.AppState;
import app.models.Camp;
import app.models.ModelException;
import app.models.formatting.ModelTableFormatter;
import app.models.managers.CampDataManager;
import app.models.managers.DataManagerException;
import app.models.managers.DataManagers;
import utils.io.commands.*;
import utils.io.helpers.Functions;
import utils.io.helpers.texts.formatting.TextFormatter;
import utils.io.menus.MenuLeadTo;
import utils.io.menus.MenuStage;

import java.util.Comparator;
import java.util.List;
import java.util.Scanner;

public class SelectCampMenu extends MenuStage {

    // ─── Overrides & inheritance ─── //

    @Override
    public MenuLeadTo use() {
        CampDataManager campDataManager;
        try {
            campDataManager = DataManagers.get(CampDataManager.class);
        } catch (DataManagerException | ModelException e) {
            System.out.printf(Functions.styleAsErrorMessage("%nLes stages n'ont pas pu être chargés dans l'application.%n%n"));
            return new MenuLeadTo("main");
        }

        int columnCount = ModelTableFormatter.getColumnCount(Camp.class);
        int sortColumnIndex = 0;
        Scanner scanner = new Scanner(System.in);

        while (true) {
            Comparator<Camp> comparator = ModelTableFormatter.comparatorForColumn(Camp.class, sortColumnIndex);
            List<Camp> sortedCamps = campDataManager.getCamps().values().stream().sorted(comparator).toList();

            ModelTableFormatter.forList(sortedCamps).display();

            System.out.println(TextFormatter.italic("Entrez l'identifiant (#) d'un stage pour le sélectionner."));
            System.out.print("> ");
            String input = scanner.nextLine().strip();
            System.out.println();

            try {
                Command command = CommandManager.convertInput(input);

                switch (command.getCommand()) {
                    case BACK -> {
                        return new MenuLeadTo("camps.manage");
                    }
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
                int campId;
                try {
                    campId = Integer.parseInt(input);
                } catch (NumberFormatException e) {
                    System.out.printf(Functions.styleAsErrorMessage("L'entrée '%s' est invalide. Veuillez entrer un identifiant de stage.%n%n"), input);
                    continue;
                }

                Camp selectedCamp = campDataManager.getCamp(campId);

                if (selectedCamp == null) {
                    System.out.printf(Functions.styleAsErrorMessage("Aucun stage ne porte l'identifiant '%d'.%n%n"), campId);
                    continue;
                }

                AppState.selectedCampId = campId;
                System.out.printf("%s%n%n", TextFormatter.green(TextFormatter.italic("Stage sélectionné : ", TextFormatter.bold(selectedCamp.toString()))));
                return new MenuLeadTo("camps.manage.camp");
            } catch (UnknownCommandException e) {
                System.out.printf(Functions.styleAsErrorMessage("Cette commande n'existe pas.%n%n"));
            } catch (CommandArgumentException e) {
                System.out.printf(Functions.styleAsErrorMessage("Les arguments de cette commande sont invalides.%n%n"));
            }
        }
    }

}
