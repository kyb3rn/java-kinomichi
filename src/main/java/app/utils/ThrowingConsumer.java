package app.utils;

@FunctionalInterface
public interface ThrowingConsumer<T> {

    void accept(T input) throws Exception;

}
