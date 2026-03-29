package app.models;

public abstract class Model {

    // ─── Utility methods ─── //

    /**
     * Required because models can be instanced with empty constructor
     */
    public abstract boolean isValid();

}
