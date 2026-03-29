package utils.data_management.converters.writers;

import utils.data_management.converters.convertibles.JsonConvertible;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class JsonWriter<T extends JsonConvertible> extends FileWriter<T> {

    // ─── Overrides & inheritance ─── //

    @Override
    public void writeFile(String filePath) throws IOException {
        String json = this.data.toJson();
        Files.writeString(Path.of(filePath), json);
    }

}
