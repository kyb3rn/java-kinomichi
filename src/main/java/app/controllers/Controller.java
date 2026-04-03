package app.controllers;

import app.models.Model;
import app.models.formatting.ModelTableFormatter;
import app.models.formatting.table.UnimplementedModelTableException;
import app.routing.Request;
import utils.io.commands.list.SortColumnCommand;

import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;

public abstract class Controller {

    // ─── Utility methods ─── //

    protected LinkedHashMap<Integer, SortColumnCommand.SortOrder> parseSortParameter(Request request) {
        LinkedHashMap<Integer, SortColumnCommand.SortOrder> sortOrders = new LinkedHashMap<>();
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

    protected <M extends Model> List<M> sortModels(Collection<M> models, Class<M> modelClass, LinkedHashMap<Integer, SortColumnCommand.SortOrder> sortOrders) throws UnimplementedModelTableException {
        Comparator<M> combinedComparator = null;

        for (var entry : sortOrders.entrySet()) {
            Comparator<M> columnComparator = ModelTableFormatter.comparatorForColumn(modelClass, entry.getKey() - 1);

            if (entry.getValue() == SortColumnCommand.SortOrder.DESCENDING) {
                columnComparator = columnComparator.reversed();
            }

            combinedComparator = (combinedComparator == null) ? columnComparator : combinedComparator.thenComparing(columnComparator);
        }

        if (combinedComparator == null) {
            combinedComparator = ModelTableFormatter.comparatorForColumn(modelClass, 0);
        }

        return models.stream().sorted(combinedComparator).toList();
    }

}
