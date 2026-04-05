package app.controllers;

import app.events.CallUrlEvent;
import app.events.Event;
import app.events.FormResultEvent;
import app.models.Address;
import app.models.Camp;
import app.models.Invitation;
import app.models.ModelException;
import app.models.managers.AddressDataManager;
import app.models.managers.CampDataManager;
import app.models.managers.DataManagerException;
import app.models.managers.InvitationDataManager;
import app.models.formatting.table.UnimplementedModelTableException;
import app.models.managers.DataManagers;
import app.routing.Request;
import app.views.ModelListView;
import app.views.camps.AddCampFormData;
import app.views.camps.AddCampView;
import app.views.camps.DeleteCampView;
import app.views.camps.ManageCampView;
import app.views.camps.ModifyCampFormData;
import app.views.camps.ModifyCampView;
import app.views.camps.SelectCampView;
import app.views.invitations.AddInvitationView;
import app.views.invitations.DeleteInvitationView;
import app.views.invitations.ManageInvitationsView;
import utils.io.commands.list.SortColumnCommand;
import utils.helpers.Functions;
import utils.io.tables.SimpleBox;
import utils.io.text_formatting.TextFormatter;

import java.util.List;
import java.util.LinkedHashMap;

public class CampController extends Controller {

    // ─── Utility methods ─── //

    public Event manageInvitations(Request request) {
        int campId = Integer.parseInt(request.getParameter("campId"));
        ManageInvitationsView manageInvitationsView = new ManageInvitationsView(campId);
        return manageInvitationsView.render();
    }

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
                AddressDataManager addressDataManager = DataManagers.get(AddressDataManager.class);
                CampDataManager campDataManager = DataManagers.get(CampDataManager.class);

                Address address = addCampFormData.address();
                addressDataManager.addAddress(address);

                Camp camp = addCampFormData.camp();
                camp.setAddress(address);
                campDataManager.addCamp(camp);

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

    public Event modify(Request request) {
        int campId = Integer.parseInt(request.getParameter("id"));

        CampDataManager campDataManager;
        try {
            campDataManager = DataManagers.get(CampDataManager.class);
        } catch (DataManagerException | ModelException e) {
            System.out.println(Functions.styleAsErrorMessage("Les données des stages n'ont pas pu être chargées."));
            return new CallUrlEvent("/camps/select");
        }

        Camp camp;
        try {
            camp = campDataManager.getCampWithExceptions(campId);
        } catch (DataManagerException | ModelException e) {
            System.out.println(Functions.styleAsErrorMessage(e.getMessage()));
            return new CallUrlEvent("/camps/select");
        }

        ModifyCampView modifyCampView = new ModifyCampView(camp);
        Event event = modifyCampView.render();

        if (event instanceof FormResultEvent<?> formResultEvent && formResultEvent.getResult() instanceof ModifyCampFormData modifyCampFormData) {
            try {
                Address modifiedAddress = modifyCampFormData.modifiedAddress();
                DataManagers.get(AddressDataManager.class).updateAddress(modifiedAddress.getId(), modifiedAddress);

                campDataManager.updateCamp(campId, modifyCampFormData.modifiedCamp());

                SimpleBox campModifiedSimpleBox = new SimpleBox();
                campModifiedSimpleBox.addLine(TextFormatter.bold(TextFormatter.magenta("# Stage modifié")));
                campModifiedSimpleBox.addLine(TextFormatter.italic("Le stage " + TextFormatter.bold("#" + campId) + " a bien été modifié"));

                System.out.println();
                campModifiedSimpleBox.display();
            } catch (DataManagerException | ModelException e) {
                System.out.println(Functions.styleAsErrorMessage(e.getMessage()));
            }

            return new CallUrlEvent("/camps/manage/" + campId);
        }

        return event;
    }

    public Event delete(Request request) {
        int campId = Integer.parseInt(request.getParameter("id"));

        CampDataManager campDataManager;
        try {
            campDataManager = DataManagers.get(CampDataManager.class);
        } catch (DataManagerException | ModelException e) {
            System.out.println(Functions.styleAsErrorMessage("Les données des stages n'ont pas pu être chargées."));
            return new CallUrlEvent("/camps/select");
        }

        LinkedHashMap<Integer, SortColumnCommand.SortOrder> sortOrders = this.parseSortParameter(request);
        List<Camp> sortedCamps;

        try {
            sortedCamps = this.sortModels(campDataManager.getModels(), Camp.class, sortOrders);
        } catch (UnimplementedModelTableException e) {
            System.out.println(Functions.styleAsErrorMessage(e.getMessage()));
            return new CallUrlEvent("/camps/select");
        }

        DeleteCampView deleteCampView = new DeleteCampView(sortedCamps, campDataManager);
        Event event = deleteCampView.render();

        if (event instanceof FormResultEvent<?> formResultEvent && formResultEvent.getResult() instanceof Integer deleteCampId) {
            try {
                Camp campToDelete = campDataManager.getCampWithExceptions(deleteCampId);
                campDataManager.deleteCamp(deleteCampId);

                SimpleBox campDeletedSimpleBox = new SimpleBox();
                campDeletedSimpleBox.addLine(TextFormatter.bold(TextFormatter.magenta("# Stage supprimé")));
                campDeletedSimpleBox.addLine(TextFormatter.italic("Le stage " + TextFormatter.bold(campToDelete.toString()) + " a bien été supprimé."));

                System.out.println();
                campDeletedSimpleBox.display();
            } catch (ModelException | DataManagerException e) {
                System.out.println(Functions.styleAsErrorMessage(e.getMessage()));
            }

            return new CallUrlEvent("/camps/select");
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

    public Event listInvitations(Request request) {
        int campId = Integer.parseInt(request.getParameter("campId"));

        CampDataManager campDataManager;
        InvitationDataManager invitationDataManager;
        try {
            campDataManager = DataManagers.get(CampDataManager.class);
            invitationDataManager = DataManagers.get(InvitationDataManager.class);
        } catch (DataManagerException | ModelException e) {
            System.out.println(Functions.styleAsErrorMessage("Les données n'ont pas pu être chargées."));
            return new CallUrlEvent("/camps/manage/" + campId);
        }

        Camp camp;
        try {
            camp = campDataManager.getCampWithExceptions(campId);
        } catch (DataManagerException | ModelException e) {
            System.out.println(Functions.styleAsErrorMessage(e.getMessage()));
            return new CallUrlEvent("/camps/manage/" + campId);
        }

        List<Invitation> campInvitations;
        try {
            campInvitations = invitationDataManager.getCampInvitations(camp);
        } catch (DataManagerException e) {
            System.out.println(Functions.styleAsErrorMessage(e.getMessage()));
            return new CallUrlEvent("/camps/manage/" + campId);
        }

        LinkedHashMap<Integer, SortColumnCommand.SortOrder> sortOrders = this.parseSortParameter(request);
        List<Invitation> sortedInvitations;

        try {
            sortedInvitations = this.sortModels(campInvitations, Invitation.class, sortOrders);
        } catch (UnimplementedModelTableException e) {
            System.out.println(Functions.styleAsErrorMessage(e.getMessage()));
            return new CallUrlEvent("/camps/manage/" + campId);
        }

        ModelListView<Invitation> invitationListView = new ModelListView<>(Invitation.class, sortedInvitations, invitationDataManager.hasUnsavedChanges(), "/camps/manage/%d/invitations/list".formatted(campId), "/camps/manage/%d/invitations".formatted(campId));
        return invitationListView.render();
    }

    public Event deleteInvitation(Request request) {
        int campId = Integer.parseInt(request.getParameter("campId"));

        CampDataManager campDataManager;
        InvitationDataManager invitationDataManager;
        try {
            campDataManager = DataManagers.get(CampDataManager.class);
            invitationDataManager = DataManagers.get(InvitationDataManager.class);
        } catch (DataManagerException | ModelException e) {
            System.out.println(Functions.styleAsErrorMessage("Les données n'ont pas pu être chargées."));
            return new CallUrlEvent("/camps/manage/%d/invitations".formatted(campId));
        }

        Camp camp;
        try {
            camp = campDataManager.getCampWithExceptions(campId);
        } catch (DataManagerException | ModelException e) {
            System.out.println(Functions.styleAsErrorMessage(e.getMessage()));
            return new CallUrlEvent("/");
        }

        List<Invitation> campInvitations;
        try {
            campInvitations = invitationDataManager.getCampInvitations(camp);
        } catch (DataManagerException e) {
            System.out.println(Functions.styleAsErrorMessage(e.getMessage()));
            return new CallUrlEvent("/camps/manage/%d/invitations".formatted(campId));
        }

        LinkedHashMap<Integer, SortColumnCommand.SortOrder> sortOrders = this.parseSortParameter(request);
        List<Invitation> sortedInvitations;

        try {
            sortedInvitations = this.sortModels(campInvitations, Invitation.class, sortOrders);
        } catch (UnimplementedModelTableException e) {
            System.out.println(Functions.styleAsErrorMessage(e.getMessage()));
            return new CallUrlEvent("/camps/manage/%d/invitations".formatted(campId));
        }

        DeleteInvitationView deleteInvitationView = new DeleteInvitationView(campId, sortedInvitations, invitationDataManager);
        Event event = deleteInvitationView.render();

        if (event instanceof FormResultEvent<?> formResultEvent && formResultEvent.getResult() instanceof Integer invitationId) {
            try {
                Invitation invitationToDelete = invitationDataManager.getInvitationWithExceptions(invitationId);
                invitationDataManager.deleteInvitation(invitationId);

                SimpleBox invitationDeletedSimpleBox = new SimpleBox();
                invitationDeletedSimpleBox.addLine(TextFormatter.bold(TextFormatter.magenta("# Invitation supprimée")));
                invitationDeletedSimpleBox.addLine(TextFormatter.italic("L'invitation " + TextFormatter.bold(invitationToDelete.toString()) + " a bien été supprimée."));

                System.out.println();
                invitationDeletedSimpleBox.display();
            } catch (ModelException | DataManagerException e) {
                System.out.println(Functions.styleAsErrorMessage(e.getMessage()));
            }

            return new CallUrlEvent("/camps/manage/%d/invitations".formatted(campId));
        }

        return event;
    }

    public Event addInvitation(Request request) {
        int campId = Integer.parseInt(request.getParameter("campId"));

        AddInvitationView addInvitationView = new AddInvitationView(campId);
        Event event = addInvitationView.render();

        if (event instanceof FormResultEvent<?> formResultEvent && formResultEvent.getResult() instanceof Invitation invitationToAdd) {
            try {
                InvitationDataManager invitationDataManager = DataManagers.get(InvitationDataManager.class);
                invitationDataManager.addInvitation(invitationToAdd);

                SimpleBox invitationAddedSimpleBox = new SimpleBox();
                invitationAddedSimpleBox.addLine(TextFormatter.bold(TextFormatter.magenta("# Invitation ajoutée")));
                invitationAddedSimpleBox.addLine(TextFormatter.italic("L'invitation a bien été enregistrée sous l'identifiant " + TextFormatter.bold("#" + invitationToAdd.getId())));

                System.out.println();
                invitationAddedSimpleBox.display();
            } catch (DataManagerException | ModelException e) {
                System.out.println(Functions.styleAsErrorMessage(e.getMessage()));
            }

            return new CallUrlEvent("/camps/manage/" + campId + "/invitations");
        }

        return event;
    }

}
