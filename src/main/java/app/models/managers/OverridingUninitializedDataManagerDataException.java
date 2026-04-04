package app.models.managers;

public class OverridingUninitializedDataManagerDataException extends DataManagerException {

    // ─── Properties ─── //

    private final Class<? extends DataManager<?>> dataManagerClass;

    // ─── Constructors ─── //

    public OverridingUninitializedDataManagerDataException(Class<? extends DataManager<?>> dataManagerClass) {
        this.dataManagerClass = dataManagerClass;
    }

    public OverridingUninitializedDataManagerDataException(Class<? extends DataManager<?>> dataManagerClass, String message) {
        super(message);
        this.dataManagerClass = dataManagerClass;
    }

    public OverridingUninitializedDataManagerDataException(Class<? extends DataManager<?>> dataManagerClass, Throwable cause) {
        super(cause);
        this.dataManagerClass = dataManagerClass;
    }

    public OverridingUninitializedDataManagerDataException(Class<? extends DataManager<?>> dataManagerClass, String message, Throwable cause) {
        super(message, cause);
        this.dataManagerClass = dataManagerClass;
    }

    // ─── Getters ─── //

    public Class<? extends DataManager<?>> getDataManagerClass() {
        return dataManagerClass;
    }

}
