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
import utils.data_management.converters.readers.JsonReader;
import utils.data_management.converters.writers.*;
import utils.helpers.Functions;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;

public abstract class DataManager<T extends CustomSerializable> implements Hydratable<T> {

    // ─── Properties ─── //

    protected static final Path DATA_FOLDER = new File(System.getProperty("user.dir")).toPath().resolve("data");

    protected FileType defaultFileType = FileType.JSON;
    protected final String fileName = Functions.toSnakeCase(this.getModelSimpleName());
    protected List<Model> pendingModels;
    protected boolean initialized = false;

    protected boolean unsavedChanges = false;

    // ─── Getters ─── //

    public boolean isInitialized() {
        return this.initialized;
    }

    // ─── Special getters ─── //

    public String getModelSimpleName() {
        return this.getClass().getSimpleName().replace("DataManager", "");
    }

    protected Path getFilePath() {
        return DATA_FOLDER.resolve(this.fileName + this.defaultFileType.getExtension());
    }

    public boolean hasUnsavedChanges() {
        return this.unsavedChanges;
    }

    // ─── Utility methods ─── //

    @SuppressWarnings("unchecked")
    public Class<? extends Model> getModelClass() {
        String managerPackage = this.getClass().getPackageName();
        String parentPackage = managerPackage.substring(0, managerPackage.lastIndexOf('.'));
        String modelName = this.getModelSimpleName();

        try {
            return (Class<? extends Model>) Class.forName(parentPackage + "." + modelName);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Impossible de résoudre la classe Model pour '%s'".formatted(this.getClass().getSimpleName()), e);
        }
    }

    public abstract Collection<? extends Model> getModels();

    public abstract void init() throws LoadDataManagerDataException;

    @SuppressWarnings("unchecked")
    protected <D extends CustomSerializable & JsonConvertible> void defaultJsonInit(D modelData) throws LoadDataManagerDataException {
        if (!this.isInitialized()) {
            DataWriter<D> dataWriter = new DataWriter<>();
            JsonReader<D> jsonReader = new JsonReader<>(dataWriter);

            String filePath = this.getFilePath().toString();
            try {
                jsonReader.readFile(filePath, modelData);
            } catch (Exception e) {
                throw new LoadDataManagerDataException("Les données du manager '%s' n'ont pas pu être lues dans le fichier '%s'".formatted(this.getClass().getSimpleName(), filePath), e);
            }

            try {
                dataWriter.write(modelData, (Hydratable<D>) this);
            } catch (Exception e) {
                throw new LoadDataManagerDataException("Les données lues dans le fichier '%s' n'ont pas pu être enregistrées dans le manager '%s'".formatted(this.getClass().getSimpleName(), filePath), e);
            }
        }
    }

    public abstract void export() throws DataManagerException, ModelException;

    public abstract void export(FileType fileType) throws DataManagerException, ModelException;

    protected void export(T data) throws DataManagerException {
        this.export(this.defaultFileType, data);
    }

    @SuppressWarnings("unchecked")
    protected void export(FileType fileType, T data) throws DataManagerException {
        if (this.isInitialized()) {
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
                case null -> throw new DataManagerException("Le type de fichier ne peut pas être nul");
                default -> throw new DataManagerException("Ce type de fichier ('%s') n'est pas implémenté pour la classe de données '%s'".formatted(fileType.getExtension(), data.getClass().getSimpleName()));
            };

            fileWriter.write(data);

            try {
                fileWriter.writeFile(this.getFilePath().toString());
                this.unsavedChanges = false;
            } catch (IOException e) {
                throw new DataManagerException("Impossible d'écrire dans le fichier '%s'".formatted(this.getFilePath()), e);
            }
        } else {
            throw new OverridingUninitializedDataManagerDataException(AddressDataManager.class);
        }
    }

    public abstract int count();

    public void resolveReferences() throws ModelException, DataManagerException {
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
                    DataManager<?> depManager = DataManagers.get(ref.manager());
                    depManager.resolveReferences();
                } catch (DataManagerException | ModelException e) {
                    throw new ModelException("Impossible de charger le manager dépendant '%s'".formatted(ref.manager().getSimpleName()), e);
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

    protected void addResolvedModel(Model model) throws ModelException, DataManagerException {}

}
