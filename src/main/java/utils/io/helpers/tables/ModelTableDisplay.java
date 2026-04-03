package utils.io.helpers.tables;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ModelTableDisplay {
    String name();
    TableDisplayFormattingOptions format() default @TableDisplayFormattingOptions;
    int order();
}
