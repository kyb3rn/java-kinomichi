package app.controllers;

import app.events.CallUrlEvent;
import app.events.Event;
import app.events.FormResultEvent;
import app.models.Affiliation;
import app.models.ModelException;
import app.models.NoResultForPrimaryKeyException;
import app.models.managers.AffiliationDataManager;
import app.models.managers.DataManagerException;
import app.models.formatting.table.UnimplementedModelTableException;
import app.models.managers.DataManagers;
import app.routing.Request;
import app.views.ModelListView;
import app.views.affiliations.AddAffiliationView;
import app.views.affiliations.AffiliationsDashboardView;
import app.views.affiliations.DeleteAffiliationView;
import app.views.affiliations.ModifyAffiliationView;
import app.views.affiliations.SelectAffiliationView;
import utils.io.commands.list.SortColumnCommand;
import utils.helpers.Functions;
import utils.io.tables.SimpleBox;
import utils.io.text_formatting.TextFormatter;

import java.util.List;
import java.util.LinkedHashMap;

public class AffiliationController extends Controller {

    // ─── Utility methods ─── //

    public Event dashboard(Request request) {
        AffiliationDataManager affiliationDataManager;
        try {
            affiliationDataManager = DataManagers.get(AffiliationDataManager.class);
        } catch (DataManagerException | ModelException e) {
            System.out.println(Functions.styleAsErrorMessage("Les données des affiliations n'ont pas pu être chargées."));
            return new CallUrlEvent("/");
        }

        AffiliationsDashboardView affiliationsDashboardView = new AffiliationsDashboardView(affiliationDataManager.count(), affiliationDataManager.hasUnsavedChanges());
        return affiliationsDashboardView.render();
    }

    public Event add(Request request) {
        AddAffiliationView addAffiliationView = new AddAffiliationView();
        Event event = addAffiliationView.render();

        if (event instanceof FormResultEvent<?> formResultEvent && formResultEvent.getResult() instanceof Affiliation affiliationToAdd) {
            try {
                AffiliationDataManager affiliationDataManager = DataManagers.get(AffiliationDataManager.class);
                affiliationDataManager.addAffiliation(affiliationToAdd);

                SimpleBox affiliationAddedSimpleBox = new SimpleBox();
                affiliationAddedSimpleBox.addLine(TextFormatter.bold(TextFormatter.magenta("# Affiliation ajoutée")));
                affiliationAddedSimpleBox.addLine(TextFormatter.italic("L'affiliation a bien été enregistrée sous l'identifiant " + TextFormatter.bold("#" + affiliationToAdd.getId())));

                System.out.println();
                affiliationAddedSimpleBox.display();
            } catch (DataManagerException | ModelException e) {
                System.out.println(Functions.styleAsErrorMessage(e.getMessage()));
            }

            return new CallUrlEvent("/affiliations/dashboard");
        }

        return event;
    }

    public Event modifySelect(Request request) {
        AffiliationDataManager affiliationDataManager;
        try {
            affiliationDataManager = DataManagers.get(AffiliationDataManager.class);
        } catch (DataManagerException | ModelException e) {
            System.out.println(Functions.styleAsErrorMessage("Les données des affiliations n'ont pas pu être chargées."));
            return new CallUrlEvent("/affiliations/dashboard");
        }

        LinkedHashMap<Integer, SortColumnCommand.SortOrder> sortOrders = this.parseSortParameter(request);
        List<Affiliation> sortedAffiliations;

        try {
            sortedAffiliations = this.sortModels(affiliationDataManager.getModels(), Affiliation.class, sortOrders);
        } catch (UnimplementedModelTableException e) {
            System.out.println(Functions.styleAsErrorMessage(e.getMessage()));
            return new CallUrlEvent("/affiliations/dashboard");
        }

        SelectAffiliationView selectAffiliationView = new SelectAffiliationView(sortedAffiliations, affiliationDataManager);
        Event event = selectAffiliationView.render();

        if (event instanceof FormResultEvent<?> formResultEvent && formResultEvent.getResult() instanceof Integer affiliationId) {
            return new CallUrlEvent("/affiliations/modify/" + affiliationId);
        }

        return event;
    }

    public Event modify(Request request) {
        int affiliationId = Integer.parseInt(request.getParameter("id"));

        AffiliationDataManager affiliationDataManager;
        try {
            affiliationDataManager = DataManagers.get(AffiliationDataManager.class);
        } catch (DataManagerException | ModelException e) {
            System.out.println(Functions.styleAsErrorMessage("Les données des affiliations n'ont pas pu être chargées."));
            return new CallUrlEvent("/affiliations/dashboard");
        }

        Affiliation affiliation;
        try {
            affiliation = affiliationDataManager.getAffiliationWithExceptions(affiliationId);
        } catch (ModelException e) {
            System.out.println(Functions.styleAsErrorMessage(e.getMessage()));
            return new CallUrlEvent("/affiliations/dashboard");
        }

        ModifyAffiliationView modifyAffiliationView = new ModifyAffiliationView(affiliation);
        Event event = modifyAffiliationView.render();

        if (event instanceof FormResultEvent<?> formResultEvent && formResultEvent.getResult() instanceof Affiliation modifiedAffiliation) {
            try {
                affiliationDataManager.updateAffiliation(affiliationId, modifiedAffiliation);

                SimpleBox affiliationModifiedSimpleBox = new SimpleBox();
                affiliationModifiedSimpleBox.addLine(TextFormatter.bold(TextFormatter.magenta("# Affiliation modifiée")));
                affiliationModifiedSimpleBox.addLine(TextFormatter.italic("L'affiliation " + TextFormatter.bold("#" + affiliationId) + " a bien été modifiée"));

                System.out.println();
                affiliationModifiedSimpleBox.display();
            } catch (DataManagerException | ModelException e) {
                System.out.println(Functions.styleAsErrorMessage(e.getMessage()));
            }

            return new CallUrlEvent("/affiliations/dashboard");
        }

        return event;
    }

    public Event delete(Request request) {
        AffiliationDataManager affiliationDataManager;
        try {
            affiliationDataManager = DataManagers.get(AffiliationDataManager.class);
        } catch (DataManagerException | ModelException e) {
            System.out.println(Functions.styleAsErrorMessage("Les données des affiliations n'ont pas pu être chargées."));
            return new CallUrlEvent("/affiliations/dashboard");
        }

        LinkedHashMap<Integer, SortColumnCommand.SortOrder> sortOrders = this.parseSortParameter(request);
        List<Affiliation> sortedAffiliations;

        try {
            sortedAffiliations = this.sortModels(affiliationDataManager.getModels(), Affiliation.class, sortOrders);
        } catch (UnimplementedModelTableException e) {
            System.out.println(Functions.styleAsErrorMessage(e.getMessage()));
            return new CallUrlEvent("/affiliations/dashboard");
        }

        DeleteAffiliationView deleteAffiliationView = new DeleteAffiliationView(sortedAffiliations, affiliationDataManager);
        Event event = deleteAffiliationView.render();

        if (event instanceof FormResultEvent<?> formResultEvent && formResultEvent.getResult() instanceof Integer deleteAffiliationId) {
            try {
                Affiliation affiliationToDelete = affiliationDataManager.getAffiliationWithExceptions(deleteAffiliationId);
                affiliationDataManager.deleteAffiliation(deleteAffiliationId);

                SimpleBox affiliationDeletedSimpleBox = new SimpleBox();
                affiliationDeletedSimpleBox.addLine(TextFormatter.bold(TextFormatter.magenta("# Affiliation supprimée")));
                affiliationDeletedSimpleBox.addLine(TextFormatter.italic("L'affiliation " + TextFormatter.bold(affiliationToDelete.toString()) + " a bien été supprimée."));

                System.out.println();
                affiliationDeletedSimpleBox.display();
            } catch (ModelException | DataManagerException e) {
                System.out.println(Functions.styleAsErrorMessage(e.getMessage()));
            }

            return new CallUrlEvent("/affiliations/dashboard");
        }

        return event;
    }

    public Event list(Request request) {
        AffiliationDataManager affiliationDataManager;
        try {
            affiliationDataManager = DataManagers.get(AffiliationDataManager.class);
        } catch (DataManagerException | ModelException e) {
            System.out.println(Functions.styleAsErrorMessage("Les données des affiliations n'ont pas pu être chargées."));
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
