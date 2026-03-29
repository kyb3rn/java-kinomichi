package utils.data_management.converters.readers;

import utils.data_management.converters.convertibles.CsvConvertible;
import utils.data_management.converters.writers.Writer;
import utils.data_management.parsing.StringParserException;

import java.io.IOException;

public class CsvReader<T extends CsvConvertible> extends FileReader<T> {

    // ─── Constructors ─── //

    public CsvReader(Writer<T> writer) {
        super(writer);
    }

    // ─── Utility methods ─── //

    public void readFile(String filePath, T parsedObject, boolean containsHeaders) throws IOException, StringParserException {
        String fileContent = this.getFileContent(filePath);
        this.parse(fileContent, parsedObject, containsHeaders);
    }

    protected void parse(String data, T parsedObject, boolean containsHeaders) throws StringParserException {
        if (containsHeaders) {
            String[] lines = data.split("\\r?\\n");
            for (int i = 1; i < lines.length; i++) {
                String line = lines[i];
                if (line.isBlank()) {
                    continue;
                }

                String[] columns = line.split(";");
                parsedObject.parseLine(columns);
            }
        } else {
            this.parse(data, parsedObject);
        }
    }

    // ─── Overrides & inheritance ─── //

    @Override
    protected void parse(String data, T parsedObject) throws StringParserException {
        String[] lines = data.split("\\r?\\n");
        for (String line : lines) {
            if (line.isBlank()) {
                continue;
            }

            String[] columns = line.split(";");
            parsedObject.parseLine(columns);
        }
    }

}
