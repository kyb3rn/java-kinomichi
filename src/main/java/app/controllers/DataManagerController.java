package app.controllers;

import app.events.Event;
import app.models.ModelException;
import app.models.managers.DataManager;
import app.models.managers.DataManagerException;
import app.models.managers.DataManagers;
import app.routing.Request;
import app.views.data_managers.ReInitDataManagersView;
import app.views.data_managers.SaveDataManagersView;
import utils.io.helpers.texts.formatting.TextFormatter;

import java.util.List;

public class DataManagerController extends Controller {

    // ─── Utility methods ─── //

    public Event reinit(Request request) {
        String managerClassName = request.getParameter("manager");

        if (managerClassName != null && !managerClassName.isBlank()) {
            DataManager<?> targetDataManager = this.findManagerByClassName(DataManagers.getBadlyInitializedOnes(), managerClassName);

            if (targetDataManager != null) {
                @SuppressWarnings("unchecked")
                Class<? extends DataManager<?>> dataManagerClass = (Class<? extends DataManager<?>>) targetDataManager.getClass();
                DataManagers.initAndResolveReferencesOf(dataManagerClass);

                String modelSimpleName = targetDataManager.getModelSimpleName();
                if (targetDataManager.isInitialized()) {
                    String successMessage = DataManagers.hasDependencies(targetDataManager)
                            ? "Le gestionnaire '%s' et ses dépendances ont été ré-initialisés avec succès !".formatted(modelSimpleName)
                            : "Le gestionnaire '%s' a été ré-initialisé avec succès !".formatted(modelSimpleName);
                    System.out.printf("%s%n", TextFormatter.green(successMessage));
                }
            }
        }

        List<DataManager<?>> badlyInitializedDataManagers = DataManagers.getBadlyInitializedOnes();
        ReInitDataManagersView reInitDataManagersView = new ReInitDataManagersView(badlyInitializedDataManagers);
        return reInitDataManagersView.render();
    }

    public Event save(Request request) {
        String managerClassName = request.getParameter("manager");

        if (managerClassName != null && !managerClassName.isBlank()) {
            DataManager<?> targetDataManager = this.findManagerByClassName(DataManagers.getUnsavedOnes(), managerClassName);

            if (targetDataManager != null) {
                String modelSimpleName = targetDataManager.getModelSimpleName();
                try {
                    targetDataManager.export();
                    System.out.printf("%s%n", TextFormatter.green("Le gestionnaire '%s' a été sauvegardé avec succès !".formatted(modelSimpleName)));
                } catch (DataManagerException | ModelException e) {
                    System.out.printf("%s%n", TextFormatter.red("Erreur lors de la sauvegarde de '%s' : %s".formatted(modelSimpleName, e.getMessage())));
                }
            }
        }

        List<DataManager<?>> unsavedDataManagers = DataManagers.getUnsavedOnes();
        SaveDataManagersView saveDataManagersView = new SaveDataManagersView(unsavedDataManagers);
        return saveDataManagersView.render();
    }

    private DataManager<?> findManagerByClassName(List<DataManager<?>> dataManagers, String className) {
        return dataManagers.stream()
                .filter(dataManager -> dataManager.getClass().getSimpleName().equals(className))
                .findFirst()
                .orElse(null);
    }

}
