package utils.data_management.converters.writers;

import utils.data_management.converters.convertibles.CsvConvertible;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class CsvWriter<T extends CsvConvertible> extends FileWriter<T> {

    /** Special methods **/

    @Override
    public void writeFile(String filePath) throws IOException {
        List<String[]> lines = this.data.toLines();
        StringBuilder finalCsvStringBuilder = new StringBuilder();
        for (String[] columns : lines) {
            finalCsvStringBuilder.append(String.join(";", columns));
            finalCsvStringBuilder.append("\n");
        }
        Files.writeString(Path.of(filePath), finalCsvStringBuilder.toString());
    }

}
