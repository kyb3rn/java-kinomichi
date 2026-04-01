package app.views.utils;

import app.AppState;
import app.models.Model;
import app.models.ModelException;
import app.models.formatting.ModelTableFormatter;
import app.models.managers.DataManager;
import app.models.managers.DataManagerException;
import app.models.managers.DataManagers;
import app.utils.ExitProgramException;
import utils.io.commands.*;
import utils.io.helpers.Functions;
import utils.io.helpers.texts.formatting.TextFormatter;
import utils.io.menus.MenuLeadTo;

import java.util.Comparator;
import java.util.List;
import java.util.Scanner;

public class ModelTableMenu extends FormMenuStage {

    // ─── Properties ─── //

    private final Class<? extends DataManager<?>> dataManagerClass;

    // ─── Constructors ─── //

    public ModelTableMenu(Class<? extends DataManager<?>> dataManagerClass) {
        this.dataManagerClass = dataManagerClass;
    }

    // ─── Overrides & inheritance ─── //

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public MenuLeadTo use() {
        DataManager<?> dataManager;
        try {
            dataManager = DataManagers.get(this.dataManagerClass);
        } catch (DataManagerException | ModelException _) {
            System.out.println();
            System.out.println(Functions.styleAsErrorMessage("Les données de '%s' n'ont pas pu être chargées dans l'application.".formatted(this.dataManagerClass.getSimpleName())));
            return AppState.navigationHistory.goBack();
        }

        Class<? extends Model> modelClass = dataManager.getModelClass();
        int columnCount = ModelTableFormatter.getColumnCount(modelClass);
        int[] sortColumnIndex = {0};
        Scanner scanner = new Scanner(System.in);

        while (true) {
            Comparator comparator = ModelTableFormatter.comparatorForColumn(modelClass, sortColumnIndex[0]);
            List sortedModels = dataManager.getModels().stream().sorted(comparator).toList();

            System.out.println();
            ModelTableFormatter.forList(sortedModels).display();

            if (dataManager.hasUnsavedChanges()) {
                System.out.println(TextFormatter.italic(TextFormatter.yellow(TextFormatter.bold("ATTENTION !"), " Des modifications dans cette liste n'ont pas encore été sauvegardées. Rendez-vous dans le menu principal pour résoudre ce problème.")));
            }

            try {
                this.promptInput(scanner, (_, command) -> {
                    switch (command.getCommand()) {
                        case QUIT -> throw new ExitProgramException();
                        case BACK -> throw new GoBackException();
                        case SORT -> {
                            int argumentsCount = command.getArguments().size();
                            if (argumentsCount != 1) {
                                throw new InvalidInputFormMenuException("Nombre d'arguments invalide (obtenu(s): %d, attendu(s): 1).".formatted(argumentsCount));
                            }

                            String firstArgument = command.getArguments().getFirst().getValue();
                            int columnChoice;
                            try {
                                columnChoice = Integer.parseInt(firstArgument);
                            } catch (NumberFormatException e) {
                                System.out.println(Functions.styleAsErrorMessage("L'entrée '%s' est invalide. Veuillez entrer un nombre entier strictement positif.".formatted(firstArgument)));
                                throw new ExitCommandHandlerException(e);
                            }

                            if (columnChoice < 1 || columnChoice > columnCount) {
                                System.out.println(Functions.styleAsErrorMessage("La colonne '%d' est invalide. Veuillez entrer un nombre entre 1 et %d.".formatted(columnChoice, columnCount)));
                                throw new ExitCommandHandlerException();
                            }

                            sortColumnIndex[0] = columnChoice - 1;
                            throw new ExitCommandHandlerException();
                        }
                        default -> throw new UnhandledCommandException(command);
                    }
                }, input -> System.out.printf(Functions.styleAsErrorMessage("L'entrée '%s' n'est pas une commande.%n".formatted(input))));
                break;
            } catch (GoBackException _) {
                break;
            } catch (ExitCommandHandlerException _) {
            } catch (ExitProgramException _) {
                return null;
            }
        }

        return AppState.navigationHistory.goBack();
    }

}
