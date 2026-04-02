package app.utils.menus;

import app.events.ExitProgramEvent;
import app.events.GoBackBackEvent;
import app.events.GoBackEvent;
import app.models.Model;
import app.models.formatting.ModelTableFormatter;
import utils.io.commands.Command;
import utils.io.commands.exceptions.UnhandledCommandException;
import utils.io.commands.list.BackBackCommand;
import utils.io.commands.list.BackCommand;
import utils.io.commands.list.ExitCommand;
import utils.io.menus.OrderedMenu;

public class ModelDetailMenu<M extends Model> extends OrderedMenu {

    // ─── Properties ─── //

    private final M model;

    // ─── Constructors ─── //

    public ModelDetailMenu(M model) {
        this.model = model;
        this.setCommandHandler(this::commandHandler);
    }

    // ─── Utility methods ─── //

    private Object commandHandler(String input, Command command) throws UnhandledCommandException {
        if (command instanceof ExitCommand) {
            return new ExitProgramEvent();
        } else if (command instanceof BackBackCommand) {
            return new GoBackBackEvent();
        } else if (command instanceof BackCommand) {
            return new GoBackEvent();
        }

        throw new UnhandledCommandException(command);
    }

    // ─── Overrides & inheritance ─── //

    @Override
    public void display() {
        ModelTableFormatter.forDetail(this.model).display();
    }

}
