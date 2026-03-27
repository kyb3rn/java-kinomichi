package app.models;

public abstract class Model {

    /**
     * Required because models can be instanced with empty constructor
     */
    public abstract boolean isValid();

}
