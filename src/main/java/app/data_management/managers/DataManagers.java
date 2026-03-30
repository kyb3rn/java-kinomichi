package app.data_management.managers;

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
    public static <M extends DataManager<?>> M get(Class<M> clazz) throws LoadDataManagerDataException {
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
        return (M) instances.get(clazz);
    }

    @SafeVarargs
    public static void initAll(Class<? extends DataManager<?>>... classes) {
        // Passe 1: instancier les managers (données primitives uniquement)
        List<DataManager<?>> loadedManagers = new ArrayList<>();
        for (var clazz : classes) {
            try {
                DataManager<?> manager = get(clazz);
                loadedManagers.add(manager);
            } catch (LoadDataManagerDataException e) {
                System.out.printf(Functions.styleAsErrorMessage("Le pré-chargement du manager '%s' n'a pas pu être effectué.%n"), clazz.getSimpleName());
            }
        }

        // Passe 2: résoudre les références par réflexion
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
            String typeName = field.getType().getSimpleName();
            String capitalizedFieldName = fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);

            // Trouver le champ pendingXxxPk
            String pendingFieldName = "pending" + capitalizedFieldName + "Pk";
            Field pendingField;
            try {
                pendingField = modelClass.getDeclaredField(pendingFieldName);
                pendingField.setAccessible(true);
            } catch (NoSuchFieldException e) {
                throw new ModelException("Champ '%s' introuvable sur '%s'".formatted(pendingFieldName, modelClass.getSimpleName()));
            }

            // Lire la valeur du pending
            Object pendingValue;
            try {
                pendingValue = pendingField.get(model);
            } catch (IllegalAccessException e) {
                throw new ModelException("Impossible de lire '%s' sur '%s'".formatted(pendingFieldName, modelClass.getSimpleName()));
            }

            // Trouver et appeler setXxxFromPk(pendingValue)
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

    public static void init(Class<? extends DataManager<?>> clazz) {
        try {
            DataManager<?> manager = get(clazz);
            manager.resolveReferences();
        } catch (LoadDataManagerDataException e) {
            System.out.printf(Functions.styleAsErrorMessage("Le pré-chargement du manager '%s' n'a pas pu être effectué.%n"), clazz.getSimpleName());
        } catch (ModelException e) {
            System.out.printf(Functions.styleAsErrorMessage("La résolution des références du manager '%s' a échoué: %s%n"), clazz.getSimpleName(), e.getMessage());
        }
    }

    public static int getCountOf(Class<? extends DataManager<?>> clazz) {
        init(clazz);
        try {
            return get(clazz).count();
        } catch (LoadDataManagerDataException e) {
            return 0;
        }
    }

}
