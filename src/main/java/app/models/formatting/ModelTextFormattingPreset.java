package app.models.formatting;

import utils.io.helpers.texts.formatting.TextFormattingOptions;
import utils.io.helpers.texts.formatting.TextFormattingPreset;

public abstract class ModelTextFormattingPreset extends TextFormattingPreset {

    // ─── Constructors ─── //

    protected ModelTextFormattingPreset(TextFormattingOptions formattingOptions) {
        super(formattingOptions);
    }

}
