package utils.data_management.converters.writers;

import java.io.IOException;
import java.io.Serializable;

public abstract class FileWriter<T extends Serializable> extends Writer<T> {

    public abstract void writeFile(String filePath) throws IOException;

}
