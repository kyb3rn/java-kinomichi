package app.controllers;

import app.events.CallUrlEvent;
import app.events.Event;
import app.events.FormResultEvent;
import app.models.Affiliation;
import app.models.Person;
import app.models.ModelException;
import app.models.managers.AffiliationDataManager;
import app.models.managers.DataManagerException;
import app.models.formatting.table.UnimplementedModelTableException;
import app.models.managers.DataManagers;
import app.models.managers.PersonDataManager;
import app.routing.Request;
import app.views.ModelListView;
import app.views.persons.AddPersonFormData;
import app.views.persons.AddPersonView;
import app.views.persons.PersonsDashboardView;
import utils.io.commands.list.SortColumnCommand;
import utils.io.helpers.Functions;
import utils.io.helpers.tables.SimpleBox;
import utils.io.helpers.texts.formatting.TextFormatter;

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

        if (event instanceof FormResultEvent<?> formResultEvent && formResultEvent.getResult() instanceof AddPersonFormData addPersonFormData) {
            try {
                PersonDataManager personDataManager = DataManagers.get(PersonDataManager.class);
                Person person = personDataManager.addPerson(addPersonFormData.person(), true);

                if (addPersonFormData.affiliation() != null) {
                    Affiliation affiliation = addPersonFormData.affiliation();
                    affiliation.setPerson(person);

                    DataManagers.get(AffiliationDataManager.class).addAffiliation(affiliation);

                    SimpleBox personAddedSimpleBox = new SimpleBox();
                    personAddedSimpleBox.addLine(TextFormatter.bold(TextFormatter.magenta("# Personne ajoutée et affiliée")));
                    personAddedSimpleBox.addLine(TextFormatter.italic("La personne a bien été enregistrée sous l'identifiant " + TextFormatter.bold("#" + person.getId())));
                    personAddedSimpleBox.addLine(TextFormatter.italic("L'affiliation a bien été enregistrée et liée à la personne " + TextFormatter.bold("#" + person.getId())));

                    System.out.println();
                    personAddedSimpleBox.display();
                } else {
                    SimpleBox personAddedSimpleBox = new SimpleBox();
                    personAddedSimpleBox.addLine(TextFormatter.bold(TextFormatter.magenta("# Personne ajoutée")));
                    personAddedSimpleBox.addLine(TextFormatter.italic("La personne a bien été enregistrée sous l'identifiant " + TextFormatter.bold("#" + person.getId())));

                    System.out.println();
                    personAddedSimpleBox.display();
                }
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
