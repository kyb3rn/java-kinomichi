package app.controllers;

import app.events.CallUrlEvent;
import app.events.Event;
import app.events.GoBackEvent;
import app.models.Address;
import app.models.ModelException;
import app.models.managers.AddressDataManager;
import app.models.managers.DataManagerException;
import app.models.managers.DataManagers;
import app.routing.Request;
import app.views.ModelListView;
import utils.io.commands.list.SortColumnCommand;
import utils.io.helpers.Functions;

import java.util.List;
import java.util.LinkedHashMap;

public class AddressController extends Controller {

    // ─── Utility methods ─── //

    public Event list(Request request) {
        AddressDataManager addressDataManager;
        try {
            addressDataManager = DataManagers.get(AddressDataManager.class);
        } catch (DataManagerException | ModelException e) {
            System.out.println(Functions.styleAsErrorMessage("Les données des adresses n'ont pas pu être chargées."));
            return new CallUrlEvent("/");
        }

        LinkedHashMap<Integer, SortColumnCommand.SortOrder> sortOrders = this.parseSortParameter(request);
        List<Address> sortedAddresses = this.sortModels(addressDataManager.getModels(), Address.class, sortOrders);

        ModelListView<Address> addressListView = new ModelListView<>(Address.class, sortedAddresses, addressDataManager.hasUnsavedChanges(), "/addresses/list");
        return addressListView.render();
    }

}
