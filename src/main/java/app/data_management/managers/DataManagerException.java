package app.data_management.managers;

public class DataManagerException extends Exception {

    public DataManagerException(String message) {
        super(message);
    }

    public DataManagerException(Throwable cause) {
        super(cause);
    }

    public DataManagerException(String message, Throwable cause) {
        super(message, cause);
    }

}
