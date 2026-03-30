package app.models.managers;

import app.models.Model;
import app.models.ModelException;
import app.models.ModelReference;
import utils.data_management.FileType;
import utils.data_management.converters.CustomSerializable;
import utils.data_management.converters.Hydratable;
import utils.data_management.converters.convertibles.CsvConvertible;
import utils.data_management.converters.convertibles.JsonConvertible;
import utils.data_management.converters.convertibles.XmlConvertible;
import utils.data_management.converters.writers.CsvWriter;
import utils.data_management.converters.writers.FileWriter;
import utils.data_management.converters.writers.JsonWriter;
import utils.data_management.converters.writers.XmlWriter;
import utils.io.helpers.Functions;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.List;

public abstract class DataManager<T extends CustomSerializable> implements Hydratable<T> {

    // ─── Properties ─── //

    protected static final Path DATA_FOLDER = new File(System.getProperty("user.dir")).toPath().resolve("data");

    protected FileType defaultFileType = FileType.JSON;
    protected final String fileName = Functions.toSnakeCase(this.getClass().getSimpleName().replace("DataManager", ""));
    protected List<Model> pendingModels;
    protected boolean initialized = false;
    protected boolean unsavedChanges = false;

    // ─── Getters ─── //

    public boolean isInitialized() {
        return this.initialized;
    }

    // ─── Special getters ─── //

    protected Path getFilePath() {
        return DATA_FOLDER.resolve(this.fileName + this.defaultFileType.getExtension());
    }

    // ─── Utility methods ─── //

    public abstract void init() throws LoadDataManagerDataException;

    public abstract void export() throws DataManagerException, ModelException;

    public abstract void export(FileType fileType) throws DataManagerException, ModelException;

    protected void export(T data) throws DataManagerException {
        this.export(this.defaultFileType, data);
    }

    @SuppressWarnings("unchecked")
    protected void export(FileType fileType, T data) throws DataManagerException {
        FileWriter<T> fileWriter = switch (fileType) {
            case JSON -> {
                if (data instanceof JsonConvertible) {
                    yield (FileWriter<T>) new JsonWriter<>();
                } else {
                    throw new DataManagerException("La classe de données de type '%s' n'implémente pas l'interface JsonConvertible".formatted(data.getClass().getSimpleName()));
                }
            }
            case CSV -> {
                if (data instanceof CsvConvertible) {
                    yield (FileWriter<T>) new CsvWriter<>();
                } else {
                    throw new DataManagerException("La classe de données de type '%s' n'implémente pas l'interface CsvConvertible".formatted(data.getClass().getSimpleName()));
                }
            }
            case XML -> {
                if (data instanceof XmlConvertible) {
                    yield (FileWriter<T>) new XmlWriter<>();
                } else {
                    throw new DataManagerException("La classe de données de type '%s' n'implémente pas l'interface XmlConvertible".formatted(data.getClass().getSimpleName()));
                }
            }
            default -> throw new DataManagerException("Ce type de fichier ('%s') n'est pas implémenté pour la classe de données '%s'".formatted(fileType.getExtension(), data.getClass().getSimpleName()));
        };

        fileWriter.write(data);

        try {
            fileWriter.writeFile(this.getFilePath().toString());
        } catch (IOException e) {
            throw new DataManagerException("Impossible d'écrire dans le fichier '%s'".formatted(this.getFilePath()));
        }
    }

    public abstract int count();

    public void resolveReferences() throws ModelException {
        if (this.pendingModels == null || this.pendingModels.isEmpty()) {
            this.initialized = true;
            return;
        }

        // Cascade: load and resolve dependent managers first
        Class<?> modelClass = this.pendingModels.getFirst().getClass();
        for (Field field : modelClass.getDeclaredFields()) {
            ModelReference ref = field.getAnnotation(ModelReference.class);
            if (ref != null) {
                try {
                    DataManager<?> depManager = DataManagers.initAndGet(ref.manager());
                    depManager.resolveReferences();
                } catch (LoadDataManagerDataException e) {
                    throw new ModelException("Impossible de charger le manager dépendant '%s'".formatted(ref.manager().getSimpleName()));
                }
            }
        }

        try {
            for (Model model : this.pendingModels) {
                DataManagers.resolveModelReferences(model);

                if (!model.isValid()) {
                    throw new ModelException("Un des objets %s résolus n'est pas valide".formatted(model.getClass().getSimpleName()));
                }
            }

            for (Model model : this.pendingModels) {
                this.addResolvedModel(model);
            }

            this.initialized = true;
        } finally {
            this.pendingModels = null;
        }
    }

    protected void addResolvedModel(Model model) throws ModelException {}

}
