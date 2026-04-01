package app.views;

import app.events.CallUrlEvent;
import app.events.Event;
import app.models.Person;
import app.utils.menus.ModelTableMenu;
import utils.io.commands.list.SortColumnCommand;
import utils.io.helpers.texts.formatting.TextFormatter;
import utils.io.menus.MenuResponse;
import utils.io.menus.UnhandledMenuResponseType;

import java.util.Collection;
import java.util.Map;

public class PersonListView extends View {

    // ─── Properties ─── //

    private final Collection<Person> persons;
    private final boolean hasUnsavedChanges;

    // ─── Constructors ─── //

    public PersonListView(Collection<Person> persons, boolean hasUnsavedChanges) {
        this.persons = persons;
        this.hasUnsavedChanges = hasUnsavedChanges;
    }

    // ─── Overrides & inheritance ─── //

    @Override
    public Event render() {
        ModelTableMenu<Person> personTableMenu = new ModelTableMenu<>(Person.class, this.persons);

        if (this.hasUnsavedChanges) {
            System.out.println(TextFormatter.italic(TextFormatter.yellow(TextFormatter.bold("ATTENTION !"), " Des modifications dans cette liste n'ont pas encore été sauvegardées. Rendez-vous dans le menu principal pour résoudre ce problème.")));
        }

        MenuResponse menuResponse = personTableMenu.use();
        Object response = menuResponse.getResponse();

        if (response instanceof Event event) {
            return event;
        } else if (response instanceof SortColumnCommand sortColumnCommand) {
            return new CallUrlEvent("/persons/sort/" + this.buildSortPathSegment(sortColumnCommand));
        } else {
            throw new UnhandledMenuResponseType();
        }
    }

    // ─── Utility methods ─── //

    private String buildSortPathSegment(SortColumnCommand sortColumnCommand) {
        StringBuilder sortPathSegmentBuilder = new StringBuilder();

        for (Map.Entry<Integer, SortColumnCommand.SortOrder> sortOrderEntry : sortColumnCommand.getSortOrders().entrySet()) {
            if (!sortPathSegmentBuilder.isEmpty()) {
                sortPathSegmentBuilder.append(",");
            }

            sortPathSegmentBuilder.append(sortOrderEntry.getKey());

            if (sortOrderEntry.getValue() == SortColumnCommand.SortOrder.DESCENDING) {
                sortPathSegmentBuilder.append(":DESC");
            }
        }

        return sortPathSegmentBuilder.toString();
    }

}
