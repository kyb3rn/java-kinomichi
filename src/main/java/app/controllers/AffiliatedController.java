package app.controllers;

import app.events.CallUrlEvent;
import app.events.Event;
import app.events.GoBackEvent;
import app.models.Affiliated;
import app.models.ModelException;
import app.models.managers.AffiliatedDataManager;
import app.models.managers.DataManagerException;
import app.models.managers.DataManagers;
import app.routing.Request;
import app.views.ModelListView;
import utils.io.commands.list.SortColumnCommand;
import utils.io.helpers.Functions;

import java.util.List;
import java.util.LinkedHashMap;

public class AffiliatedController extends Controller {

    // ─── Utility methods ─── //

    public Event list(Request request) {
        AffiliatedDataManager affiliatedDataManager;
        try {
            affiliatedDataManager = DataManagers.get(AffiliatedDataManager.class);
        } catch (DataManagerException | ModelException e) {
            System.out.println(Functions.styleAsErrorMessage("Les données des affiliés n'ont pas pu être chargées."));
            return new CallUrlEvent("/");
        }

        LinkedHashMap<Integer, SortColumnCommand.SortOrder> sortOrders = this.parseSortParameter(request);
        List<Affiliated> sortedAffiliateds = this.sortModels(affiliatedDataManager.getModels(), Affiliated.class, sortOrders);

        ModelListView<Affiliated> affiliatedListView = new ModelListView<>(Affiliated.class, sortedAffiliateds, affiliatedDataManager.hasUnsavedChanges(), "/affiliateds/list");
        return affiliatedListView.render();
    }

}
