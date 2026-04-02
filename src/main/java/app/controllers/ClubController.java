package app.controllers;

import app.events.CallUrlEvent;
import app.events.Event;
import app.events.FormResultEvent;
import app.events.GoBackEvent;
import app.models.Address;
import app.models.Club;
import app.models.ModelException;
import app.models.managers.AddressDataManager;
import app.models.managers.ClubDataManager;
import app.models.managers.DataManagerException;
import app.models.managers.DataManagers;
import app.routing.Request;
import app.views.ModelListView;
import app.views.clubs.AddClubFormData;
import app.views.clubs.AddClubView;
import app.views.clubs.ClubsDashboardView;
import utils.io.commands.list.SortColumnCommand;
import utils.io.helpers.Functions;
import utils.io.helpers.tables.SimpleBox;
import utils.io.helpers.texts.formatting.TextFormatter;

import java.util.List;
import java.util.LinkedHashMap;

public class ClubController extends Controller {

    // ─── Utility methods ─── //

    public Event dashboard(Request request) {
        ClubDataManager clubDataManager;
        try {
            clubDataManager = DataManagers.get(ClubDataManager.class);
        } catch (DataManagerException | ModelException e) {
            System.out.println(Functions.styleAsErrorMessage("Impossible d'initialiser et récupérer le manager 'ClubDataManager'."));
            return new GoBackEvent();
        }

        ClubsDashboardView clubsDashboardView = new ClubsDashboardView(clubDataManager.count(), clubDataManager.hasUnsavedChanges());
        return clubsDashboardView.render();
    }

    public Event add(Request request) {
        AddClubView addClubView = new AddClubView();
        Event event = addClubView.render();

        if (event instanceof FormResultEvent<?> formResultEvent && formResultEvent.getResult() instanceof AddClubFormData addClubFormData) {
            try {
                Address address = DataManagers.get(AddressDataManager.class).addAddress(addClubFormData.addressData());
                addClubFormData.clubData().setAddressId(address.getId());
                Club club = DataManagers.get(ClubDataManager.class).addClub(addClubFormData.clubData());

                SimpleBox clubAddedSimpleBox = new SimpleBox();
                clubAddedSimpleBox.addLine(TextFormatter.bold(TextFormatter.magenta("# Club ajouté")));
                clubAddedSimpleBox.addLine(TextFormatter.italic("Le club a bien été enregistré sous l'identifiant " + TextFormatter.bold("#" + club.getId())));

                System.out.println();
                clubAddedSimpleBox.display();
            } catch (DataManagerException | ModelException e) {
                System.out.println(Functions.styleAsErrorMessage(e.getMessage()));
            }

            return new CallUrlEvent("/clubs/dashboard");
        }

        return event;
    }

    public Event list(Request request) {
        ClubDataManager clubDataManager;
        try {
            clubDataManager = DataManagers.get(ClubDataManager.class);
        } catch (DataManagerException | ModelException e) {
            System.out.println(Functions.styleAsErrorMessage("Les données des clubs n'ont pas pu être chargées."));
            return new GoBackEvent();
        }

        LinkedHashMap<Integer, SortColumnCommand.SortOrder> sortOrders = this.parseSortParameter(request);
        List<Club> sortedClubs = this.sortModels(clubDataManager.getModels(), Club.class, sortOrders);

        ModelListView<Club> clubListView = new ModelListView<>(Club.class, sortedClubs, clubDataManager.hasUnsavedChanges(), "/clubs/list");
        return clubListView.render();
    }

}
