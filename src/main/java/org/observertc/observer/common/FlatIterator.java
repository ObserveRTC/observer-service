package org.observertc.observer.common;

import java.util.Iterator;
import java.util.function.Function;

public class FlatIterator<T> implements Iterator<T> {

    private final Iterator<Iterator<T>> parentIterator;
    private Iterator<T> actualIterator;

    public<K> FlatIterator(Iterator<K> base, Function<K, Iterator<T>> getIterator) {
        this(new Iterator<Iterator<T>>() {
            @Override
            public boolean hasNext() {
                return base.hasNext();
            }

            @Override
            public Iterator<T> next() {
                return getIterator.apply(base.next());
            }
        });
    }

    public FlatIterator(Iterator<Iterator<T>> base) {
        this.parentIterator = base;
    }

    @Override
    public boolean hasNext() {
        if (actualIterator != null && !actualIterator.hasNext()) {
            this.actualIterator = null;
        }
        if (actualIterator == null && parentIterator.hasNext()) {
            this.actualIterator = parentIterator.next();
        }
        return this.actualIterator != null && this.actualIterator.hasNext();
    }

    @Override
    public T next() {
        return this.actualIterator.next();
    }
}
