package utils.data_management.converters.convertibles.parseables;

import utils.data_management.converters.CustomSerializable;
import utils.data_management.converters.enums.WebsitesForReader;
import utils.data_management.parsing.ParserException;

public interface WebsiteDataParseable extends CustomSerializable {

    void parseWebsiteData(WebsitesForReader site, String data) throws ParserException;

}
