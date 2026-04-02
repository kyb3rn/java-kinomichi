package utils.io.commands.list;

import utils.io.commands.*;
import utils.io.commands.exceptions.BadCommandArgumentFormatException;
import utils.io.commands.exceptions.CommandArgumentsException;
import utils.io.commands.exceptions.TooFewCommandArgumentsException;

import java.util.*;

public class SortColumnCommand extends Command {

    private final LinkedHashMap<Integer, SortOrder> sortOrders = new LinkedHashMap<>();

    public SortColumnCommand(ArrayList<CommandArgument> arguments) throws CommandArgumentsException {
        if (arguments.isEmpty()) {
            throw new TooFewCommandArgumentsException();
        }

        this.addArguments(arguments);
    }

    @Override
    protected void addArgument(CommandArgument argument) throws CommandArgumentsException {
        String[] parts = argument.getValue().split(":");
        if (parts.length > 2) {
            throw new BadCommandArgumentFormatException("Un argument de tri de colonne doit être au format [0-9]+(:(ASC|DESC))?");
        } else {
            int columnIndex;
            try {
                columnIndex = Integer.parseInt(parts[0]);
            } catch (NumberFormatException e) {
                throw new BadCommandArgumentFormatException("Un argument de tri de colonne doit être au format [0-9]+(:(ASC|DESC))?");
            }

            SortOrder sortOrder = (parts.length == 1) ? SortOrder.ASCENDING : SortOrder.fromString(parts[1]);

            this.sortOrders.put(columnIndex, sortOrder);
        }
    }

    @Override
    protected void addArguments(ArrayList<CommandArgument> arguments) throws CommandArgumentsException {
        for (CommandArgument argument : arguments) {
            this.addArgument(argument);
        }
    }

    public LinkedHashMap<Integer, SortOrder> getSortOrders() {
        return this.sortOrders;
    }

    public enum SortOrder {

        ASCENDING,
        DESCENDING;

        public static SortOrder fromString(String sortOrder) {
            if (sortOrder == null) {
                return null;
            }

            ArrayList<String> ascendingWritingOptions = new ArrayList<>(List.of(
                "A", "ASC", "ASCENDING"
            ));
            ArrayList<String> descendingWritingOptions = new ArrayList<>(List.of(
                "D", "DESC", "DESCENDING"
            ));

            if (ascendingWritingOptions.contains(sortOrder.toUpperCase())) {
                return ASCENDING;
            } else if (descendingWritingOptions.contains(sortOrder.toUpperCase())) {
                return DESCENDING;
            } else {
                return null;
            }
        }

    }

}
