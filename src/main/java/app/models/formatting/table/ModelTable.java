package app.models.formatting.table;

import app.models.Model;
import utils.io.helpers.texts.formatting.FormattedText;
import utils.io.helpers.texts.formatting.TextFormatter;

import java.lang.reflect.Constructor;

public abstract class ModelTable<T extends Model> {

    // ─── Properties ─── //

    private T model;

    // ─── Constructors ─── //

    public ModelTable(T model) throws ModelTableException {
        this.setModel(model);
    }

    // ─── Getters ─── //

    public T getModel() {
        return this.model;
    }

    // ─── Setters ─── //

    public void setModel(T model) throws ModelTableException {
        if (model == null) {
            throw new ModelTableException("La model assigné à la table détaillée ne peut pas être nul");
        }

        this.model = model;
    }

    // ─── Utility methods ─── //

    public static void verifyImplementationExists(Class<? extends Model> modelClass) throws UnimplementedModelTableException {
        fromModelClass(modelClass);
    }

    @SuppressWarnings("unchecked")
    public static <M extends Model> Class<? extends ModelTable<M>> fromModelType(M model) throws UnimplementedModelTableException {
        return (Class<? extends ModelTable<M>>) fromModelClass(model.getClass());
    }

    @SuppressWarnings("unchecked")
    public static Class<? extends ModelTable<?>> fromModelClass(Class<? extends Model> modelClass) throws UnimplementedModelTableException {
        String modelSimpleName = modelClass.getSimpleName();
        String modelTableFullyQualifiedClassName = ModelTable.class.getPackageName() + "." + modelSimpleName + "ModelTable";

        try {
            return (Class<? extends ModelTable<?>>) Class.forName(modelTableFullyQualifiedClassName);
        } catch (ClassNotFoundException e) {
            throw new UnimplementedModelTableException(modelClass);
        }
    }

    @SuppressWarnings("unchecked")
    public static <M extends Model> ModelTable<M> instantiate(Class<? extends ModelTable<M>> modelTableClass, M model) throws ModelTableInstanciationException {
        try {
            Constructor<?> constructor = modelTableClass.getDeclaredConstructors()[0];
            return (ModelTable<M>) constructor.newInstance(model);
        } catch (Exception e) {
            Throwable cause = e.getCause();

            if (cause instanceof ModelTableException modelTableException) {
                throw new ModelTableInstanciationException(modelTableException);
            }

            throw new ModelTableInstanciationException(e);
        }
    }

    protected static FormattedText getNullFormattedText() {
        return TextFormatter.italic(TextFormatter.yellow("null"));
    }

}
