package utils.data_management;

public enum FileType {
    JSON(".json"),
    CSV(".csv"),
    XML(".xml");

    // ─── Properties ─── //

    private final String extension;

    // ─── Constructors ─── //

    FileType(String extension) {
        this.extension = extension;
    }

    // ─── Getters ─── //

    public String getExtension() {
        return this.extension;
    }

}
