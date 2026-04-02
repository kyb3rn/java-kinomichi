package utils.io.commands;

import utils.io.commands.exceptions.BadCommandArgumentFormatException;
import utils.io.commands.exceptions.CommandArgumentsException;

public class CommandArgument {

    private String value;

    public CommandArgument(String value) throws CommandArgumentsException {
        this.setValue(value);
    }

    public String getValue() {
        return this.value;
    }

    public void setValue(String value) throws CommandArgumentsException {
        if (value == null || value.isBlank()) {
            throw new BadCommandArgumentFormatException("Un argument de commande ne peut pas être vide");
        }

        this.value = value;
    }

}
