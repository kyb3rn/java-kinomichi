package app.controllers;

import app.events.CallUrlEvent;
import app.events.Event;
import app.events.FormResultEvent;
import app.models.Person;
import app.models.ModelException;
import app.models.managers.DataManagerException;
import app.models.NoResultForPrimaryKeyException;
import app.models.formatting.table.UnimplementedModelTableException;
import app.models.managers.DataManagers;
import app.models.managers.PersonDataManager;
import app.routing.Request;
import app.views.ModelListView;
import app.views.persons.AddPersonView;
import app.views.persons.ModifyPersonView;
import app.views.persons.PersonsDashboardView;
import app.views.persons.SelectPersonView;
import utils.io.commands.list.SortColumnCommand;
import utils.helpers.Functions;
import utils.io.tables.SimpleBox;
import utils.io.text_formatting.TextFormatter;

import java.util.List;
import java.util.LinkedHashMap;

public class PersonController extends Controller {

    // ─── Utility methods ─── //

    public Event dashboard(Request request) {
        PersonDataManager personDataManager;
        try {
            personDataManager = DataManagers.get(PersonDataManager.class);
        } catch (DataManagerException | ModelException e) {
            System.out.println(Functions.styleAsErrorMessage("Les données des personnes n'ont pas pu être chargées."));
            return new CallUrlEvent("/");
        }

        PersonsDashboardView personsDashboardView = new PersonsDashboardView(personDataManager.count(), personDataManager.hasUnsavedChanges());
        return personsDashboardView.render();
    }

    public Event add(Request request) {
        AddPersonView addPersonView = new AddPersonView();
        Event event = addPersonView.render();

        if (event instanceof FormResultEvent<?> formResultEvent && formResultEvent.getResult() instanceof Person personToAdd) {
            try {
                PersonDataManager personDataManager = DataManagers.get(PersonDataManager.class);
                personDataManager.addPerson(personToAdd);

                SimpleBox personAddedSimpleBox = new SimpleBox();
                personAddedSimpleBox.addLine(TextFormatter.bold(TextFormatter.magenta("# Personne ajoutée")));
                personAddedSimpleBox.addLine(TextFormatter.italic("La personne a bien été enregistrée sous l'identifiant " + TextFormatter.bold("#" + personToAdd.getId())));

                System.out.println();
                personAddedSimpleBox.display();
            } catch (DataManagerException | ModelException e) {
                System.out.println(Functions.styleAsErrorMessage(e.getMessage()));
            }

            return new CallUrlEvent("/persons/dashboard");
        }

        return event;
    }

    public Event modifySelect(Request request) {
        PersonDataManager personDataManager;
        try {
            personDataManager = DataManagers.get(PersonDataManager.class);
        } catch (DataManagerException | ModelException e) {
            System.out.println(Functions.styleAsErrorMessage("Les données des personnes n'ont pas pu être chargées."));
            return new CallUrlEvent("/persons/dashboard");
        }

        LinkedHashMap<Integer, SortColumnCommand.SortOrder> sortOrders = this.parseSortParameter(request);
        List<Person> sortedPersons;

        try {
            sortedPersons = this.sortModels(personDataManager.getModels(), Person.class, sortOrders);
        } catch (UnimplementedModelTableException e) {
            System.out.println(Functions.styleAsErrorMessage(e.getMessage()));
            return new CallUrlEvent("/persons/dashboard");
        }

        SelectPersonView selectPersonView = new SelectPersonView(sortedPersons, personDataManager);
        Event event = selectPersonView.render();

        if (event instanceof FormResultEvent<?> formResultEvent && formResultEvent.getResult() instanceof Integer personId) {
            return new CallUrlEvent("/persons/modify/" + personId);
        }

        return event;
    }

    public Event modify(Request request) {
        int personId = Integer.parseInt(request.getParameter("id"));

        PersonDataManager personDataManager;
        try {
            personDataManager = DataManagers.get(PersonDataManager.class);
        } catch (DataManagerException | ModelException e) {
            System.out.println(Functions.styleAsErrorMessage("Les données des personnes n'ont pas pu être chargées."));
            return new CallUrlEvent("/persons/dashboard");
        }

        Person person;
        try {
            person = personDataManager.getPersonWithExceptions(personId);
        } catch (ModelException e) {
            System.out.println(Functions.styleAsErrorMessage(e.getMessage()));
            return new CallUrlEvent("/persons/dashboard");
        }

        ModifyPersonView modifyPersonView = new ModifyPersonView(person);
        Event event = modifyPersonView.render();

        if (event instanceof FormResultEvent<?> formResultEvent && formResultEvent.getResult() instanceof Person modifiedPerson) {
            try {
                personDataManager.updatePerson(personId, modifiedPerson);

                SimpleBox personModifiedSimpleBox = new SimpleBox();
                personModifiedSimpleBox.addLine(TextFormatter.bold(TextFormatter.magenta("# Personne modifiée")));
                personModifiedSimpleBox.addLine(TextFormatter.italic("La personne " + TextFormatter.bold("#" + personId) + " a bien été modifiée"));

                System.out.println();
                personModifiedSimpleBox.display();
            } catch (DataManagerException | ModelException e) {
                System.out.println(Functions.styleAsErrorMessage(e.getMessage()));
            }

            return new CallUrlEvent("/persons/dashboard");
        }

        return event;
    }

    public Event list(Request request) {
        PersonDataManager personDataManager;
        try {
            personDataManager = DataManagers.get(PersonDataManager.class);
        } catch (DataManagerException | ModelException e) {
            System.out.println(Functions.styleAsErrorMessage("Les données des personnes n'ont pas pu être chargées."));
            return new CallUrlEvent("/explore");
        }

        LinkedHashMap<Integer, SortColumnCommand.SortOrder> sortOrders = this.parseSortParameter(request);
        List<Person> sortedPersons;

        try {
            sortedPersons = this.sortModels(personDataManager.getModels(), Person.class, sortOrders);
        } catch (UnimplementedModelTableException e) {
            System.out.println(Functions.styleAsErrorMessage(e.getMessage()));
            return new CallUrlEvent("/explore");
        }

        ModelListView<Person> personListView = new ModelListView<>(Person.class, sortedPersons, personDataManager.hasUnsavedChanges(), "/persons/list");
        return personListView.render();
    }

}
