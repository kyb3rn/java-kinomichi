package app.controllers;

import app.events.CallUrlEvent;
import app.events.Event;
import app.events.ReInitDataManagerEvent;
import app.events.SaveDataManagerEvent;
import app.models.ModelException;
import app.models.managers.DataManager;
import app.models.managers.DataManagerException;
import app.models.managers.DataManagers;
import app.routing.Request;
import app.views.data_managers.ReInitDataManagersView;
import app.views.data_managers.SaveDataManagersView;
import utils.io.helpers.Functions;
import utils.io.helpers.texts.formatting.TextFormatter;

import java.util.List;

public class DataManagerController extends Controller {

    // ─── Utility methods ─── //

    @SuppressWarnings("unchecked")
    public Event reinit(Request request) {
        List<DataManager<?>> badlyInitializedDataManagers = DataManagers.getBadlyInitializedOnes();
        ReInitDataManagersView reInitDataManagersView = new ReInitDataManagersView(badlyInitializedDataManagers);

        Event event = reInitDataManagersView.render();

        if (event instanceof ReInitDataManagerEvent reInitDataManagerEvent) {
            DataManager<?> targetDataManager = reInitDataManagerEvent.getDataManager();

            DataManagers.initAndResolveReferencesOf((Class<? extends DataManager<?>>) targetDataManager.getClass());

            String modelSimpleName = targetDataManager.getModelSimpleName();
            if (targetDataManager.isInitialized()) {
                String successMessage = DataManagers.hasDependencies(targetDataManager)
                    ? "Le gestionnaire de données '%s' et ses dépendances ont été ré-initialisés avec succès !".formatted(modelSimpleName)
                    : "Le gestionnaire de données '%s' a été ré-initialisé avec succès !".formatted(modelSimpleName);
                System.out.printf("%s%n", TextFormatter.green(successMessage));
            } else {
                System.out.println(Functions.styleAsErrorMessage("Le gestionnaire de données '%s' n'a pas pu être ré-initialisé.".formatted(modelSimpleName)));
            }

            return new CallUrlEvent(DataManagers.getBadlyInitializedOnes().isEmpty() ? "/" : "/data-managers/reinit");
        }

        return event;
    }

    public Event save(Request request) {
        List<DataManager<?>> unsavedDataManagers = DataManagers.getUnsavedOnes();
        SaveDataManagersView saveDataManagersView = new SaveDataManagersView(unsavedDataManagers);

        Event event = saveDataManagersView.render();

        if (event instanceof SaveDataManagerEvent saveDataManagerEvent) {
            DataManager<?> targetDataManager = saveDataManagerEvent.getDataManager();
            String modelSimpleName = targetDataManager.getModelSimpleName();

            try {
                targetDataManager.export();
                System.out.printf("%s%n", TextFormatter.green("Le gestionnaire de données '%s' a été sauvegardé avec succès !".formatted(modelSimpleName)));
            } catch (DataManagerException | ModelException e) {
                System.out.printf("%s%n", TextFormatter.red("Erreur lors de la sauvegarde de '%s' : %s".formatted(modelSimpleName, e.getMessage())));
            }

            return new CallUrlEvent(DataManagers.getUnsavedOnes().isEmpty() ? "/" : "/data-managers/save");
        }

        return event;
    }

}
