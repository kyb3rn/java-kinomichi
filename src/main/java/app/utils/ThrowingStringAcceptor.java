package app.utils;

@FunctionalInterface
public interface ThrowingStringAcceptor {

    void accept(String input) throws Exception;

}
