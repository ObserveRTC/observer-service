package org.observertc.observer.mappings;

public interface Decoder<U, R> {

    static<T, U> Decoder<T, U> from(Mapper<T, U> mapper) {
        return mapper::map;
    }

    R decode(U data) throws Throwable;

}