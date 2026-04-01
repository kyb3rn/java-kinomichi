package app.controllers;

import app.events.Event;
import app.events.GoBackEvent;
import app.models.Person;
import app.models.ModelException;
import app.models.formatting.ModelTableFormatter;
import app.models.managers.DataManagerException;
import app.models.managers.DataManagers;
import app.models.managers.PersonDataManager;
import app.rooting.Request;
import app.views.PersonListView;
import utils.io.commands.list.SortColumnCommand;
import utils.io.helpers.Functions;

import java.util.Comparator;
import java.util.List;
import java.util.TreeMap;

public class PersonController extends Controller {

    // ─── Utility methods ─── //

    public Event list(Request request) {
        PersonDataManager personDataManager;
        try {
            personDataManager = DataManagers.get(PersonDataManager.class);
        } catch (DataManagerException | ModelException e) {
            System.out.println(Functions.styleAsErrorMessage("Les données des personnes n'ont pas pu être chargées."));
            return new GoBackEvent();
        }

        TreeMap<Integer, SortColumnCommand.SortOrder> sortOrders = this.parseSortParameter(request);
        List<Person> sortedPersons = this.sortPersons(personDataManager, sortOrders);

        PersonListView personListView = new PersonListView(sortedPersons, personDataManager.hasUnsavedChanges());
        return personListView.render();
    }

    // ─── Utility methods ─── //

    private TreeMap<Integer, SortColumnCommand.SortOrder> parseSortParameter(Request request) {
        TreeMap<Integer, SortColumnCommand.SortOrder> sortOrders = new TreeMap<>();
        String sortParameter = request.getParameter("sort");

        if (sortParameter == null || sortParameter.isBlank()) {
            return sortOrders;
        }

        String[] sortParts = sortParameter.split(",");
        for (String sortPart : sortParts) {
            String[] columnAndOrder = sortPart.split(":");
            int columnIndex = Integer.parseInt(columnAndOrder[0]);
            SortColumnCommand.SortOrder sortOrder = (columnAndOrder.length > 1)
                    ? SortColumnCommand.SortOrder.fromString(columnAndOrder[1])
                    : SortColumnCommand.SortOrder.ASCENDING;
            sortOrders.put(columnIndex, sortOrder);
        }

        return sortOrders;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private List<Person> sortPersons(PersonDataManager personDataManager, TreeMap<Integer, SortColumnCommand.SortOrder> sortOrders) {
        Comparator<Person> combinedComparator = sortOrders.entrySet().stream()
                .map(entry -> {
                    Comparator<Person> columnComparator = ModelTableFormatter.comparatorForColumn(Person.class, entry.getKey() - 1);
                    return entry.getValue() == SortColumnCommand.SortOrder.DESCENDING
                            ? columnComparator.reversed()
                            : columnComparator;
                })
                .reduce(Comparator::thenComparing)
                .orElse(ModelTableFormatter.comparatorForColumn(Person.class, 0));

        return personDataManager.getModels().stream().sorted(combinedComparator).toList();
    }

}
