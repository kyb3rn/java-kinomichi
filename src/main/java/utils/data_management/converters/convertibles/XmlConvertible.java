package utils.data_management.converters.convertibles;

import utils.data_management.converters.CustomSerializable;
import utils.data_management.parsing.ParserException;

public interface XmlConvertible extends CustomSerializable {

    void parseXml(String xml) throws ParserException;

    String toXml();

}
