package app.views.menus.camps;

import app.AppState;
import app.models.Camp;
import app.models.ModelException;
import app.models.formatting.ModelTableFormatter;
import app.models.managers.CampDataManager;
import app.models.managers.DataManagerException;
import app.models.managers.DataManagers;
import app.utils.ExitProgramException;
import app.views.utils.ExitCommandHandlerException;
import app.views.utils.FormMenuStage;
import app.views.utils.GoBackException;
import app.views.utils.InvalidInputFormMenuException;
import utils.io.commands.UnhandledCommandException;
import utils.io.helpers.Functions;
import utils.io.helpers.texts.formatting.TextFormatter;
import utils.io.menus.MenuLeadTo;

import java.util.Comparator;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicInteger;

public class SelectCampMenu extends FormMenuStage {

    // ─── Overrides & inheritance ─── //

    @Override
    public MenuLeadTo use() {
        CampDataManager campDataManager;
        try {
            campDataManager = DataManagers.get(CampDataManager.class);
        } catch (DataManagerException | ModelException _) {
            System.out.println(Functions.styleAsErrorMessage("Les stages n'ont pas pu être chargés dans l'application."));
            return AppState.navigationHistory.goBack();
        }

        int columnCount = ModelTableFormatter.getColumnCount(Camp.class);
        AtomicInteger sortColumnIndex = new AtomicInteger(0);
        Scanner scanner = new Scanner(System.in);

        while (true) {
            Comparator<Camp> comparator = ModelTableFormatter.comparatorForColumn(Camp.class, sortColumnIndex.get());
            List<Camp> sortedCamps = campDataManager.getCamps().values().stream().sorted(comparator).toList();

            System.out.println();
            ModelTableFormatter.forList(sortedCamps).display();

            System.out.println(TextFormatter.italic("Entrez l'identifiant (#) d'un stage pour le sélectionner."));

            try {
                this.promptInput(scanner, (_, command) -> {
                    switch (command.getCommand()) {
                        case QUIT -> throw new ExitProgramException();
                        case BACK -> throw new GoBackException();
                        case SORT -> {
                            int argumentsCount = command.getArguments().size();
                            if (argumentsCount != 1) {
                                throw new InvalidInputFormMenuException("Nombre d'arguments invalide (obtenu(s): %d, attendu(s): 1). Veuillez entrer un nombre entier strictement positif.".formatted(argumentsCount));
                            }

                            String firstArgument = command.getArguments().getFirst().getValue();
                            int columnChoice;
                            try {
                                columnChoice = Integer.parseInt(firstArgument);
                            } catch (NumberFormatException e) {
                                throw new InvalidInputFormMenuException("L'argument '%s' est invalide. Veuillez entrer un nombre entier strictement positif.".formatted(firstArgument), e);
                            }

                            if (columnChoice < 1 || columnChoice > columnCount) {
                                throw new InvalidInputFormMenuException("La colonne '%d' est invalide. Veuillez entrer un nombre entre 1 et %d.".formatted(columnChoice, columnCount));
                            }

                            sortColumnIndex.set(columnChoice - 1);
                            throw new ExitCommandHandlerException();
                        }
                        default -> throw new UnhandledCommandException(command);
                    }
                }, input -> {
                    int campId;
                    try {
                        campId = Integer.parseInt(input);
                    } catch (NumberFormatException e) {
                        throw new InvalidInputFormMenuException("L'entrée '%s' est invalide. Veuillez entrer un identifiant de stage.".formatted(input), e);
                    }

                    Camp selectedCamp = campDataManager.getCamp(campId);

                    if (selectedCamp == null) {
                        throw new InvalidInputFormMenuException("Aucun stage ne porte l'identifiant '%d'.".formatted(campId));
                    }

                    AppState.selectedCampId = campId;
                    System.out.printf("%s%n", TextFormatter.green(TextFormatter.italic("Stage sélectionné : ", TextFormatter.bold(selectedCamp.toString()))));
                });
                return new MenuLeadTo("camps.manage.camp");
            } catch (GoBackException _) {
                return AppState.navigationHistory.goBack();
            } catch (ExitCommandHandlerException _) {
            } catch (ExitProgramException _) {
                return null;
            }
        }
    }

}
