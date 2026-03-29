package app.models.formatting;

import utils.io.helpers.texts.formatting.TextFormattingOptions;
import utils.io.helpers.texts.formatting.TextStyle;

public class ModelKeyTextFormattingPreset extends ModelTextFormattingPreset {

    // ─── Constructors ─── //

    public ModelKeyTextFormattingPreset() {
        super(new TextFormattingOptions()
                .addStyle(TextStyle.UNDERLINE));
    }

}
