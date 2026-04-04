package app.models.formatting;

import utils.io.text_formatting.TextFormattingOptions;
import utils.io.text_formatting.TextFormattingPreset;

public abstract class ModelTextFormattingPreset extends TextFormattingPreset {

    // ─── Constructors ─── //

    protected ModelTextFormattingPreset(TextFormattingOptions formattingOptions) {
        super(formattingOptions);
    }

}
