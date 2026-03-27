package utils.data_management.converters.convertibles;

import utils.data_management.converters.CustomSerializable;
import utils.data_management.parsing.ParserException;

import java.util.List;

public interface CsvConvertible extends CustomSerializable {

    void parseLine(String[] columns) throws ParserException;

    List<String[]> toLines();

}
