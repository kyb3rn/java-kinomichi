package app.models.managers;

import app.models.Model;
import app.models.ModelException;
import app.models.ModelReference;
import utils.helpers.Functions;

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
    private static <M extends DataManager<?>> M initAndGet(Class<M> clazz) throws LoadDataManagerDataException {
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
                throw new LoadDataManagerDataException("Impossible d'instancier le manager '%s'".formatted(clazz.getSimpleName()), e);
            }
        }
        M instance = (M) instances.get(clazz);
        instance.init();
        return instance;
    }

    public static void initAndResolveReferencesWithThrow(Class<? extends DataManager<?>> clazz) throws DataManagerException, ModelException {
        DataManager<?> manager = initAndGet(clazz);
        manager.resolveReferences();
    }

    public static void initAll() {
        initAndResolveReferencesOf(
            CountryDataManager.class,
            PersonDataManager.class,
            AddressDataManager.class,
            ClubDataManager.class,
            CampDataManager.class,
            SessionDataManager.class,
            LodgingDataManager.class,
            LodgingReservationDataManager.class,
            DinnerDataManager.class,
            DinnerReservationDataManager.class,
            InvitationDataManager.class,
            AffiliationDataManager.class,
            SessionTrainerDataManager.class,
            SessionRegistrationDataManager.class,
            CampDiscountDataManager.class
        );
    }

    @SafeVarargs
    public static void initAndResolveReferencesOf(Class<? extends DataManager<?>>... classes) {
        // Pass 1: instantiate managers (primitive data only)
        List<DataManager<?>> loadedManagers = new ArrayList<>();
        for (var clazz : classes) {
            try {
                DataManager<?> manager = initAndGet(clazz);
                loadedManagers.add(manager);
            } catch (LoadDataManagerDataException _) {
                System.out.println(Functions.styleAsErrorMessage("Le pré-chargement du manager '%s' n'a pas pu être effectué.".formatted(clazz.getSimpleName())));
            }
        }

        // Pass 2: resolve references via reflection
        for (DataManager<?> manager : loadedManagers) {
            try {
                manager.resolveReferences();
            } catch (DataManagerException | ModelException e) {
                System.out.println(Functions.styleAsErrorMessage("La résolution des références du manager '%s' a échoué: %s".formatted(manager.getClass().getSimpleName(), e.getMessage())));
            }
        }

        // Pass 3: cross-DataManager validation + mark as initialized
        for (DataManager<?> manager : loadedManagers) {
            if (!manager.isInitialized() && manager.dataLoaded) {
                manager.initialized = true;
                try {
                    manager.validateResolvedModels();
                } catch (DataManagerException | ModelException e) {
                    manager.initialized = false;
                    System.out.println(Functions.styleAsErrorMessage("La validation post-chargement du manager '%s' a échoué: %s".formatted(manager.getClass().getSimpleName(), e.getMessage())));
                }
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
                throw new ModelException("Champ '%s' introuvable sur '%s'".formatted(pendingFieldName, modelClass.getSimpleName()), e);
            }

            // Read the pending value
            Object pendingValue;
            try {
                pendingValue = pendingField.get(model);
            } catch (IllegalAccessException e) {
                throw new ModelException("Impossible de lire '%s' sur '%s'".formatted(pendingFieldName, modelClass.getSimpleName()), e);
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
                throw new ModelException("Impossible d'appeler '%s' sur '%s'".formatted(setterName, modelClass.getSimpleName()), e);
            }
        }
    }

    public static int getCountOf(Class<? extends DataManager<?>> clazz) {
        try {
            return initAndGet(clazz).count();
        } catch (LoadDataManagerDataException _) {
            return 0;
        }
    }

    public static boolean isInitialized(Class<? extends DataManager<?>> clazz) {
        try {
            return initAndGet(clazz).isInitialized();
        } catch (LoadDataManagerDataException _) {
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
        } catch (ClassNotFoundException _) {}

        return false;
    }

    public static List<DataManager<?>> getUnsavedOnes() {
        return instances.values().stream().filter(DataManager::hasUnsavedChanges).toList();
    }

    public static List<DataManager<?>> getBadlyInitializedOnes() {
        List<DataManager<?>> badlyInitializedOnes = new ArrayList<>();

        for (DataManager<?> manager : instances.values()) {
            if (!manager.isInitialized()) {
                badlyInitializedOnes.add(manager);
            }
        }

        return badlyInitializedOnes;
    }

    public static void exportAll() {
        for (DataManager<?> dataManager : instances.values()) {
            if (dataManager.hasUnsavedChanges()) {
                try {
                    dataManager.export();
                } catch (DataManagerException | ModelException _) {
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    public static <M extends DataManager<?>> M get(Class<M> clazz) throws ModelException, DataManagerException {
        initAndResolveReferencesWithThrow(clazz);
        M manager = (M) instances.get(clazz);

        if (!manager.isInitialized() && manager.dataLoaded) {
            manager.initialized = true;
            try {
                manager.validateResolvedModels();
            } catch (Exception e) {
                manager.initialized = false;
                if (e instanceof DataManagerException dme) {
                    throw dme;
                }
                if (e instanceof ModelException me) {
                    throw me;
                }
                throw new DataManagerException("La validation post-chargement du manager '%s' a échoué".formatted(clazz.getSimpleName()), e);
            }
        }

        return manager;
    }

}
