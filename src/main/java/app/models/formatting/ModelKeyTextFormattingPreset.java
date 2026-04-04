package app.models.formatting;

import utils.io.text_formatting.TextFormattingOptions;
import utils.io.text_formatting.TextStyle;

public class ModelKeyTextFormattingPreset extends ModelTextFormattingPreset {

    // ─── Constructors ─── //

    public ModelKeyTextFormattingPreset() {
        super(new TextFormattingOptions()
            .addStyle(TextStyle.UNDERLINE));
    }

    public ModelKeyTextFormattingPreset(TextFormattingOptions textFormattingOptions) {
        super(textFormattingOptions);
    }

}
