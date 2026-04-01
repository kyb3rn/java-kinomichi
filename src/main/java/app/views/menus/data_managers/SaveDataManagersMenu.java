package app.views.menus.data_managers;

import app.AppState;
import app.models.managers.DataManager;
import app.models.managers.DataManagers;
import utils.io.helpers.Functions;
import utils.io.menus.MenuLeadTo;
import utils.io.menus.MenuStage;
import utils.io.menus.StandardMenu;

import java.util.List;

public class SaveDataManagersMenu extends MenuStage {

    // ─── Overrides & inheritance ─── //

    @Override
    public MenuLeadTo use() {
        List<DataManager<?>> unsavedDataManagers = DataManagers.getUnsavedOnes();

        if (unsavedDataManagers.isEmpty()) {
            System.out.println(Functions.styleAsErrorMessage("Cette page est inaccessible. Tous les gestionnaires de données sont sauvegardés. Retour à la page précédente."));
            return AppState.navigationHistory.goBack();
        }

        StandardMenu menu = new StandardMenu("Sauvegarde de gestionnaires de données");

        for (DataManager<?> unsavedDataManager : unsavedDataManagers) {
            menu.addOption(unsavedDataManager.getModelSimpleName(), new SaveDataManager(unsavedDataManager));
        }

        return menu.use();
    }

}
