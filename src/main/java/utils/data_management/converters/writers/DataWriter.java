package utils.data_management.converters.writers;

import utils.data_management.converters.CustomSerializable;
import utils.data_management.converters.Hydratable;

public class DataWriter<T extends CustomSerializable> extends Writer<T> {

    // ─── Utility methods ─── //

    public void write(T dataObject, Hydratable<T> hydratable) throws Exception {
        hydratable.hydrate(dataObject);
    }

}
