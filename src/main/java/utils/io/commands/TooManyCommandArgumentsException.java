package utils.io.commands;

public class TooManyCommandArgumentsException extends CommandArgumentsAmountException {

    public TooManyCommandArgumentsException() {
    }

    public TooManyCommandArgumentsException(String message) {
        super(message);
    }

    public TooManyCommandArgumentsException(Throwable cause) {
        super(cause);
    }

    public TooManyCommandArgumentsException(String message, Throwable cause) {
        super(message, cause);
    }

}
