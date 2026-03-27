package utils.data_management;

public enum FileType {
    JSON(".json"),
    CSV(".csv"),
    XML(".xml");

    private final String extension;

    FileType(String extension) {
        this.extension = extension;
    }

    public String getExtension() {
        return this.extension;
    }

}
