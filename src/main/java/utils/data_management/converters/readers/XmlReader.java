package utils.data_management.converters.readers;

import utils.data_management.converters.convertibles.XmlConvertible;
import utils.data_management.converters.writers.Writer;
import utils.data_management.parsing.StringParserException;

public class XmlReader<T extends XmlConvertible> extends FileReader<T> {

    /** Constructors **/

    public XmlReader(Writer<T> writer) {
        super(writer);
    }

    /** Overrides & inheritance **/

    @Override
    protected void parse(String data, T parsedObject) throws StringParserException {
        parsedObject.parseXml(data);
    }

}
