package app.data_management.managers;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

public class DataManagers {

    /** Properties **/

    private static final Map<Class<?>, DataManager<?>> instances = new HashMap<>();

    /** Special methods **/

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
    public static void init(Class<? extends DataManager<?>>... classes) throws LoadDataManagerDataException {
        for (var clazz : classes) {
            get(clazz);
        }
    }

    public static int getCountOf(Class<? extends DataManager<?>> clazz) {
        try {
            return get(clazz).count();
        } catch (LoadDataManagerDataException e) {
            return 0;
        }
    }

}
