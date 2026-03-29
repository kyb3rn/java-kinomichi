package utils.data_management.converters.readers;

import utils.data_management.converters.convertibles.JsonConvertible;
import utils.data_management.converters.writers.Writer;
import utils.data_management.parsing.StringParserException;

public class JsonReader<T extends JsonConvertible> extends FileReader<T> {

    // ─── Constructors ─── //

    public JsonReader(Writer<T> writer) {
        super(writer);
    }

    // ─── Overrides & inheritance ─── //

    @Override
    protected void parse(String data, T parsedObject) throws StringParserException {
        parsedObject.parseJson(data);
    }

}
