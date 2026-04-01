package app.views.menus.data_managers;

import app.models.ModelException;
import app.models.managers.DataManager;
import app.models.managers.DataManagerException;
import utils.io.helpers.texts.formatting.TextFormatter;
import utils.io.menus.MenuLeadTo;
import utils.io.menus.MenuOptionOutcomeLeadingAction;

public class SaveDataManager extends MenuOptionOutcomeLeadingAction {

    // ─── Properties ─── //

    private final DataManager<?> dataManager;

    // ─── Constructors ─── //

    public SaveDataManager(DataManager<?> dataManager) {
        super(new MenuLeadTo("data_managers.save"));
        this.dataManager = dataManager;
    }

    // ─── Overrides & inheritance ─── //

    @Override
    public void execute() {
        String modelSimpleName = this.dataManager.getModelSimpleName();

        try {
            this.dataManager.export();
            System.out.printf("%s%n", TextFormatter.green("Le gestionnaire '%s' a été sauvegardé avec succès !".formatted(modelSimpleName)));
        } catch (DataManagerException | ModelException e) {
            System.out.printf("%s%n", TextFormatter.red("Erreur lors de la sauvegarde de '%s' : %s".formatted(modelSimpleName, e.getMessage())));
        }
    }

}
