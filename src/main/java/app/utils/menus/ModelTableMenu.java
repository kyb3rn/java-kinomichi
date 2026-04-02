package app.utils.menus;

import app.events.ExitProgramEvent;
import app.events.GoBackEvent;
import app.models.Model;
import app.models.formatting.ModelTableFormatter;
import utils.io.commands.Command;
import utils.io.commands.UnhandledCommandException;
import utils.io.commands.list.BackCommand;
import utils.io.commands.list.ExitCommand;
import utils.io.commands.list.SortColumnCommand;
import utils.io.menus.OrderedMenu;

import java.util.Collection;
import java.util.List;

public class ModelTableMenu<M extends Model> extends OrderedMenu {

    // ─── Properties ─── //

    private final Class<M> modelClass;
    private final List<M> models;
    private final int columnCount;

    // ─── Constructors ─── //

    public ModelTableMenu(Class<M> modelClass, Collection<M> models) {
        this.modelClass = modelClass;
        this.models = List.copyOf(models);
        this.columnCount = ModelTableFormatter.getColumnCount(this.modelClass);
        this.setCommandHandler(this::commandHandler);
    }

    // ─── Utility methods ─── //

    private Object commandHandler(String input, Command command) throws UnhandledCommandException {
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
        } else if (command instanceof BackCommand) {
            return new GoBackEvent();
        }

        throw new UnhandledCommandException(command);
    }

    // ─── Overrides & inheritance ─── //

    @Override
    public void display() {
        ModelTableFormatter.forList(this.models).display();
    }

}
