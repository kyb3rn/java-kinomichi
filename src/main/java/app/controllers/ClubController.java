package app.controllers;

import app.events.CallUrlEvent;
import app.events.Event;
import app.events.FormResultEvent;
import app.models.Address;
import app.models.Club;
import app.models.ModelException;
import app.models.NoResultForPrimaryKeyException;
import app.models.managers.AddressDataManager;
import app.models.managers.ClubDataManager;
import app.models.managers.DataManagerException;
import app.models.formatting.table.UnimplementedModelTableException;
import app.models.managers.DataManagers;
import app.routing.Request;
import app.views.ModelListView;
import app.views.clubs.AddClubFormData;
import app.views.clubs.AddClubView;
import app.views.clubs.ClubsDashboardView;
import app.views.clubs.DeleteClubView;
import app.views.clubs.ModifyClubFormData;
import app.views.clubs.ModifyClubView;
import app.views.clubs.SelectClubView;
import utils.io.commands.list.SortColumnCommand;
import utils.helpers.Functions;
import utils.io.tables.SimpleBox;
import utils.io.text_formatting.TextFormatter;

import java.util.List;
import java.util.LinkedHashMap;

public class ClubController extends Controller {

    // ─── Utility methods ─── //

    public Event dashboard(Request request) {
        ClubDataManager clubDataManager;
        try {
            clubDataManager = DataManagers.get(ClubDataManager.class);
        } catch (DataManagerException | ModelException e) {
            System.out.println(Functions.styleAsErrorMessage("Les données des clubs n'ont pas pu être chargées."));
            return new CallUrlEvent("/");
        }

        ClubsDashboardView clubsDashboardView = new ClubsDashboardView(clubDataManager.count(), clubDataManager.hasUnsavedChanges());
        return clubsDashboardView.render();
    }

    public Event add(Request request) {
        AddClubView addClubView = new AddClubView();
        Event event = addClubView.render();

        if (event instanceof FormResultEvent<?> formResultEvent && formResultEvent.getResult() instanceof AddClubFormData addClubFormData) {
            try {
                AddressDataManager addressDataManager = DataManagers.get(AddressDataManager.class);
                ClubDataManager clubDataManager = DataManagers.get(ClubDataManager.class);

                Address address = addClubFormData.address();
                addressDataManager.addAddress(address);

                Club club = addClubFormData.club();
                club.setAddress(address);
                clubDataManager.addClub(club);

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

    public Event modifySelect(Request request) {
        ClubDataManager clubDataManager;
        try {
            clubDataManager = DataManagers.get(ClubDataManager.class);
        } catch (DataManagerException | ModelException e) {
            System.out.println(Functions.styleAsErrorMessage("Les données des clubs n'ont pas pu être chargées."));
            return new CallUrlEvent("/clubs/dashboard");
        }

        LinkedHashMap<Integer, SortColumnCommand.SortOrder> sortOrders = this.parseSortParameter(request);
        List<Club> sortedClubs;

        try {
            sortedClubs = this.sortModels(clubDataManager.getModels(), Club.class, sortOrders);
        } catch (UnimplementedModelTableException e) {
            System.out.println(Functions.styleAsErrorMessage(e.getMessage()));
            return new CallUrlEvent("/clubs/dashboard");
        }

        SelectClubView selectClubView = new SelectClubView(sortedClubs, clubDataManager);
        Event event = selectClubView.render();

        if (event instanceof FormResultEvent<?> formResultEvent && formResultEvent.getResult() instanceof Integer clubId) {
            return new CallUrlEvent("/clubs/modify/" + clubId);
        }

        return event;
    }

    public Event modify(Request request) {
        int clubId = Integer.parseInt(request.getParameter("id"));

        ClubDataManager clubDataManager;
        try {
            clubDataManager = DataManagers.get(ClubDataManager.class);
        } catch (DataManagerException | ModelException e) {
            System.out.println(Functions.styleAsErrorMessage("Les données des clubs n'ont pas pu être chargées."));
            return new CallUrlEvent("/clubs/dashboard");
        }

        Club club;
        try {
            club = clubDataManager.getClubWithExceptions(clubId);
        } catch (DataManagerException | ModelException e) {
            System.out.println(Functions.styleAsErrorMessage(e.getMessage()));
            return new CallUrlEvent("/clubs/dashboard");
        }

        ModifyClubView modifyClubView = new ModifyClubView(club);
        Event event = modifyClubView.render();

        if (event instanceof FormResultEvent<?> formResultEvent && formResultEvent.getResult() instanceof ModifyClubFormData modifyClubFormData) {
            try {
                Address modifiedAddress = modifyClubFormData.modifiedAddress();
                DataManagers.get(AddressDataManager.class).updateAddress(modifiedAddress.getId(), modifiedAddress);

                clubDataManager.updateClub(clubId, modifyClubFormData.modifiedClub());

                SimpleBox clubModifiedSimpleBox = new SimpleBox();
                clubModifiedSimpleBox.addLine(TextFormatter.bold(TextFormatter.magenta("# Club modifié")));
                clubModifiedSimpleBox.addLine(TextFormatter.italic("Le club " + TextFormatter.bold("#" + clubId) + " a bien été modifié"));

                System.out.println();
                clubModifiedSimpleBox.display();
            } catch (DataManagerException | ModelException e) {
                System.out.println(Functions.styleAsErrorMessage(e.getMessage()));
            }

            return new CallUrlEvent("/clubs/dashboard");
        }

        return event;
    }

    public Event delete(Request request) {
        ClubDataManager clubDataManager;
        try {
            clubDataManager = DataManagers.get(ClubDataManager.class);
        } catch (DataManagerException | ModelException e) {
            System.out.println(Functions.styleAsErrorMessage("Les données des clubs n'ont pas pu être chargées."));
            return new CallUrlEvent("/clubs/dashboard");
        }

        LinkedHashMap<Integer, SortColumnCommand.SortOrder> sortOrders = this.parseSortParameter(request);
        List<Club> sortedClubs;

        try {
            sortedClubs = this.sortModels(clubDataManager.getModels(), Club.class, sortOrders);
        } catch (UnimplementedModelTableException e) {
            System.out.println(Functions.styleAsErrorMessage(e.getMessage()));
            return new CallUrlEvent("/clubs/dashboard");
        }

        DeleteClubView deleteClubView = new DeleteClubView(sortedClubs, clubDataManager);
        Event event = deleteClubView.render();

        if (event instanceof FormResultEvent<?> formResultEvent && formResultEvent.getResult() instanceof Integer clubId) {
            try {
                Club clubToDelete = clubDataManager.getClubWithExceptions(clubId);
                clubDataManager.deleteClub(clubId);

                SimpleBox clubDeletedSimpleBox = new SimpleBox();
                clubDeletedSimpleBox.addLine(TextFormatter.bold(TextFormatter.magenta("# Club supprimé")));
                clubDeletedSimpleBox.addLine(TextFormatter.italic("Le club " + TextFormatter.bold(clubToDelete.toString()) + " a bien été supprimé."));

                System.out.println();
                clubDeletedSimpleBox.display();
            } catch (ModelException | DataManagerException e) {
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
            return new CallUrlEvent("/explore");
        }

        LinkedHashMap<Integer, SortColumnCommand.SortOrder> sortOrders = this.parseSortParameter(request);
        List<Club> sortedClubs;

        try {
            sortedClubs = this.sortModels(clubDataManager.getModels(), Club.class, sortOrders);
        } catch (UnimplementedModelTableException e) {
            System.out.println(Functions.styleAsErrorMessage(e.getMessage()));
            return new CallUrlEvent("/explore");
        }

        ModelListView<Club> clubListView = new ModelListView<>(Club.class, sortedClubs, clubDataManager.hasUnsavedChanges(), "/clubs/list", "/clubs/dashboard");
        return clubListView.render();
    }

}
