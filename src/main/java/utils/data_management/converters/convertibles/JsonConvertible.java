package utils.data_management.converters.convertibles;

import utils.data_management.converters.CustomSerializable;
import utils.data_management.parsing.ParserException;

public interface JsonConvertible extends CustomSerializable {

    void parseJson(String json) throws ParserException;

    String toJson();

}
