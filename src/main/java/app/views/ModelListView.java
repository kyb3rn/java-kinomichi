package app.views;

import app.events.CallUrlEvent;
import app.events.Event;
import app.models.Model;
import app.models.formatting.EmptyContentModelTableFormatterException;
import app.models.formatting.table.UnimplementedModelTableException;
import app.utils.menus.ModelListMenu;
import utils.io.commands.list.SortColumnCommand;
import utils.helpers.Functions;
import utils.io.text_formatting.TextFormatter;
import utils.io.menus.MenuResponse;
import utils.io.menus.UnhandledMenuResponseType;

import java.util.Collection;
import java.util.Map;

public class ModelListView<M extends Model> extends View {

    // ─── Properties ─── //

    private final Class<M> modelClass;
    private final Collection<M> models;
    private final boolean hasUnsavedChanges;
    private final String sortBasePath;

    // ─── Constructors ─── //

    public ModelListView(Class<M> modelClass, Collection<M> models, boolean hasUnsavedChanges, String sortBasePath) {
        this.modelClass = modelClass;
        this.models = models;
        this.hasUnsavedChanges = hasUnsavedChanges;
        this.sortBasePath = sortBasePath;
    }

    // ─── Overrides & inheritance ─── //

    @Override
    public Event render() {
        ModelListMenu<M> modelListMenu;
        try {
            modelListMenu = new ModelListMenu<>(this.models);
        } catch (UnimplementedModelTableException | EmptyContentModelTableFormatterException e) {
            System.out.println(Functions.styleAsErrorMessage(e.getMessage()));
            return new CallUrlEvent("/");
        }

        if (!modelListMenu.generateTable()) {
            return new CallUrlEvent("/");
        }

        if (this.hasUnsavedChanges) {
            System.out.println(TextFormatter.italic(TextFormatter.yellow(TextFormatter.bold("ATTENTION !"), " Des modifications dans cette liste n'ont pas encore été sauvegardées. Rendez-vous dans le menu principal pour résoudre ce problème.")));
        }

        MenuResponse menuResponse = modelListMenu.use();
        Object response = menuResponse.getResponse();

        if (response instanceof Event event) {
            return event;
        } else if (response instanceof SortColumnCommand sortColumnCommand) {
            return new CallUrlEvent(this.sortBasePath + "/sort/" + this.buildSortPathSegment(sortColumnCommand));
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
