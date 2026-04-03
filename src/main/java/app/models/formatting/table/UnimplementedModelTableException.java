package app.models.formatting.table;

import app.models.Model;

public class UnimplementedModelTableException extends ModelTableException {

    // ─── Properties ─── //

    private final Class<? extends Model> modelClass;

    // ─── Constructors ─── //

    public UnimplementedModelTableException(Class<? extends Model> modelClass) {
        super("Aucun ModelTable n'a été implémentée pour le modèle '%s'".formatted(modelClass.getSimpleName()));
        this.modelClass = modelClass;
    }

    // ─── Getters ─── //

    public Class<? extends Model> getModelClass() {
        return this.modelClass;
    }

}
