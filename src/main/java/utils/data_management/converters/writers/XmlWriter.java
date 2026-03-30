package utils.data_management.converters.writers;

import utils.data_management.converters.convertibles.XmlConvertible;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class XmlWriter<T extends XmlConvertible> extends FileWriter<T> {

    // ─── Overrides & inheritance ─── //

    @Override
    public void writeFile(String filePath) throws IOException {
        String xml = this.data.toXml();
        Files.writeString(Path.of(filePath), xml);
    }

}
