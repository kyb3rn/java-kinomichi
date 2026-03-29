package utils.data_management.converters.readers;

import utils.data_management.converters.CustomSerializable;
import utils.data_management.converters.Hydratable;
import utils.data_management.converters.writers.Writer;

public class DataReader<T extends CustomSerializable> extends Reader<T> {

    // ─── Constructors ─── //

    public DataReader(Writer<T> writer) {
        super(writer);
    }

    // ─── Utility methods ─── //

    public void parse(Hydratable<T> hydratableObject) throws Exception {
        T dataObject = hydratableObject.dehydrate();
        this.writer.write(dataObject);
    }

}
