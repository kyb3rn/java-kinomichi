package app.controllers;

import app.events.CallUrlEvent;
import app.events.Event;
import app.events.GoBackEvent;
import app.models.Country;
import app.models.ModelException;
import app.models.managers.CountryDataManager;
import app.models.managers.DataManagerException;
import app.models.managers.DataManagers;
import app.routing.Request;
import app.views.ModelListView;
import utils.io.commands.list.SortColumnCommand;
import utils.io.helpers.Functions;

import java.util.List;
import java.util.LinkedHashMap;

public class CountryController extends Controller {

    // ─── Utility methods ─── //

    public Event list(Request request) {
        CountryDataManager countryDataManager;
        try {
            countryDataManager = DataManagers.get(CountryDataManager.class);
        } catch (DataManagerException | ModelException e) {
            System.out.println(Functions.styleAsErrorMessage("Les données des pays n'ont pas pu être chargées."));
            return new CallUrlEvent("/");
        }

        LinkedHashMap<Integer, SortColumnCommand.SortOrder> sortOrders = this.parseSortParameter(request);
        List<Country> sortedCountries = this.sortModels(countryDataManager.getModels(), Country.class, sortOrders);

        ModelListView<Country> countryListView = new ModelListView<>(Country.class, sortedCountries, countryDataManager.hasUnsavedChanges(), "/countries/list");
        return countryListView.render();
    }

}
