package utils.io.helpers.texts.formatting;

public abstract class TextFormattingPreset {

    // ─── Properties ─── //

    private final TextFormattingOptions formattingOptions;

    // ─── Constructors ─── //

    protected TextFormattingPreset(TextFormattingOptions formattingOptions) {
        this.formattingOptions = formattingOptions;
    }

    // ─── Getters ─── //

    public TextFormattingOptions getFormattingOptions() {
        return this.formattingOptions;
    }

}
