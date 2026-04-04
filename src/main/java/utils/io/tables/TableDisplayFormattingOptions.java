package utils.io.tables;

import utils.io.text_formatting.TextAlignment;
import utils.io.text_formatting.TextBackgroundColor;
import utils.io.text_formatting.TextColor;
import utils.io.text_formatting.TextFormattingPreset;
import utils.io.text_formatting.TextStyle;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface TableDisplayFormattingOptions {
    Class<? extends TextFormattingPreset> preset() default TextFormattingPreset.class;
    TextAlignment alignment() default TextAlignment.NONE;
    TextColor color() default TextColor.NONE;
    TextBackgroundColor backgroundColor() default TextBackgroundColor.NONE;
    TextStyle[] styles() default {};
}
