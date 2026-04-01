package utils.io.commands;

public class CommandArgument {

    private String value;

    public CommandArgument(String value) throws CommandArgumentException {
        this.setValue(value);
    }

    public String getValue() {
        return this.value;
    }

    public void setValue(String value) throws CommandArgumentException {
        if (value == null || value.isBlank()) {
            throw new BadCommandArgumentFormatException("Un argument de commande ne peut pas être vide");
        }

        this.value = value;
    }

}
