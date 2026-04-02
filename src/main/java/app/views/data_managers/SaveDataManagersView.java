package app.views.data_managers;

import app.events.CallUrlEvent;
import app.events.Event;
import app.events.GoBackEvent;
import app.models.managers.DataManager;
import app.views.View;
import utils.io.helpers.Functions;
import utils.io.menus.MenuResponse;
import app.utils.menus.KinomichiStandardMenu;
import utils.io.menus.UnhandledMenuResponseType;

import java.util.List;

public class SaveDataManagersView extends View {

    // ─── Properties ─── //

    private final List<DataManager<?>> unsavedDataManagers;

    // ─── Constructors ─── //

    public SaveDataManagersView(List<DataManager<?>> unsavedDataManagers) {
        this.unsavedDataManagers = unsavedDataManagers;
    }

    // ─── Overrides & inheritance ─── //

    @Override
    public Event render() {
        if (this.unsavedDataManagers.isEmpty()) {
            System.out.println(Functions.styleAsErrorMessage("Cette page est inaccessible. Tous les gestionnaires de données sont sauvegardés. Retour à la page précédente."));
            return new GoBackEvent();
        }

        KinomichiStandardMenu saveMenu = new KinomichiStandardMenu("Sauvegarde de gestionnaires de données", new CallUrlEvent("/"));

        for (DataManager<?> unsavedDataManager : this.unsavedDataManagers) {
            saveMenu.addOption(unsavedDataManager.getModelSimpleName(), new CallUrlEvent("/data-managers/save/" + unsavedDataManager.getClass().getSimpleName()));
        }

        MenuResponse menuResponse = saveMenu.use();

        if (menuResponse.getResponse() instanceof Event event) {
            return event;
        } else {
            throw new UnhandledMenuResponseType();
        }
    }

}
