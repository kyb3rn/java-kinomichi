package app.menus.addresses;

import app.models.ModelException;
import app.models.managers.AddressDataManager;
import app.models.managers.DataManagerException;
import app.models.managers.DataManagers;
import app.models.managers.LoadDataManagerDataException;
import utils.io.menus.StandardMenu;

public class ManageAddressesMenu extends StandardMenu {

    // ─── Constructors ─── //

    public ManageAddressesMenu() {
        try {
            AddressDataManager addressDataManager = DataManagers.get(AddressDataManager.class);

            String unsavedIcon = addressDataManager.hasUnsavedChanges() ? " (!)" : "";

            this.setTitle("Kinomichi - Gestion des adresses (%d)%s".formatted(addressDataManager.count(), unsavedIcon));

            this.addOption("Liste des adresses", "addresses.list");
            this.addOption("Retour", "main");
        } catch (DataManagerException | ModelException e) {
            throw new RuntimeException(e);
        }
    }

}
