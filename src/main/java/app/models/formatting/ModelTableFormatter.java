package app.models.formatting;

import app.models.Model;
import app.models.formatting.table.ModelTable;
import app.models.formatting.table.ModelTableInstanciationException;
import app.models.formatting.table.UnimplementedModelTableException;
import utils.io.helpers.tables.Table;
import utils.io.helpers.tables.ModelTableDisplay;
import utils.io.helpers.tables.TableDisplayFormattingOptions;
import utils.io.helpers.tables.TableOptions;
import utils.io.helpers.texts.formatting.TextAlignment;
import utils.io.helpers.texts.formatting.TextFormattingOptions;
import utils.io.helpers.texts.formatting.TextFormattingPreset;
import utils.io.helpers.texts.formatting.TextStyle;

import java.lang.reflect.Method;
import java.util.*;

public class ModelTableFormatter {

    // ─── Utility methods ─── //

    public static <T extends Model> Table forList(List<T> items) throws EmptyContentModelTableFormatterException, UnimplementedModelTableException, ModelTableInstanciationException {
        if (items == null || items.isEmpty()) {
            throw new EmptyContentModelTableFormatterException("La liste de modèles est vide ou nulle");
        }

        T firstItem = items.getFirst();
        Class<? extends ModelTable<T>> modelTableClass = ModelTable.fromModelType(firstItem);

        List<ModelTable<T>> modelTables = new ArrayList<>();
        for (T item : items) {
            modelTables.add(ModelTable.instantiate(modelTableClass, item));
        }

        List<AnnotatedMethod> tableDisplayAnnotatedMethods = getTableDisplayAnnotatedMethods(modelTableClass);
        Table formatter = new Table();

        for (AnnotatedMethod am : tableDisplayAnnotatedMethods) {
            Table.Column column = new Table.Column(am.annotation().name(), toFormattingOptions(am.annotation().format()));
            for (ModelTable<T> modelTable : modelTables) {
                try {
                    Object value = am.method().invoke(modelTable);
                    column.addValue(Objects.toString(value, ""));
                } catch (Exception _) {
                    column.addValue("");
                }
            }
            formatter.addColumn(column);
        }

        return formatter;
    }

    public static <T extends Model> Table forDetail(T item) throws EmptyContentModelTableFormatterException, UnimplementedModelTableException, ModelTableInstanciationException {
        if (item == null) {
            throw new EmptyContentModelTableFormatterException("Le modèle est nul");
        }

        Class<? extends ModelTable<T>> modelTableClass = ModelTable.fromModelType(item);
        ModelTable<T> modelTable = ModelTable.instantiate(modelTableClass, item);

        List<AnnotatedMethod> tableDisplayAnnotatedMethods = getTableDisplayAnnotatedMethods(modelTableClass);

        EnumSet<TableOptions> options = EnumSet.of(
                TableOptions.SEPARATE_COLUMNS,
                TableOptions.BOX_AROUND
        );
        Table formatter = new Table(options);

        Table.Column keyColumn = new Table.Column(TextAlignment.RIGHT);

        for (AnnotatedMethod am : tableDisplayAnnotatedMethods) {
            keyColumn.addValue(am.annotation().name());
        }

        Table.Column valueColumn = new Table.Column(new TextFormattingOptions());

        for (AnnotatedMethod am : tableDisplayAnnotatedMethods) {
            try {
                Object value = am.method().invoke(modelTable);
                valueColumn.addValue(Objects.toString(value, ""));
            } catch (Exception _) {
                valueColumn.addValue("");
            }
        }

        formatter.addColumn(keyColumn);
        formatter.addColumn(valueColumn);

        return formatter;
    }

    public static int getColumnCount(Class<? extends Model> clazz) throws UnimplementedModelTableException {
        Class<? extends ModelTable<?>> modelTableClass = ModelTable.fromModelClass(clazz);
        return getTableDisplayAnnotatedMethods(modelTableClass).size();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <T extends Model> Comparator<T> comparatorForColumn(Class<T> clazz, int columnIndex) throws UnimplementedModelTableException {
        Class<? extends ModelTable<?>> modelTableClass = ModelTable.fromModelClass(clazz);
        List<AnnotatedMethod> tableDisplayAnnotatedMethods = getTableDisplayAnnotatedMethods(modelTableClass);
        Method method = tableDisplayAnnotatedMethods.get(columnIndex).method();

        return (item1, item2) -> {
            try {
                ModelTable modelTable1 = ModelTable.instantiate((Class) modelTableClass, item1);
                ModelTable modelTable2 = ModelTable.instantiate((Class) modelTableClass, item2);

                Object value1 = method.invoke(modelTable1);
                Object value2 = method.invoke(modelTable2);

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
            ModelTableDisplay annotation = method.getAnnotation(ModelTableDisplay.class);
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

        if (annotation.alignment() != TextAlignment.NONE) {
            options.setAlignment(annotation.alignment());
        }

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

    private record AnnotatedMethod(Method method, ModelTableDisplay annotation) {}

}
