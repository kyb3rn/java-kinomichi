package app.controllers;

import app.events.CallUrlEvent;
import app.events.Event;
import app.events.FormResultEvent;
import app.models.Camp;
import app.models.Lodging;
import app.models.LodgingReservation;
import app.models.ModelException;
import app.models.managers.CampDataManager;
import app.models.managers.DataManagerException;
import app.models.managers.LodgingDataManager;
import app.models.managers.LodgingReservationDataManager;
import app.models.formatting.table.UnimplementedModelTableException;
import app.models.managers.DataManagers;
import app.routing.Request;
import app.views.ModelListView;
import app.views.lodgings.AddLodgingReservationView;
import app.views.lodgings.AddLodgingView;
import app.views.lodgings.DeleteLodgingReservationView;
import app.views.lodgings.DeleteLodgingView;
import app.views.lodgings.ManageLodgingReservationsView;
import app.views.lodgings.ManageLodgingsView;
import app.views.lodgings.ModifyLodgingFormData;
import app.views.lodgings.ModifyLodgingView;
import app.views.lodgings.SelectLodgingView;
import utils.io.commands.list.SortColumnCommand;
import utils.helpers.Functions;
import utils.io.tables.SimpleBox;
import utils.io.text_formatting.TextFormatter;

import java.util.List;
import java.util.LinkedHashMap;

public class LodgingController extends Controller {

    // ─── Utility methods ─── //

    public Event manageLodgings(Request request) {
        int campId = Integer.parseInt(request.getParameter("campId"));
        ManageLodgingsView manageLodgingsView = new ManageLodgingsView(campId);
        return manageLodgingsView.render();
    }

    public Event listLodgings(Request request) {
        int campId = Integer.parseInt(request.getParameter("campId"));

        CampDataManager campDataManager;
        LodgingDataManager lodgingDataManager;
        try {
            campDataManager = DataManagers.get(CampDataManager.class);
            lodgingDataManager = DataManagers.get(LodgingDataManager.class);
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

        List<Lodging> campLodgings;
        try {
            campLodgings = lodgingDataManager.getCampLodgings(camp);
        } catch (DataManagerException e) {
            System.out.println(Functions.styleAsErrorMessage(e.getMessage()));
            return new CallUrlEvent("/camps/manage/" + campId);
        }

        LinkedHashMap<Integer, SortColumnCommand.SortOrder> sortOrders = this.parseSortParameter(request);
        List<Lodging> sortedLodgings;

        try {
            sortedLodgings = this.sortModels(campLodgings, Lodging.class, sortOrders);
        } catch (UnimplementedModelTableException e) {
            System.out.println(Functions.styleAsErrorMessage(e.getMessage()));
            return new CallUrlEvent("/camps/manage/" + campId);
        }

        ModelListView<Lodging> lodgingListView = new ModelListView<>(Lodging.class, sortedLodgings, lodgingDataManager.hasUnsavedChanges(), "/camps/manage/%d/lodgings/list".formatted(campId), "/camps/manage/%d/lodgings".formatted(campId));
        return lodgingListView.render();
    }

    public Event addLodging(Request request) {
        int campId = Integer.parseInt(request.getParameter("campId"));

        AddLodgingView addLodgingView = new AddLodgingView(campId);
        Event event = addLodgingView.render();

        if (event instanceof FormResultEvent<?> formResultEvent && formResultEvent.getResult() instanceof Lodging lodgingToAdd) {
            try {
                LodgingDataManager lodgingDataManager = DataManagers.get(LodgingDataManager.class);
                lodgingDataManager.addLodging(lodgingToAdd);

                SimpleBox lodgingAddedSimpleBox = new SimpleBox();
                lodgingAddedSimpleBox.addLine(TextFormatter.bold(TextFormatter.magenta("# Hébergement ajouté")));
                lodgingAddedSimpleBox.addLine(TextFormatter.italic("L'hébergement a bien été enregistré sous l'identifiant " + TextFormatter.bold("#" + lodgingToAdd.getId())));

                System.out.println();
                lodgingAddedSimpleBox.display();
            } catch (DataManagerException | ModelException e) {
                System.out.println(Functions.styleAsErrorMessage(e.getMessage()));
            }

            return new CallUrlEvent("/camps/manage/%d/lodgings".formatted(campId));
        }

        return event;
    }

    public Event modifySelect(Request request) {
        int campId = Integer.parseInt(request.getParameter("campId"));

        CampDataManager campDataManager;
        LodgingDataManager lodgingDataManager;
        try {
            campDataManager = DataManagers.get(CampDataManager.class);
            lodgingDataManager = DataManagers.get(LodgingDataManager.class);
        } catch (DataManagerException | ModelException e) {
            System.out.println(Functions.styleAsErrorMessage("Les données n'ont pas pu être chargées."));
            return new CallUrlEvent("/camps/manage/%d/lodgings".formatted(campId));
        }

        Camp camp;
        try {
            camp = campDataManager.getCampWithExceptions(campId);
        } catch (DataManagerException | ModelException e) {
            System.out.println(Functions.styleAsErrorMessage(e.getMessage()));
            return new CallUrlEvent("/");
        }

        List<Lodging> campLodgings;
        try {
            campLodgings = lodgingDataManager.getCampLodgings(camp);
        } catch (DataManagerException e) {
            System.out.println(Functions.styleAsErrorMessage(e.getMessage()));
            return new CallUrlEvent("/camps/manage/%d/lodgings".formatted(campId));
        }

        LinkedHashMap<Integer, SortColumnCommand.SortOrder> sortOrders = this.parseSortParameter(request);
        List<Lodging> sortedLodgings;

        try {
            sortedLodgings = this.sortModels(campLodgings, Lodging.class, sortOrders);
        } catch (UnimplementedModelTableException e) {
            System.out.println(Functions.styleAsErrorMessage(e.getMessage()));
            return new CallUrlEvent("/camps/manage/%d/lodgings".formatted(campId));
        }

        SelectLodgingView selectLodgingView = new SelectLodgingView(campId, sortedLodgings, lodgingDataManager);
        Event event = selectLodgingView.render();

        if (event instanceof FormResultEvent<?> formResultEvent && formResultEvent.getResult() instanceof Integer lodgingId) {
            return new CallUrlEvent("/camps/manage/%d/lodgings/modify/%d".formatted(campId, lodgingId));
        }

        return event;
    }

    public Event modify(Request request) {
        int campId = Integer.parseInt(request.getParameter("campId"));
        int lodgingId = Integer.parseInt(request.getParameter("lodgingId"));

        LodgingDataManager lodgingDataManager;
        try {
            lodgingDataManager = DataManagers.get(LodgingDataManager.class);
        } catch (DataManagerException | ModelException e) {
            System.out.println(Functions.styleAsErrorMessage("Les données des hébergements n'ont pas pu être chargées."));
            return new CallUrlEvent("/camps/manage/%d/lodgings".formatted(campId));
        }

        Lodging lodging;
        try {
            lodging = lodgingDataManager.getLodgingWithExceptions(lodgingId);
        } catch (DataManagerException | ModelException e) {
            System.out.println(Functions.styleAsErrorMessage(e.getMessage()));
            return new CallUrlEvent("/camps/manage/%d/lodgings".formatted(campId));
        }

        ModifyLodgingView modifyLodgingView = new ModifyLodgingView(lodging);
        Event event = modifyLodgingView.render();

        if (event instanceof FormResultEvent<?> formResultEvent && formResultEvent.getResult() instanceof ModifyLodgingFormData modifyLodgingFormData) {
            try {
                lodgingDataManager.updateLodging(lodgingId, modifyLodgingFormData.modifiedLodging());

                SimpleBox lodgingModifiedSimpleBox = new SimpleBox();
                lodgingModifiedSimpleBox.addLine(TextFormatter.bold(TextFormatter.magenta("# Hébergement modifié")));
                lodgingModifiedSimpleBox.addLine(TextFormatter.italic("L'hébergement " + TextFormatter.bold("#" + lodgingId) + " a bien été modifié"));

                System.out.println();
                lodgingModifiedSimpleBox.display();
            } catch (DataManagerException | ModelException e) {
                System.out.println(Functions.styleAsErrorMessage(e.getMessage()));
            }

            return new CallUrlEvent("/camps/manage/%d/lodgings".formatted(campId));
        }

        return event;
    }

    public Event deleteSelect(Request request) {
        int campId = Integer.parseInt(request.getParameter("campId"));

        CampDataManager campDataManager;
        LodgingDataManager lodgingDataManager;
        try {
            campDataManager = DataManagers.get(CampDataManager.class);
            lodgingDataManager = DataManagers.get(LodgingDataManager.class);
        } catch (DataManagerException | ModelException e) {
            System.out.println(Functions.styleAsErrorMessage("Les données n'ont pas pu être chargées."));
            return new CallUrlEvent("/camps/manage/%d/lodgings".formatted(campId));
        }

        Camp camp;
        try {
            camp = campDataManager.getCampWithExceptions(campId);
        } catch (DataManagerException | ModelException e) {
            System.out.println(Functions.styleAsErrorMessage(e.getMessage()));
            return new CallUrlEvent("/");
        }

        List<Lodging> campLodgings;
        try {
            campLodgings = lodgingDataManager.getCampLodgings(camp);
        } catch (DataManagerException e) {
            System.out.println(Functions.styleAsErrorMessage(e.getMessage()));
            return new CallUrlEvent("/camps/manage/%d/lodgings".formatted(campId));
        }

        LinkedHashMap<Integer, SortColumnCommand.SortOrder> sortOrders = this.parseSortParameter(request);
        List<Lodging> sortedLodgings;

        try {
            sortedLodgings = this.sortModels(campLodgings, Lodging.class, sortOrders);
        } catch (UnimplementedModelTableException e) {
            System.out.println(Functions.styleAsErrorMessage(e.getMessage()));
            return new CallUrlEvent("/camps/manage/%d/lodgings".formatted(campId));
        }

        DeleteLodgingView deleteLodgingView = new DeleteLodgingView(campId, sortedLodgings, lodgingDataManager);
        Event event = deleteLodgingView.render();

        if (event instanceof FormResultEvent<?> formResultEvent && formResultEvent.getResult() instanceof Integer deleteLodgingId) {
            try {
                Lodging lodgingToDelete = lodgingDataManager.getLodgingWithExceptions(deleteLodgingId);
                lodgingDataManager.deleteLodging(deleteLodgingId);

                SimpleBox lodgingDeletedSimpleBox = new SimpleBox();
                lodgingDeletedSimpleBox.addLine(TextFormatter.bold(TextFormatter.magenta("# Hébergement supprimé")));
                lodgingDeletedSimpleBox.addLine(TextFormatter.italic("L'hébergement " + TextFormatter.bold(lodgingToDelete.toString()) + " a bien été supprimé."));

                System.out.println();
                lodgingDeletedSimpleBox.display();
            } catch (ModelException | DataManagerException e) {
                System.out.println(Functions.styleAsErrorMessage(e.getMessage()));
            }

            return new CallUrlEvent("/camps/manage/%d/lodgings".formatted(campId));
        }

        return event;
    }

    public Event selectLodgingForReservations(Request request) {
        int campId = Integer.parseInt(request.getParameter("campId"));

        CampDataManager campDataManager;
        LodgingDataManager lodgingDataManager;
        try {
            campDataManager = DataManagers.get(CampDataManager.class);
            lodgingDataManager = DataManagers.get(LodgingDataManager.class);
        } catch (DataManagerException | ModelException e) {
            System.out.println(Functions.styleAsErrorMessage("Les données n'ont pas pu être chargées."));
            return new CallUrlEvent("/camps/manage/%d/lodgings".formatted(campId));
        }

        Camp camp;
        try {
            camp = campDataManager.getCampWithExceptions(campId);
        } catch (DataManagerException | ModelException e) {
            System.out.println(Functions.styleAsErrorMessage(e.getMessage()));
            return new CallUrlEvent("/");
        }

        List<Lodging> campLodgings;
        try {
            campLodgings = lodgingDataManager.getCampLodgings(camp);
        } catch (DataManagerException e) {
            System.out.println(Functions.styleAsErrorMessage(e.getMessage()));
            return new CallUrlEvent("/camps/manage/%d/lodgings".formatted(campId));
        }

        LinkedHashMap<Integer, SortColumnCommand.SortOrder> sortOrders = this.parseSortParameter(request);
        List<Lodging> sortedLodgings;

        try {
            sortedLodgings = this.sortModels(campLodgings, Lodging.class, sortOrders);
        } catch (UnimplementedModelTableException e) {
            System.out.println(Functions.styleAsErrorMessage(e.getMessage()));
            return new CallUrlEvent("/camps/manage/%d/lodgings".formatted(campId));
        }

        SelectLodgingView selectLodgingView = new SelectLodgingView(campId, sortedLodgings, lodgingDataManager);
        Event event = selectLodgingView.render();

        if (event instanceof FormResultEvent<?> formResultEvent && formResultEvent.getResult() instanceof Integer lodgingId) {
            return new CallUrlEvent("/camps/manage/%d/lodgings/%d/reservations".formatted(campId, lodgingId));
        }

        return event;
    }

    public Event manageLodgingReservations(Request request) {
        int campId = Integer.parseInt(request.getParameter("campId"));
        int lodgingId = Integer.parseInt(request.getParameter("lodgingId"));
        ManageLodgingReservationsView manageLodgingReservationsView = new ManageLodgingReservationsView(campId, lodgingId);
        return manageLodgingReservationsView.render();
    }

    public Event listLodgingReservations(Request request) {
        int campId = Integer.parseInt(request.getParameter("campId"));
        int lodgingId = Integer.parseInt(request.getParameter("lodgingId"));

        LodgingDataManager lodgingDataManager;
        LodgingReservationDataManager lodgingReservationDataManager;
        try {
            lodgingDataManager = DataManagers.get(LodgingDataManager.class);
            lodgingReservationDataManager = DataManagers.get(LodgingReservationDataManager.class);
        } catch (DataManagerException | ModelException e) {
            System.out.println(Functions.styleAsErrorMessage("Les données n'ont pas pu être chargées."));
            return new CallUrlEvent("/camps/manage/%d/lodgings/%d/reservations".formatted(campId, lodgingId));
        }

        Lodging lodging;
        try {
            lodging = lodgingDataManager.getLodgingWithExceptions(lodgingId);
        } catch (DataManagerException | ModelException e) {
            System.out.println(Functions.styleAsErrorMessage(e.getMessage()));
            return new CallUrlEvent("/camps/manage/%d/lodgings".formatted(campId));
        }

        List<LodgingReservation> lodgingReservations;
        try {
            lodgingReservations = lodgingReservationDataManager.getLodgingLodgingReservations(lodging);
        } catch (DataManagerException e) {
            System.out.println(Functions.styleAsErrorMessage(e.getMessage()));
            return new CallUrlEvent("/camps/manage/%d/lodgings/%d/reservations".formatted(campId, lodgingId));
        }

        LinkedHashMap<Integer, SortColumnCommand.SortOrder> sortOrders = this.parseSortParameter(request);
        List<LodgingReservation> sortedLodgingReservations;

        try {
            sortedLodgingReservations = this.sortModels(lodgingReservations, LodgingReservation.class, sortOrders);
        } catch (UnimplementedModelTableException e) {
            System.out.println(Functions.styleAsErrorMessage(e.getMessage()));
            return new CallUrlEvent("/camps/manage/%d/lodgings/%d/reservations".formatted(campId, lodgingId));
        }

        ModelListView<LodgingReservation> lodgingReservationListView = new ModelListView<>(LodgingReservation.class, sortedLodgingReservations, lodgingReservationDataManager.hasUnsavedChanges(), "/camps/manage/%d/lodgings/%d/reservations/list".formatted(campId, lodgingId), "/camps/manage/%d/lodgings/%d/reservations".formatted(campId, lodgingId));
        return lodgingReservationListView.render();
    }

    public Event addLodgingReservation(Request request) {
        int campId = Integer.parseInt(request.getParameter("campId"));
        int lodgingId = Integer.parseInt(request.getParameter("lodgingId"));

        AddLodgingReservationView addLodgingReservationView = new AddLodgingReservationView(campId, lodgingId);
        Event event = addLodgingReservationView.render();

        if (event instanceof FormResultEvent<?> formResultEvent && formResultEvent.getResult() instanceof LodgingReservation lodgingReservationToAdd) {
            try {
                LodgingReservationDataManager lodgingReservationDataManager = DataManagers.get(LodgingReservationDataManager.class);
                lodgingReservationDataManager.addLodgingReservation(lodgingReservationToAdd);

                SimpleBox lodgingReservationAddedSimpleBox = new SimpleBox();
                lodgingReservationAddedSimpleBox.addLine(TextFormatter.bold(TextFormatter.magenta("# Réservation d'hébergement ajoutée")));
                lodgingReservationAddedSimpleBox.addLine(TextFormatter.italic("La réservation d'hébergement a bien été enregistrée sous l'identifiant " + TextFormatter.bold("#" + lodgingReservationToAdd.getId())));

                System.out.println();
                lodgingReservationAddedSimpleBox.display();
            } catch (DataManagerException | ModelException e) {
                System.out.println(Functions.styleAsErrorMessage(e.getMessage()));
            }

            return new CallUrlEvent("/camps/manage/%d/lodgings/%d/reservations".formatted(campId, lodgingId));
        }

        return event;
    }

    public Event deleteLodgingReservation(Request request) {
        int campId = Integer.parseInt(request.getParameter("campId"));
        int lodgingId = Integer.parseInt(request.getParameter("lodgingId"));

        LodgingDataManager lodgingDataManager;
        LodgingReservationDataManager lodgingReservationDataManager;
        try {
            lodgingDataManager = DataManagers.get(LodgingDataManager.class);
            lodgingReservationDataManager = DataManagers.get(LodgingReservationDataManager.class);
        } catch (DataManagerException | ModelException e) {
            System.out.println(Functions.styleAsErrorMessage("Les données n'ont pas pu être chargées."));
            return new CallUrlEvent("/camps/manage/%d/lodgings/%d/reservations".formatted(campId, lodgingId));
        }

        Lodging lodging;
        try {
            lodging = lodgingDataManager.getLodgingWithExceptions(lodgingId);
        } catch (DataManagerException | ModelException e) {
            System.out.println(Functions.styleAsErrorMessage(e.getMessage()));
            return new CallUrlEvent("/camps/manage/%d/lodgings".formatted(campId));
        }

        List<LodgingReservation> lodgingReservations;
        try {
            lodgingReservations = lodgingReservationDataManager.getLodgingLodgingReservations(lodging);
        } catch (DataManagerException e) {
            System.out.println(Functions.styleAsErrorMessage(e.getMessage()));
            return new CallUrlEvent("/camps/manage/%d/lodgings/%d/reservations".formatted(campId, lodgingId));
        }

        LinkedHashMap<Integer, SortColumnCommand.SortOrder> sortOrders = this.parseSortParameter(request);
        List<LodgingReservation> sortedLodgingReservations;

        try {
            sortedLodgingReservations = this.sortModels(lodgingReservations, LodgingReservation.class, sortOrders);
        } catch (UnimplementedModelTableException e) {
            System.out.println(Functions.styleAsErrorMessage(e.getMessage()));
            return new CallUrlEvent("/camps/manage/%d/lodgings/%d/reservations".formatted(campId, lodgingId));
        }

        DeleteLodgingReservationView deleteLodgingReservationView = new DeleteLodgingReservationView(campId, lodgingId, sortedLodgingReservations, lodgingReservationDataManager);
        Event event = deleteLodgingReservationView.render();

        if (event instanceof FormResultEvent<?> formResultEvent && formResultEvent.getResult() instanceof Integer lodgingReservationId) {
            try {
                LodgingReservation lodgingReservationToDelete = lodgingReservationDataManager.getLodgingReservationWithExceptions(lodgingReservationId);
                lodgingReservationDataManager.deleteLodgingReservation(lodgingReservationId);

                SimpleBox lodgingReservationDeletedSimpleBox = new SimpleBox();
                lodgingReservationDeletedSimpleBox.addLine(TextFormatter.bold(TextFormatter.magenta("# Réservation d'hébergement supprimée")));
                lodgingReservationDeletedSimpleBox.addLine(TextFormatter.italic("La réservation d'hébergement " + TextFormatter.bold(lodgingReservationToDelete.toString()) + " a bien été supprimée."));

                System.out.println();
                lodgingReservationDeletedSimpleBox.display();
            } catch (ModelException | DataManagerException e) {
                System.out.println(Functions.styleAsErrorMessage(e.getMessage()));
            }

            return new CallUrlEvent("/camps/manage/%d/lodgings/%d/reservations".formatted(campId, lodgingId));
        }

        return event;
    }

}
