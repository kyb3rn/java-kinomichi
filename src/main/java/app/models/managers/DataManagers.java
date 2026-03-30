package app.models.managers;

import app.models.Model;
import app.models.ModelException;
import app.models.ModelReference;
import utils.io.helpers.Functions;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataManagers {

    // ─── Properties ─── //

    private static final Map<Class<?>, DataManager<?>> instances = new HashMap<>();

    // ─── Utility methods ─── //

    @SuppressWarnings("unchecked")
    public static <M extends DataManager<?>> M initAndGet(Class<M> clazz) throws LoadDataManagerDataException {
        if (!instances.containsKey(clazz)) {
            try {
                Constructor<M> constructor = clazz.getDeclaredConstructor();
                constructor.setAccessible(true);
                M instance = constructor.newInstance();
                instances.put(clazz, instance);
            } catch (Exception e) {
                Throwable cause = e.getCause();
                if (cause instanceof LoadDataManagerDataException lde) {
                    throw lde;
                }
                throw new LoadDataManagerDataException(
                    "Impossible d'instancier le manager '%s'".formatted(clazz.getSimpleName())
                );
            }
        }
        M instance = (M) instances.get(clazz);
        instance.init();
        return instance;
    }

    public static void init(Class<? extends DataManager<?>> clazz) {
        try {
            DataManager<?> manager = initAndGet(clazz);
            manager.resolveReferences();
        } catch (LoadDataManagerDataException e) {
            System.out.printf(Functions.styleAsErrorMessage("Le pré-chargement du manager '%s' n'a pas pu être effectué.%n"), clazz.getSimpleName());
        } catch (ModelException e) {
            System.out.printf(Functions.styleAsErrorMessage("La résolution des références du manager '%s' a échoué: %s%n"), clazz.getSimpleName(), e.getMessage());
        }
    }

    @SafeVarargs
    public static void initAll(Class<? extends DataManager<?>>... classes) {
        // Pass 1: instantiate managers (primitive data only)
        List<DataManager<?>> loadedManagers = new ArrayList<>();
        for (var clazz : classes) {
            try {
                DataManager<?> manager = initAndGet(clazz);
                loadedManagers.add(manager);
            } catch (LoadDataManagerDataException e) {
                System.out.printf(Functions.styleAsErrorMessage("Le pré-chargement du manager '%s' n'a pas pu être effectué.%n"), clazz.getSimpleName());
            }
        }

        // Pass 2: resolve references via reflection
        for (DataManager<?> manager : loadedManagers) {
            try {
                manager.resolveReferences();
            } catch (ModelException e) {
                System.out.printf(Functions.styleAsErrorMessage("La résolution des références du manager '%s' a échoué: %s%n"), manager.getClass().getSimpleName(), e.getMessage());
            }
        }
    }

    public static void resolveModelReferences(Model model) throws ModelException {
        Class<?> modelClass = model.getClass();

        for (Field field : modelClass.getDeclaredFields()) {
            if (!field.isAnnotationPresent(ModelReference.class)) {
                continue;
            }

            String fieldName = field.getName();
            String capitalizedFieldName = fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);

            // Find the pendingXxxPk field
            String pendingFieldName = "pending" + capitalizedFieldName + "Pk";
            Field pendingField;
            try {
                pendingField = modelClass.getDeclaredField(pendingFieldName);
                pendingField.setAccessible(true);
            } catch (NoSuchFieldException e) {
                throw new ModelException("Champ '%s' introuvable sur '%s'".formatted(pendingFieldName, modelClass.getSimpleName()));
            }

            // Read the pending value
            Object pendingValue;
            try {
                pendingValue = pendingField.get(model);
            } catch (IllegalAccessException e) {
                throw new ModelException("Impossible de lire '%s' sur '%s'".formatted(pendingFieldName, modelClass.getSimpleName()));
            }

            // Find and call setXxxFromPk(pendingValue)
            String setterName = "set" + capitalizedFieldName + "FromPk";
            try {
                Method setter = modelClass.getMethod(setterName, pendingField.getType());
                setter.invoke(model, pendingValue);
            } catch (Exception e) {
                Throwable cause = e.getCause();
                if (cause instanceof ModelException me) {
                    throw me;
                }
                throw new ModelException("Impossible d'appeler '%s' sur '%s'".formatted(setterName, modelClass.getSimpleName()));
            }
        }
    }

    public static int getCountOf(Class<? extends DataManager<?>> clazz) {
        try {
            return initAndGet(clazz).count();
        } catch (LoadDataManagerDataException e) {
            return 0;
        }
    }

    public static boolean isInitialized(Class<? extends DataManager<?>> clazz) {
        try {
            return initAndGet(clazz).isInitialized();
        } catch (LoadDataManagerDataException e) {
            return false;
        }
    }

    public static boolean hasDependencies(DataManager<?> manager) {
        String managerPackage = manager.getClass().getPackageName();
        String parentPackage = managerPackage.substring(0, managerPackage.lastIndexOf('.'));
        String modelName = manager.getModelSimpleName();

        try {
            Class<?> modelClass = Class.forName(parentPackage + "." + modelName);
            for (Field field : modelClass.getDeclaredFields()) {
                if (field.isAnnotationPresent(ModelReference.class)) {
                    return true;
                }
            }
        } catch (ClassNotFoundException ignored) {}

        return false;
    }

    public static List<DataManager<?>> getUnsavedOnes() {
        return DataManagers.instances.values().stream().filter(DataManager::hasUnsavedChanges).toList();
    }

    public static List<DataManager<?>> getBadlyInitializedOnes() {
        List<DataManager<?>> badlyInitializedOnes = new ArrayList<>();

        for (DataManager<?> manager : DataManagers.instances.values()) {
            if (!manager.isInitialized()) {
                badlyInitializedOnes.add(manager);
            }
        }

        return badlyInitializedOnes;
    }
}
