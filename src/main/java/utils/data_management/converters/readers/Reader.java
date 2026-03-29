package utils.data_management.converters.readers;

import utils.data_management.converters.CustomSerializable;
import utils.data_management.converters.writers.Writer;

public abstract class Reader<T extends CustomSerializable> {

    // ─── Properties ─── //

    protected final Writer<T> writer;

    // ─── Constructors ─── //

    public Reader(Writer<T> writer) {
        this.writer = writer;
    }

}
