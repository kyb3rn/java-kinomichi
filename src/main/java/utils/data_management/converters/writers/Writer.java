package utils.data_management.converters.writers;

import java.io.Serializable;

public abstract class Writer<T extends Serializable> {

    // ─── Properties ─── //

    protected T data;

    // ─── Utility methods ─── //

    public void write(T serializable) {
        this.data = serializable;
    }

}

