package app.controllers;

import app.events.CallUrlEvent;
import app.events.Event;
import app.events.FormResultEvent;
import app.models.Camp;
import app.models.Dinner;
import app.models.DinnerReservation;
import app.models.ModelException;
import app.models.managers.CampDataManager;
import app.models.managers.DataManagerException;
import app.models.managers.DinnerDataManager;
import app.models.managers.DinnerReservationDataManager;
import app.models.formatting.table.UnimplementedModelTableException;
import app.models.managers.DataManagers;
import app.routing.Request;
import app.views.ModelListView;
import app.views.dinners.AddDinnerReservationView;
import app.views.dinners.AddDinnerView;
import app.views.dinners.DeleteDinnerReservationView;
import app.views.dinners.DeleteDinnerView;
import app.views.dinners.ManageDinnerReservationsView;
import app.views.dinners.ManageDinnersView;
import app.views.dinners.ModifyDinnerFormData;
import app.views.dinners.ModifyDinnerView;
import app.views.dinners.SelectDinnerView;
import utils.io.commands.list.SortColumnCommand;
import utils.helpers.Functions;
import utils.io.tables.SimpleBox;
import utils.io.text_formatting.TextFormatter;

import java.util.List;
import java.util.LinkedHashMap;

public class DinnerController extends Controller {

    // ─── Utility methods ─── //

    public Event manageDinners(Request request) {
        int campId = Integer.parseInt(request.getParameter("campId"));
        ManageDinnersView manageDinnersView = new ManageDinnersView(campId);
        return manageDinnersView.render();
    }

    public Event listDinners(Request request) {
        int campId = Integer.parseInt(request.getParameter("campId"));

        CampDataManager campDataManager;
        DinnerDataManager dinnerDataManager;
        try {
            campDataManager = DataManagers.get(CampDataManager.class);
            dinnerDataManager = DataManagers.get(DinnerDataManager.class);
        } catch (DataManagerException | ModelException e) {
            System.out.println(Functions.styleAsErrorMessage("Les données n'ont pas pu être chargées."));
            return new CallUrlEvent("/camps/manage/" + campId);
        }

        Camp camp;
        try {
            camp = campDataManager.getCampWithExceptions(campId);
        } catch (DataManagerException | ModelException e) {
            System.out.println(Functions.styleAsErrorMessage(e.getMessage()));
            return new CallUrlEvent("/");
        }

        List<Dinner> campDinners;
        try {
            campDinners = dinnerDataManager.getCampDinners(camp);
        } catch (DataManagerException e) {
            System.out.println(Functions.styleAsErrorMessage(e.getMessage()));
            return new CallUrlEvent("/camps/manage/" + campId);
        }

        LinkedHashMap<Integer, SortColumnCommand.SortOrder> sortOrders = this.parseSortParameter(request);
        List<Dinner> sortedDinners;

        try {
            sortedDinners = this.sortModels(campDinners, Dinner.class, sortOrders);
        } catch (UnimplementedModelTableException e) {
            System.out.println(Functions.styleAsErrorMessage(e.getMessage()));
            return new CallUrlEvent("/camps/manage/" + campId);
        }

        ModelListView<Dinner> dinnerListView = new ModelListView<>(Dinner.class, sortedDinners, dinnerDataManager.hasUnsavedChanges(), "/camps/manage/%d/dinners/list".formatted(campId), "/camps/manage/%d/dinners".formatted(campId));
        return dinnerListView.render();
    }

    public Event addDinner(Request request) {
        int campId = Integer.parseInt(request.getParameter("campId"));

        AddDinnerView addDinnerView = new AddDinnerView(campId);
        Event event = addDinnerView.render();

        if (event instanceof FormResultEvent<?> formResultEvent && formResultEvent.getResult() instanceof Dinner dinnerToAdd) {
            try {
                DinnerDataManager dinnerDataManager = DataManagers.get(DinnerDataManager.class);
                dinnerDataManager.addDinner(dinnerToAdd);

                SimpleBox dinnerAddedSimpleBox = new SimpleBox();
                dinnerAddedSimpleBox.addLine(TextFormatter.bold(TextFormatter.magenta("# Repas ajouté")));
                dinnerAddedSimpleBox.addLine(TextFormatter.italic("Le repas a bien été enregistré sous l'identifiant " + TextFormatter.bold("#" + dinnerToAdd.getId())));

                System.out.println();
                dinnerAddedSimpleBox.display();
            } catch (DataManagerException | ModelException e) {
                System.out.println(Functions.styleAsErrorMessage(e.getMessage()));
            }

            return new CallUrlEvent("/camps/manage/%d/dinners".formatted(campId));
        }

        return event;
    }

    public Event modifySelect(Request request) {
        int campId = Integer.parseInt(request.getParameter("campId"));

        CampDataManager campDataManager;
        DinnerDataManager dinnerDataManager;
        try {
            campDataManager = DataManagers.get(CampDataManager.class);
            dinnerDataManager = DataManagers.get(DinnerDataManager.class);
        } catch (DataManagerException | ModelException e) {
            System.out.println(Functions.styleAsErrorMessage("Les données n'ont pas pu être chargées."));
            return new CallUrlEvent("/camps/manage/%d/dinners".formatted(campId));
        }

        Camp camp;
        try {
            camp = campDataManager.getCampWithExceptions(campId);
        } catch (DataManagerException | ModelException e) {
            System.out.println(Functions.styleAsErrorMessage(e.getMessage()));
            return new CallUrlEvent("/");
        }

        List<Dinner> campDinners;
        try {
            campDinners = dinnerDataManager.getCampDinners(camp);
        } catch (DataManagerException e) {
            System.out.println(Functions.styleAsErrorMessage(e.getMessage()));
            return new CallUrlEvent("/camps/manage/%d/dinners".formatted(campId));
        }

        LinkedHashMap<Integer, SortColumnCommand.SortOrder> sortOrders = this.parseSortParameter(request);
        List<Dinner> sortedDinners;

        try {
            sortedDinners = this.sortModels(campDinners, Dinner.class, sortOrders);
        } catch (UnimplementedModelTableException e) {
            System.out.println(Functions.styleAsErrorMessage(e.getMessage()));
            return new CallUrlEvent("/camps/manage/%d/dinners".formatted(campId));
        }

        SelectDinnerView selectDinnerView = new SelectDinnerView(campId, sortedDinners, dinnerDataManager);
        Event event = selectDinnerView.render();

        if (event instanceof FormResultEvent<?> formResultEvent && formResultEvent.getResult() instanceof Integer dinnerId) {
            return new CallUrlEvent("/camps/manage/%d/dinners/modify/%d".formatted(campId, dinnerId));
        }

        return event;
    }

    public Event modify(Request request) {
        int campId = Integer.parseInt(request.getParameter("campId"));
        int dinnerId = Integer.parseInt(request.getParameter("dinnerId"));

        DinnerDataManager dinnerDataManager;
        try {
            dinnerDataManager = DataManagers.get(DinnerDataManager.class);
        } catch (DataManagerException | ModelException e) {
            System.out.println(Functions.styleAsErrorMessage("Les données des repas n'ont pas pu être chargées."));
            return new CallUrlEvent("/camps/manage/%d/dinners".formatted(campId));
        }

        Dinner dinner;
        try {
            dinner = dinnerDataManager.getDinnerWithExceptions(dinnerId);
        } catch (DataManagerException | ModelException e) {
            System.out.println(Functions.styleAsErrorMessage(e.getMessage()));
            return new CallUrlEvent("/camps/manage/%d/dinners".formatted(campId));
        }

        ModifyDinnerView modifyDinnerView = new ModifyDinnerView(dinner);
        Event event = modifyDinnerView.render();

        if (event instanceof FormResultEvent<?> formResultEvent && formResultEvent.getResult() instanceof ModifyDinnerFormData modifyDinnerFormData) {
            try {
                dinnerDataManager.updateDinner(dinnerId, modifyDinnerFormData.modifiedDinner());

                SimpleBox dinnerModifiedSimpleBox = new SimpleBox();
                dinnerModifiedSimpleBox.addLine(TextFormatter.bold(TextFormatter.magenta("# Repas modifié")));
                dinnerModifiedSimpleBox.addLine(TextFormatter.italic("Le repas " + TextFormatter.bold("#" + dinnerId) + " a bien été modifié"));

                System.out.println();
                dinnerModifiedSimpleBox.display();
            } catch (DataManagerException | ModelException e) {
                System.out.println(Functions.styleAsErrorMessage(e.getMessage()));
            }

            return new CallUrlEvent("/camps/manage/%d/dinners".formatted(campId));
        }

        return event;
    }

    public Event deleteSelect(Request request) {
        int campId = Integer.parseInt(request.getParameter("campId"));

        CampDataManager campDataManager;
        DinnerDataManager dinnerDataManager;
        try {
            campDataManager = DataManagers.get(CampDataManager.class);
            dinnerDataManager = DataManagers.get(DinnerDataManager.class);
        } catch (DataManagerException | ModelException e) {
            System.out.println(Functions.styleAsErrorMessage("Les données n'ont pas pu être chargées."));
            return new CallUrlEvent("/camps/manage/%d/dinners".formatted(campId));
        }

        Camp camp;
        try {
            camp = campDataManager.getCampWithExceptions(campId);
        } catch (DataManagerException | ModelException e) {
            System.out.println(Functions.styleAsErrorMessage(e.getMessage()));
            return new CallUrlEvent("/");
        }

        List<Dinner> campDinners;
        try {
            campDinners = dinnerDataManager.getCampDinners(camp);
        } catch (DataManagerException e) {
            System.out.println(Functions.styleAsErrorMessage(e.getMessage()));
            return new CallUrlEvent("/camps/manage/%d/dinners".formatted(campId));
        }

        LinkedHashMap<Integer, SortColumnCommand.SortOrder> sortOrders = this.parseSortParameter(request);
        List<Dinner> sortedDinners;

        try {
            sortedDinners = this.sortModels(campDinners, Dinner.class, sortOrders);
        } catch (UnimplementedModelTableException e) {
            System.out.println(Functions.styleAsErrorMessage(e.getMessage()));
            return new CallUrlEvent("/camps/manage/%d/dinners".formatted(campId));
        }

        DeleteDinnerView deleteDinnerView = new DeleteDinnerView(campId, sortedDinners, dinnerDataManager);
        Event event = deleteDinnerView.render();

        if (event instanceof FormResultEvent<?> formResultEvent && formResultEvent.getResult() instanceof Integer dinnerId) {
            try {
                Dinner dinnerToDelete = dinnerDataManager.getDinnerWithExceptions(dinnerId);
                dinnerDataManager.deleteDinner(dinnerId);

                SimpleBox dinnerDeletedSimpleBox = new SimpleBox();
                dinnerDeletedSimpleBox.addLine(TextFormatter.bold(TextFormatter.magenta("# Repas supprimé")));
                dinnerDeletedSimpleBox.addLine(TextFormatter.italic("Le repas " + TextFormatter.bold(dinnerToDelete.toString()) + " a bien été supprimé."));

                System.out.println();
                dinnerDeletedSimpleBox.display();
            } catch (ModelException | DataManagerException e) {
                System.out.println(Functions.styleAsErrorMessage(e.getMessage()));
            }

            return new CallUrlEvent("/camps/manage/%d/dinners".formatted(campId));
        }

        return event;
    }

    public Event selectDinnerForReservations(Request request) {
        int campId = Integer.parseInt(request.getParameter("campId"));

        CampDataManager campDataManager;
        DinnerDataManager dinnerDataManager;
        try {
            campDataManager = DataManagers.get(CampDataManager.class);
            dinnerDataManager = DataManagers.get(DinnerDataManager.class);
        } catch (DataManagerException | ModelException e) {
            System.out.println(Functions.styleAsErrorMessage("Les données n'ont pas pu être chargées."));
            return new CallUrlEvent("/camps/manage/%d/dinners".formatted(campId));
        }

        Camp camp;
        try {
            camp = campDataManager.getCampWithExceptions(campId);
        } catch (DataManagerException | ModelException e) {
            System.out.println(Functions.styleAsErrorMessage(e.getMessage()));
            return new CallUrlEvent("/");
        }

        List<Dinner> campDinners;
        try {
            campDinners = dinnerDataManager.getCampDinners(camp);
        } catch (DataManagerException e) {
            System.out.println(Functions.styleAsErrorMessage(e.getMessage()));
            return new CallUrlEvent("/camps/manage/%d/dinners".formatted(campId));
        }

        LinkedHashMap<Integer, SortColumnCommand.SortOrder> sortOrders = this.parseSortParameter(request);
        List<Dinner> sortedDinners;

        try {
            sortedDinners = this.sortModels(campDinners, Dinner.class, sortOrders);
        } catch (UnimplementedModelTableException e) {
            System.out.println(Functions.styleAsErrorMessage(e.getMessage()));
            return new CallUrlEvent("/camps/manage/%d/dinners".formatted(campId));
        }

        SelectDinnerView selectDinnerView = new SelectDinnerView(campId, sortedDinners, dinnerDataManager);
        Event event = selectDinnerView.render();

        if (event instanceof FormResultEvent<?> formResultEvent && formResultEvent.getResult() instanceof Integer dinnerId) {
            return new CallUrlEvent("/camps/manage/%d/dinners/%d/reservations".formatted(campId, dinnerId));
        }

        return event;
    }

    public Event manageDinnerReservations(Request request) {
        int campId = Integer.parseInt(request.getParameter("campId"));
        int dinnerId = Integer.parseInt(request.getParameter("dinnerId"));
        ManageDinnerReservationsView manageDinnerReservationsView = new ManageDinnerReservationsView(campId, dinnerId);
        return manageDinnerReservationsView.render();
    }

    public Event listDinnerReservations(Request request) {
        int campId = Integer.parseInt(request.getParameter("campId"));
        int dinnerId = Integer.parseInt(request.getParameter("dinnerId"));

        DinnerDataManager dinnerDataManager;
        DinnerReservationDataManager dinnerReservationDataManager;
        try {
            dinnerDataManager = DataManagers.get(DinnerDataManager.class);
            dinnerReservationDataManager = DataManagers.get(DinnerReservationDataManager.class);
        } catch (DataManagerException | ModelException e) {
            System.out.println(Functions.styleAsErrorMessage("Les données n'ont pas pu être chargées."));
            return new CallUrlEvent("/camps/manage/%d/dinners/%d/reservations".formatted(campId, dinnerId));
        }

        Dinner dinner;
        try {
            dinner = dinnerDataManager.getDinnerWithExceptions(dinnerId);
        } catch (DataManagerException | ModelException e) {
            System.out.println(Functions.styleAsErrorMessage(e.getMessage()));
            return new CallUrlEvent("/camps/manage/%d/dinners".formatted(campId));
        }

        List<DinnerReservation> dinnerReservations;
        try {
            dinnerReservations = dinnerReservationDataManager.getDinnerDinnerReservations(dinner);
        } catch (DataManagerException e) {
            System.out.println(Functions.styleAsErrorMessage(e.getMessage()));
            return new CallUrlEvent("/camps/manage/%d/dinners/%d/reservations".formatted(campId, dinnerId));
        }

        LinkedHashMap<Integer, SortColumnCommand.SortOrder> sortOrders = this.parseSortParameter(request);
        List<DinnerReservation> sortedDinnerReservations;

        try {
            sortedDinnerReservations = this.sortModels(dinnerReservations, DinnerReservation.class, sortOrders);
        } catch (UnimplementedModelTableException e) {
            System.out.println(Functions.styleAsErrorMessage(e.getMessage()));
            return new CallUrlEvent("/camps/manage/%d/dinners/%d/reservations".formatted(campId, dinnerId));
        }

        ModelListView<DinnerReservation> dinnerReservationListView = new ModelListView<>(DinnerReservation.class, sortedDinnerReservations, dinnerReservationDataManager.hasUnsavedChanges(), "/camps/manage/%d/dinners/%d/reservations/list".formatted(campId, dinnerId), "/camps/manage/%d/dinners/%d/reservations".formatted(campId, dinnerId));
        return dinnerReservationListView.render();
    }

    public Event addDinnerReservation(Request request) {
        int campId = Integer.parseInt(request.getParameter("campId"));
        int dinnerId = Integer.parseInt(request.getParameter("dinnerId"));

        AddDinnerReservationView addDinnerReservationView = new AddDinnerReservationView(campId, dinnerId);
        Event event = addDinnerReservationView.render();

        if (event instanceof FormResultEvent<?> formResultEvent && formResultEvent.getResult() instanceof DinnerReservation dinnerReservationToAdd) {
            try {
                DinnerReservationDataManager dinnerReservationDataManager = DataManagers.get(DinnerReservationDataManager.class);
                dinnerReservationDataManager.addDinnerReservation(dinnerReservationToAdd);

                SimpleBox dinnerReservationAddedSimpleBox = new SimpleBox();
                dinnerReservationAddedSimpleBox.addLine(TextFormatter.bold(TextFormatter.magenta("# Réservation de repas ajoutée")));
                dinnerReservationAddedSimpleBox.addLine(TextFormatter.italic("L'réservation de repas a bien été enregistrée sous l'identifiant " + TextFormatter.bold("#" + dinnerReservationToAdd.getId())));

                System.out.println();
                dinnerReservationAddedSimpleBox.display();
            } catch (DataManagerException | ModelException e) {
                System.out.println(Functions.styleAsErrorMessage(e.getMessage()));
            }

            return new CallUrlEvent("/camps/manage/%d/dinners/%d/reservations".formatted(campId, dinnerId));
        }

        return event;
    }

    public Event deleteDinnerReservation(Request request) {
        int campId = Integer.parseInt(request.getParameter("campId"));
        int dinnerId = Integer.parseInt(request.getParameter("dinnerId"));

        DinnerDataManager dinnerDataManager;
        DinnerReservationDataManager dinnerReservationDataManager;
        try {
            dinnerDataManager = DataManagers.get(DinnerDataManager.class);
            dinnerReservationDataManager = DataManagers.get(DinnerReservationDataManager.class);
        } catch (DataManagerException | ModelException e) {
            System.out.println(Functions.styleAsErrorMessage("Les données n'ont pas pu être chargées."));
            return new CallUrlEvent("/camps/manage/%d/dinners/%d/reservations".formatted(campId, dinnerId));
        }

        Dinner dinner;
        try {
            dinner = dinnerDataManager.getDinnerWithExceptions(dinnerId);
        } catch (DataManagerException | ModelException e) {
            System.out.println(Functions.styleAsErrorMessage(e.getMessage()));
            return new CallUrlEvent("/camps/manage/%d/dinners".formatted(campId));
        }

        List<DinnerReservation> dinnerReservations;
        try {
            dinnerReservations = dinnerReservationDataManager.getDinnerDinnerReservations(dinner);
        } catch (DataManagerException e) {
            System.out.println(Functions.styleAsErrorMessage(e.getMessage()));
            return new CallUrlEvent("/camps/manage/%d/dinners/%d/reservations".formatted(campId, dinnerId));
        }

        LinkedHashMap<Integer, SortColumnCommand.SortOrder> sortOrders = this.parseSortParameter(request);
        List<DinnerReservation> sortedDinnerReservations;

        try {
            sortedDinnerReservations = this.sortModels(dinnerReservations, DinnerReservation.class, sortOrders);
        } catch (UnimplementedModelTableException e) {
            System.out.println(Functions.styleAsErrorMessage(e.getMessage()));
            return new CallUrlEvent("/camps/manage/%d/dinners/%d/reservations".formatted(campId, dinnerId));
        }

        DeleteDinnerReservationView deleteDinnerReservationView = new DeleteDinnerReservationView(campId, dinnerId, sortedDinnerReservations, dinnerReservationDataManager);
        Event event = deleteDinnerReservationView.render();

        if (event instanceof FormResultEvent<?> formResultEvent && formResultEvent.getResult() instanceof Integer dinnerReservationId) {
            try {
                DinnerReservation dinnerReservationToDelete = dinnerReservationDataManager.getDinnerReservationWithExceptions(dinnerReservationId);
                dinnerReservationDataManager.deleteDinnerReservation(dinnerReservationId);

                SimpleBox dinnerReservationDeletedSimpleBox = new SimpleBox();
                dinnerReservationDeletedSimpleBox.addLine(TextFormatter.bold(TextFormatter.magenta("# Réservation de repas supprimée")));
                dinnerReservationDeletedSimpleBox.addLine(TextFormatter.italic("L'réservation de repas " + TextFormatter.bold(dinnerReservationToDelete.toString()) + " a bien été supprimée."));

                System.out.println();
                dinnerReservationDeletedSimpleBox.display();
            } catch (ModelException | DataManagerException e) {
                System.out.println(Functions.styleAsErrorMessage(e.getMessage()));
            }

            return new CallUrlEvent("/camps/manage/%d/dinners/%d/reservations".formatted(campId, dinnerId));
        }

        return event;
    }

}
