package app.views.menus.data_managers;

import app.AppState;
import app.models.managers.DataManager;
import app.models.managers.DataManagers;
import utils.io.helpers.Functions;
import utils.io.menus.MenuLeadTo;
import utils.io.menus.MenuStage;
import utils.io.menus.StandardMenu;

import java.util.List;

public class ReInitDataManagersMenu extends MenuStage {

    // ─── Overrides & inheritance ─── //

    @Override
    public MenuLeadTo use() {
        List<DataManager<?>> badlyInitializedDataManagers = DataManagers.getBadlyInitializedOnes();

        if (badlyInitializedDataManagers.isEmpty()) {
            System.out.println(Functions.styleAsErrorMessage("Cette page est inaccessible. Tous les gestionnaires de données sont initialisés. Retour à la page précédente."));
            return AppState.navigationHistory.goBack();
        }

        StandardMenu menu = new StandardMenu("Re-initialisation de gestionnaires de données");

        for (DataManager<?> badlyInitializedDataManager : badlyInitializedDataManagers) {
            String modelSimpleName = badlyInitializedDataManager.getModelSimpleName();
            String label = DataManagers.hasDependencies(badlyInitializedDataManager) ? modelSimpleName + " et ses dépendances" : modelSimpleName;
            menu.addOption(label, new ReInitDataManager(badlyInitializedDataManager));
        }

        return menu.use();
    }

}
