package utils.data_management.converters.readers;

import utils.data_management.converters.CustomSerializable;
import utils.data_management.converters.writers.Writer;
import utils.data_management.parsing.StringParserException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public abstract class FileReader<T extends CustomSerializable> extends StringReader<T> {

    /** Constructors **/

    public FileReader(Writer<T> writer) {
        super(writer);
    }

    /** Special methods **/

    public boolean fileExists(String filePath) {
        return new File(filePath).exists();
    }

    public String getFileContent(String filePath) throws IOException {
        return Files.readString(Path.of(filePath));
    }

    public void readFile(String filePath, T parsedObject) throws IOException, StringParserException {
        String fileContent = this.getFileContent(filePath);
        this.parse(fileContent, parsedObject);
    }

}
