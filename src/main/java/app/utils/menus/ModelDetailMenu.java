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
import utils.io.helpers.Functions;
import utils.io.helpers.tables.Table;
import utils.io.menus.OrderedMenu;

public class ModelDetailMenu<M extends Model> extends ModelMenu<M> {

    // ─── Properties ─── //

    private final M model;

    // ─── Constructors ─── //

    public ModelDetailMenu(M model) throws UnimplementedModelTableException {
        ModelTable.verifyImplementationExists(model.getClass());
        this.model = model;
        this.setCommandHandler(this::commandHandler);
    }

    // ─── Utility methods ─── //

    protected Object commandHandler(String input, Command command) throws UnhandledCommandException {
        if (command instanceof ExitCommand) {
            return new ExitProgramEvent();
        } else if (command instanceof BackBackCommand) {
            return new GoBackBackEvent();
        } else if (command instanceof BackCommand) {
            return new GoBackEvent();
        }

        throw new UnhandledCommandException(command);
    }

    public void generateTableWithThrows() throws UnimplementedModelTableException, ModelTableInstanciationException, EmptyContentModelTableFormatterException {
        this.generatedModelTable = ModelTableFormatter.forDetail(this.model);
    }

}
