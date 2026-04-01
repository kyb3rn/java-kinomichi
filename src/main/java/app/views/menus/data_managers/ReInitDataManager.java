package app.views.menus.data_managers;

import app.models.managers.DataManager;
import app.models.managers.DataManagers;
import utils.io.helpers.texts.formatting.TextFormatter;
import utils.io.menus.MenuLeadTo;
import utils.io.menus.MenuOptionOutcomeLeadingAction;

public class ReInitDataManager extends MenuOptionOutcomeLeadingAction {

    // ─── Properties ─── //

    private final DataManager<?> dataManager;

    // ─── Constructors ─── //

    public ReInitDataManager(DataManager<?> dataManager) {
        super(new MenuLeadTo("data_managers.reinit"));
        this.dataManager = dataManager;
    }

    // ─── Overrides & inheritance ─── //

    @Override
    public void execute() {
        String modelSimpleName = this.dataManager.getModelSimpleName();

        @SuppressWarnings("unchecked")
        Class<? extends DataManager<?>> clazz = (Class<? extends DataManager<?>>) this.dataManager.getClass();
        DataManagers.initAndResolveReferencesOf(clazz);

        if (this.dataManager.isInitialized()) {
            String successMessage = DataManagers.hasDependencies(this.dataManager)
                    ? "Le gestionnaire '%s' et ses dépendances ont été ré-initialisés avec succès !".formatted(modelSimpleName)
                    : "Le gestionnaire '%s' a été ré-initialisé avec succès !".formatted(modelSimpleName);
            System.out.printf("%s%n", TextFormatter.green(successMessage));
        }
    }

}
