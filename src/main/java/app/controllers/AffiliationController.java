package app.controllers;

import app.events.CallUrlEvent;
import app.events.Event;
import app.models.Affiliation;
import app.models.ModelException;
import app.models.managers.AffiliationDataManager;
import app.models.managers.DataManagerException;
import app.models.formatting.table.UnimplementedModelTableException;
import app.models.managers.DataManagers;
import app.routing.Request;
import app.views.ModelListView;
import utils.io.commands.list.SortColumnCommand;
import utils.helpers.Functions;

import java.util.List;
import java.util.LinkedHashMap;

public class AffiliationController extends Controller {

    // ─── Utility methods ─── //

    public Event list(Request request) {
        AffiliationDataManager affiliationDataManager;
        try {
            affiliationDataManager = DataManagers.get(AffiliationDataManager.class);
        } catch (DataManagerException | ModelException e) {
            System.out.println(Functions.styleAsErrorMessage("Les données des affiliés n'ont pas pu être chargées."));
            return new CallUrlEvent("/explore");
        }

        LinkedHashMap<Integer, SortColumnCommand.SortOrder> sortOrders = this.parseSortParameter(request);
        List<Affiliation> sortedAffiliations;

        try {
            sortedAffiliations = this.sortModels(affiliationDataManager.getModels(), Affiliation.class, sortOrders);
        } catch (UnimplementedModelTableException e) {
            System.out.println(Functions.styleAsErrorMessage(e.getMessage()));
            return new CallUrlEvent("/explore");
        }

        ModelListView<Affiliation> affiliationListView = new ModelListView<>(Affiliation.class, sortedAffiliations, affiliationDataManager.hasUnsavedChanges(), "/affiliations/list");
        return affiliationListView.render();
    }

}
