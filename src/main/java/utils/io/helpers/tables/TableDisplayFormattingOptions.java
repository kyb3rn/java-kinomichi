package utils.io.helpers.tables;

import utils.io.helpers.texts.formatting.TextAlignement;
import utils.io.helpers.texts.formatting.TextBackgroundColor;
import utils.io.helpers.texts.formatting.TextColor;
import utils.io.helpers.texts.formatting.TextFormattingPreset;
import utils.io.helpers.texts.formatting.TextStyle;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface TableDisplayFormattingOptions {
    Class<? extends TextFormattingPreset> preset() default TextFormattingPreset.class;
    TextAlignement alignment() default TextAlignement.LEFT;
    TextColor color() default TextColor.NONE;
    TextBackgroundColor backgroundColor() default TextBackgroundColor.NONE;
    TextStyle[] styles() default {};
}
