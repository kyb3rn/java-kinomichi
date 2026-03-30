package app.menus.data_managers;

import app.data_management.managers.DataManager;
import app.data_management.managers.DataManagers;
import utils.io.helpers.texts.formatting.TextFormatter;
import utils.io.menus.MenuStage;
import utils.io.menus.StandardMenu;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class ReinitDataManagersMenu extends MenuStage {

    // ─── Overrides & inheritance ─── //

    @Override
    public String use() {
        List<DataManager<?>> badlyInitialized = DataManagers.getBadlyInitializedOnes();

        if (badlyInitialized.isEmpty()) {
            return "main";
        }

        StandardMenu menu = new StandardMenu("Re-initialisation de gestionnaires de données");

        for (DataManager<?> manager : badlyInitialized) {
            String name = manager.getClass().getSimpleName().replace("DataManager", "");
            menu.addOption(name, name);
        }

        menu.addOption("Retour", "main");

        String choice = menu.use();

        if ("main".equals(choice)) {
            return "main";
        }

        for (DataManager<?> manager : badlyInitialized) {
            String name = manager.getClass().getSimpleName().replace("DataManager", "");
            if (name.equals(choice)) {
                @SuppressWarnings("unchecked")
                Class<? extends DataManager<?>> clazz = (Class<? extends DataManager<?>>) manager.getClass();
                DataManagers.init(clazz);

                if (manager.isInitialized()) {
                    System.out.printf("%s%n%n", TextFormatter.green("Le gestionnaire '%s' a été ré-initialisé avec succès !".formatted(name)));
                }

                break;
            }
        }

        return "data_managers.reinit";
    }

}
