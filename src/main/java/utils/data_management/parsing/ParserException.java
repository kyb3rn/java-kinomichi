package utils.data_management.parsing;

public class ParserException extends RuntimeException {

    // ─── Constructors ─── //

    public ParserException(String message) {
        super(message);
    }

    public ParserException(Throwable cause) {
        super(cause);
    }

    public ParserException(String message, Throwable cause) {
        super(message, cause);
    }

}
