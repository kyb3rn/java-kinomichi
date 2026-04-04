package app.models.formatting;

import utils.io.text_formatting.TextAlignment;
import utils.io.text_formatting.TextColor;
import utils.io.text_formatting.TextFormattingOptions;
import utils.io.text_formatting.TextStyle;

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
