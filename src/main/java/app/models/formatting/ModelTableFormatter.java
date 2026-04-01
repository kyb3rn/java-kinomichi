package app.models.formatting;

import app.models.Model;
import utils.io.helpers.tables.Table;
import utils.io.helpers.tables.TableDisplay;
import utils.io.helpers.tables.TableDisplayFormattingOptions;
import utils.io.helpers.tables.TableOptions;
import utils.io.helpers.texts.formatting.TextAlignement;
import utils.io.helpers.texts.formatting.TextFormattingOptions;
import utils.io.helpers.texts.formatting.TextFormattingPreset;
import utils.io.helpers.texts.formatting.TextStyle;

import java.lang.reflect.Method;
import java.util.*;

public class ModelTableFormatter {

    // ─── Utility methods ─── //

    public static <T extends Model> Table forList(List<T> items) {
        if (items.isEmpty()) {
            return new Table();
        }

        List<AnnotatedMethod> tableDisplayAnnotatedMethods = getTableDisplayAnnotatedMethods(items.getFirst().getClass());
        Table formatter = new Table();

        for (AnnotatedMethod am : tableDisplayAnnotatedMethods) {
            Table.Column column = new Table.Column(am.annotation().name(), toFormattingOptions(am.annotation().format()));
            for (T item : items) {
                try {
                    Object value = am.method().invoke(item);
                    column.addValue(Objects.toString(value, ""));
                } catch (Exception _) {
                    column.addValue("");
                }
            }
            formatter.addColumn(column);
        }

        return formatter;
    }

    public static <T extends Model> Table forDetail(T item) {
        List<AnnotatedMethod> tableDisplayAnnotatedMethods = getTableDisplayAnnotatedMethods(item.getClass());

        EnumSet<TableOptions> options = EnumSet.of(
                TableOptions.SEPARATE_COLUMNS,
                TableOptions.BOX_AROUND
        );
        Table formatter = new Table(options);

        Table.Column keyColumn = new Table.Column(TextAlignement.RIGHT);

        for (AnnotatedMethod am : tableDisplayAnnotatedMethods) {
            keyColumn.addValue(am.annotation().name());
        }

        Table.Column valueColumn = new Table.Column(new TextFormattingOptions());

        for (AnnotatedMethod am : tableDisplayAnnotatedMethods) {
            try {
                Object value = am.method().invoke(item);
                valueColumn.addValue(Objects.toString(value, ""));
            } catch (Exception _) {
                valueColumn.addValue("");
            }
        }

        formatter.addColumn(keyColumn);
        formatter.addColumn(valueColumn);

        return formatter;
    }

    public static int getColumnCount(Class<? extends Model> clazz) {
        return getTableDisplayAnnotatedMethods(clazz).size();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <T extends Model> Comparator<T> comparatorForColumn(Class<T> clazz, int columnIndex) {
        List<AnnotatedMethod> tableDisplayAnnotatedMethods = getTableDisplayAnnotatedMethods(clazz);
        Method method = tableDisplayAnnotatedMethods.get(columnIndex).method();

        return (item1, item2) -> {
            try {
                Object value1 = method.invoke(item1);
                Object value2 = method.invoke(item2);

                if (value1 == null && value2 == null) return 0;
                if (value1 == null) return 1;
                if (value2 == null) return -1;

                if (value1 instanceof Comparable comparable1) {
                    return comparable1.compareTo(value2);
                }

                return value1.toString().compareTo(value2.toString());
            } catch (Exception _) {
                return 0;
            }
        };
    }

    private static List<AnnotatedMethod> getTableDisplayAnnotatedMethods(Class<?> clazz) {
        List<AnnotatedMethod> result = new ArrayList<>();

        for (Method method : clazz.getMethods()) {
            TableDisplay annotation = method.getAnnotation(TableDisplay.class);
            if (annotation != null) {
                result.add(new AnnotatedMethod(method, annotation));
            }
        }

        result.sort(Comparator.comparingInt(am -> am.annotation().order()));
        return result;
    }

    private static TextFormattingOptions toFormattingOptions(TableDisplayFormattingOptions annotation) {
        TextFormattingOptions options;

        Class<? extends TextFormattingPreset> presetClass = annotation.preset();
        if (presetClass != TextFormattingPreset.class) {
            try {
                options = presetClass.getDeclaredConstructor().newInstance().getFormattingOptions();
            } catch (Exception _) {
                options = new TextFormattingOptions();
            }
        } else {
            options = new TextFormattingOptions();
        }

        options.setAlignment(annotation.alignment());

        if (annotation.color() != utils.io.helpers.texts.formatting.TextColor.NONE) {
            options.setColor(annotation.color());
        }

        if (annotation.backgroundColor() != utils.io.helpers.texts.formatting.TextBackgroundColor.NONE) {
            options.setBackgroundColor(annotation.backgroundColor());
        }

        for (TextStyle style : annotation.styles()) {
            options.addStyle(style);
        }

        return options;
    }

    // ─── Sub classes ─── //

    private record AnnotatedMethod(Method method, TableDisplay annotation) {}

}
