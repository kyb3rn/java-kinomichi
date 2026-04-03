package app.controllers;

import app.events.CallUrlEvent;
import app.events.Event;
import app.events.FormResultEvent;
import app.events.GoBackEvent;
import app.models.Address;
import app.models.Camp;
import app.models.ModelException;
import app.models.managers.AddressDataManager;
import app.models.managers.CampDataManager;
import app.models.managers.DataManagerException;
import app.models.formatting.table.UnimplementedModelTableException;
import app.models.managers.DataManagers;
import app.routing.Request;
import app.views.ModelListView;
import app.views.camps.AddCampFormData;
import app.views.camps.AddCampView;
import app.views.camps.ManageCampView;
import app.views.camps.SelectCampView;
import utils.io.commands.list.SortColumnCommand;
import utils.io.helpers.Functions;
import utils.io.helpers.tables.SimpleBox;
import utils.io.helpers.texts.formatting.TextFormatter;

import java.util.List;
import java.util.LinkedHashMap;

public class CampController extends Controller {

    // ─── Utility methods ─── //

    public Event manageCamp(Request request) {
        int campId = Integer.parseInt(request.getParameter("id"));
        ManageCampView manageCampView = new ManageCampView(campId);
        return manageCampView.render();
    }

    public Event add(Request request) {
        AddCampView addCampView = new AddCampView();
        Event event = addCampView.render();

        if (event instanceof FormResultEvent<?> formResultEvent && formResultEvent.getResult() instanceof AddCampFormData addCampFormData) {
            try {
                Address address = DataManagers.get(AddressDataManager.class).addAddress(addCampFormData.addressData());
                addCampFormData.campData().setAddressId(address.getId());
                Camp camp = DataManagers.get(CampDataManager.class).addCamp(addCampFormData.campData());

                SimpleBox campAddedSimpleBox = new SimpleBox();
                campAddedSimpleBox.addLine(TextFormatter.bold(TextFormatter.magenta("# Stage ajouté")));
                campAddedSimpleBox.addLine(TextFormatter.italic("Le stage a bien été enregistré sous l'identifiant " + TextFormatter.bold("#" + camp.getId())));

                System.out.println();
                campAddedSimpleBox.display();
            } catch (DataManagerException | ModelException e) {
                System.out.println(Functions.styleAsErrorMessage(e.getMessage()));
            }

            return new CallUrlEvent("/");
        }

        return event;
    }

    public Event select(Request request) {
        CampDataManager campDataManager;
        try {
            campDataManager = DataManagers.get(CampDataManager.class);
        } catch (DataManagerException | ModelException e) {
            System.out.println(Functions.styleAsErrorMessage("Les stages n'ont pas pu être chargés dans l'application."));
            return new CallUrlEvent("/explore");
        }

        LinkedHashMap<Integer, SortColumnCommand.SortOrder> sortOrders = this.parseSortParameter(request);
        List<Camp> sortedCamps;

        try {
            sortedCamps = this.sortModels(campDataManager.getModels(), Camp.class, sortOrders);
        } catch (UnimplementedModelTableException e) {
            System.out.println(Functions.styleAsErrorMessage(e.getMessage()));
            return new CallUrlEvent("/explore");
        }

        SelectCampView selectCampView = new SelectCampView(sortedCamps, campDataManager);
        Event event = selectCampView.render();

        if (event instanceof FormResultEvent<?> formResultEvent && formResultEvent.getResult() instanceof Integer campId) {
            return new CallUrlEvent("/camps/manage/" + campId);
        }

        return event;
    }

    public Event list(Request request) {
        CampDataManager campDataManager;
        try {
            campDataManager = DataManagers.get(CampDataManager.class);
        } catch (DataManagerException | ModelException e) {
            System.out.println(Functions.styleAsErrorMessage("Les données des stages n'ont pas pu être chargées."));
            return new CallUrlEvent("/explore");
        }

        LinkedHashMap<Integer, SortColumnCommand.SortOrder> sortOrders = this.parseSortParameter(request);
        List<Camp> sortedCamps;

        try {
            sortedCamps = this.sortModels(campDataManager.getModels(), Camp.class, sortOrders);
        } catch (UnimplementedModelTableException e) {
            System.out.println(Functions.styleAsErrorMessage(e.getMessage()));
            return new CallUrlEvent("/explore");
        }

        ModelListView<Camp> campListView = new ModelListView<>(Camp.class, sortedCamps, campDataManager.hasUnsavedChanges(), "/camps/list");
        return campListView.render();
    }

}
