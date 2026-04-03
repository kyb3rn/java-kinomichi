package app.models.formatting;

import utils.io.helpers.texts.formatting.TextAlignment;
import utils.io.helpers.texts.formatting.TextColor;
import utils.io.helpers.texts.formatting.TextFormattingOptions;
import utils.io.helpers.texts.formatting.TextStyle;

public class ModelPrimaryKeyTextFormattingPreset extends ModelKeyTextFormattingPreset {

    // ─── Constructors ─── //

    public ModelPrimaryKeyTextFormattingPreset() {
        super(
            new TextFormattingOptions()
                .addStyle(TextStyle.UNDERLINE)
                .addStyle(TextStyle.BOLD)
                .setColor(TextColor.BLUE)
                .setAlignment(TextAlignment.RIGHT)
        );
    }

}
