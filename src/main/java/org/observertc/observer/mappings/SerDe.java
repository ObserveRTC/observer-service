package org.observertc.observer.mappings;

public interface SerDe<T>  {
    byte[] serialize(T object);
    T deserialize(byte[] data);
}
