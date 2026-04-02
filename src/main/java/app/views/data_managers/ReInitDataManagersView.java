package app.views.data_managers;

import app.events.CallUrlEvent;
import app.events.Event;
import app.events.GoBackEvent;
import app.events.ReInitDataManagerEvent;
import app.models.managers.DataManager;
import app.models.managers.DataManagers;
import app.views.View;
import utils.io.helpers.Functions;
import utils.io.menus.MenuResponse;
import app.utils.menus.KinomichiStandardMenu;
import utils.io.menus.UnhandledMenuResponseType;

import java.util.List;

public class ReInitDataManagersView extends View {

    // ─── Properties ─── //

    private final List<DataManager<?>> badlyInitializedDataManagers;

    // ─── Constructors ─── //

    public ReInitDataManagersView(List<DataManager<?>> badlyInitializedDataManagers) {
        this.badlyInitializedDataManagers = badlyInitializedDataManagers;
    }

    // ─── Overrides & inheritance ─── //

    @Override
    public Event render() {
        if (this.badlyInitializedDataManagers.isEmpty()) {
            System.out.println(Functions.styleAsErrorMessage("Cette page est inaccessible. Tous les gestionnaires de données sont initialisés."));
            return new CallUrlEvent("/");
        }

        KinomichiStandardMenu reinitMenu = new KinomichiStandardMenu("Re-initialisation de gestionnaires de données", new CallUrlEvent("/"));

        for (DataManager<?> badlyInitializedDataManager : this.badlyInitializedDataManagers) {
            String modelSimpleName = badlyInitializedDataManager.getModelSimpleName();
            String label = DataManagers.hasDependencies(badlyInitializedDataManager) ? modelSimpleName + " et ses dépendances" : modelSimpleName;
            reinitMenu.addOption(label, new ReInitDataManagerEvent(badlyInitializedDataManager));
        }

        MenuResponse menuResponse = reinitMenu.use();

        if (menuResponse.getResponse() instanceof Event event) {
            return event;
        } else {
            throw new UnhandledMenuResponseType();
        }
    }

}
