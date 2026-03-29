package utils.data_management.converters.readers;

import utils.data_management.converters.CustomSerializable;
import utils.data_management.converters.writers.Writer;
import utils.data_management.parsing.ParserException;

public abstract class StringReader<T extends CustomSerializable> extends Reader<T> {

    // ─── Constructors ─── //

    public StringReader(Writer<T> writer) {
        super(writer);
    }

    // ─── Utility methods ─── //

    protected abstract void parse(String data, T parsedObject) throws ParserException;

}
