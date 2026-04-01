package app.utils.menus;

import app.events.ExitProgramEvent;
import app.events.GoBackEvent;
import app.models.Model;
import app.models.formatting.ModelTableFormatter;
import app.utils.ExitProgramException;
import app.utils.helpers.KinomichiFunctions;
import utils.io.commands.Command;
import utils.io.commands.UnhandledCommandException;
import utils.io.commands.list.SortColumnCommand;
import utils.io.menus.HookInterruptException;
import utils.io.menus.MenuResponse;
import utils.io.menus.OrderedMenuOption;

import java.util.Collection;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicReference;

public class ModelTableMenu<M extends Model> extends CommandHandlingMenu {

    // ─── Properties ─── //

    private final Class<M> modelClass;
    private final List<M> models;
    private final int columnCount;
    private MenuResponse pendingCommandMenuResponse;

    // ─── Constructors ─── //

    public ModelTableMenu(Class<M> modelClass, Collection<M> models) {
        this.modelClass = modelClass;
        this.models = List.copyOf(models);
        this.columnCount = ModelTableFormatter.getColumnCount(this.modelClass);
    }

    // ─── Overrides & inheritance ─── //

    @Override
    public void display() {
        ModelTableFormatter.forList(this.models).display();
    }

    @Override
    protected void handleCommand(Command command) throws ExitProgramException, ExitInputPromptException, UnhandledCommandException {
        if (command instanceof SortColumnCommand sortColumnCommand) {
            for (int columnIndex : sortColumnCommand.getSortOrders().keySet()) {
                if (columnIndex < 1 || columnIndex > this.columnCount) {
                    System.out.println("L'index de colonne '%d' est invalide. Veuillez entrer un nombre entre 1 et %d.".formatted(columnIndex, this.columnCount));
                    throw new ExitInputPromptException();
                }
            }

            this.pendingCommandMenuResponse = new MenuResponse(sortColumnCommand);
            throw new ExitInputPromptException();
        }

        super.handleCommand(command);
    }

    @Override
    protected MenuResponse askingValidInputLoopHandle(AtomicReference<String> validInput, AtomicReference<OrderedMenuOption> selectedOption) {
        Scanner scanner = new Scanner(System.in);

        try {
            KinomichiFunctions.promptInput(scanner, (_, command) -> this.handleCommand(command), input -> {
                throw new InvalidInputFormMenuException("L'entrée '%s' n'est pas une commande.".formatted(input));
            }, this::beforeInput, this::afterEveryInput);
        } catch (GoBackException _) {
            return new MenuResponse(new GoBackEvent());
        } catch (ExitProgramException _) {
            return new MenuResponse(new ExitProgramEvent());
        } catch (HookInterruptException hookInterruptException) {
            return hookInterruptException.getMenuResponse();
        } catch (ExitInputPromptException _) {
            return this.pendingCommandMenuResponse;
        }

        return null;
    }

}
