package utils.data_management.converters.readers;

import utils.data_management.converters.convertibles.parseables.WebsiteDataParseable;
import utils.data_management.converters.enums.WebsitesForReader;
import utils.data_management.converters.writers.Writer;
import utils.data_management.parsing.StringParserException;

import java.io.IOException;
import java.net.URISyntaxException;

public class HtmlReader<T extends WebsiteDataParseable> extends WebsiteReader<T> {

    /** Properties **/

    private final WebsitesForReader site;

    /** Constructors **/

    public HtmlReader(Writer<T> writer, WebsitesForReader site) {
        super(writer);
        this.site = site;
    }

    /** Overrides & inheritance **/

    @Override
    protected void parse(String data, T parsedObject) throws StringParserException {
        parsedObject.parseWebsiteData(this.site, data);
    }

    public void readWebsite(String url, T modelData) throws StringParserException, IOException, URISyntaxException, InterruptedException {
        String data = switch (this.site) {
            default -> this.scrap(url);
        };

        this.parse(data, modelData);
    }

}
