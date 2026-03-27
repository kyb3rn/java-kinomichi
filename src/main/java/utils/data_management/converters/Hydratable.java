package utils.data_management.converters;

public interface Hydratable<T extends CustomSerializable> {

    void hydrate(T dataObject) throws Exception;

    T dehydrate() throws Exception;

}
