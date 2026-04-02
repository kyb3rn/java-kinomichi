package app.utils;

@FunctionalInterface
public interface ThrowingVerificator<T> {

    boolean accept(T input) throws Exception;

}
