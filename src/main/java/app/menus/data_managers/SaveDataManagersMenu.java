package app.menus.data_managers;

import app.models.managers.DataManager;
import app.models.managers.DataManagers;
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
            return new MenuLeadTo("main");
        }

        StandardMenu menu = new StandardMenu("Sauvegarde de gestionnaires de données");

        for (DataManager<?> unsavedDataManager : unsavedDataManagers) {
            menu.addOption(unsavedDataManager.getModelSimpleName(), new SaveDataManager(unsavedDataManager));
        }

        menu.addOption("Retour", "main");

        return menu.use();
    }

}
