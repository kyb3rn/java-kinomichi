package app.menus.data_managers;

import app.models.managers.DataManager;
import app.models.managers.DataManagers;
import utils.io.helpers.texts.formatting.TextFormatter;
import utils.io.menus.MenuStage;
import utils.io.menus.StandardMenu;

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
            String label = DataManagers.hasDependencies(manager) ? name + " et ses dépendances" : name;
            menu.addOption(label, name);
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
                    String successMessage = DataManagers.hasDependencies(manager)
                            ? "Le gestionnaire '%s' et ses dépendances ont été ré-initialisés avec succès !".formatted(name)
                            : "Le gestionnaire '%s' a été ré-initialisé avec succès !".formatted(name);
                    System.out.printf("%s%n%n", TextFormatter.green(successMessage));
                }

                break;
            }
        }

        return "data_managers.reinit";
    }

}
