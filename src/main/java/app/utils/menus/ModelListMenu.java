package app.utils.menus;

import app.events.ExitProgramEvent;
import app.events.GoBackBackEvent;
import app.events.GoBackEvent;
import app.models.Model;
import app.models.formatting.EmptyContentModelTableFormatterException;
import app.models.formatting.ModelTableFormatter;
import app.models.formatting.table.ModelTable;
import app.models.formatting.table.ModelTableInstanciationException;
import app.models.formatting.table.UnimplementedModelTableException;
import utils.io.commands.Command;
import utils.io.commands.exceptions.UnhandledCommandException;
import utils.io.commands.list.BackBackCommand;
import utils.io.commands.list.BackCommand;
import utils.io.commands.list.ExitCommand;
import utils.io.commands.list.SortColumnCommand;

import java.util.Collection;
import java.util.List;

public class ModelListMenu<M extends Model> extends ModelMenu<M> {

    // ─── Properties ─── //

    private final List<M> models;
    private final int columnCount;

    // ─── Constructors ─── //

    public ModelListMenu(Collection<M> models) throws UnimplementedModelTableException, EmptyContentModelTableFormatterException {
        if (models.isEmpty()) {
            throw new EmptyContentModelTableFormatterException("La liste de modèles est vide ou nulle");
        }

        @SuppressWarnings("unchecked")
        Class<M> clazz = (Class<M>) models.iterator().next().getClass();
        ModelTable.verifyImplementationExists(clazz);
        this.models = List.copyOf(models);
        this.columnCount = ModelTableFormatter.getColumnCount(clazz);
        this.setCommandHandler(this::commandHandler);
    }

    // ─── Utility methods ─── //

    protected Object commandHandler(String input, Command command) throws UnhandledCommandException {
        if (command instanceof SortColumnCommand sortColumnCommand) {
            for (int columnIndex : sortColumnCommand.getSortOrders().keySet()) {
                if (columnIndex < 1 || columnIndex > this.columnCount) {
                    System.out.println("L'index de colonne '%d' est invalide. Veuillez entrer un nombre entre 1 et %d.".formatted(columnIndex, this.columnCount));
                    return null;
                }
            }
            return sortColumnCommand;
        } else if (command instanceof ExitCommand) {
            return new ExitProgramEvent();
        } else if (command instanceof BackBackCommand) {
            return new GoBackBackEvent();
        } else if (command instanceof BackCommand) {
            return new GoBackEvent();
        }

        throw new UnhandledCommandException(command);
    }

    public void generateTableWithThrows() throws UnimplementedModelTableException, ModelTableInstanciationException, EmptyContentModelTableFormatterException {
        this.generatedModelTable = ModelTableFormatter.forList(this.models);
    }

}
