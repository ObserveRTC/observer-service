package org.observertc.observer.common;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

public class CollectionChunker<T> {
    public static<U> Builder<U> builder() {
        return new Builder<>();
    }

    private int limit = 0;
    private Function<T, Integer> getSize = obj -> 1;
    private boolean canOverflow = false;

//    public Stream<Collection<T>> stream(Collection<T> source) {
//        if (this.limit < 1) {
//            return Stream.of(source);
//        }
//    }

    public Iterator<Collection<T>> iterate(Collection<T> source) {
        if (this.limit < 1) {
            AtomicReference<Collection<T>> item = new AtomicReference<>(source);
            return new Iterator<Collection<T>>() {
                @Override
                public boolean hasNext() {
                    return item.get() != null;
                }

                @Override
                public Collection<T> next() {
                    return item.getAndSet(null);
                }
            };
        }
        var it = source.iterator();
        var remedy = new AtomicReference<T>(null);
        return new Iterator<Collection<T>>() {
            @Override
            public boolean hasNext() {
                return it.hasNext();
            }

            @Override
            public Collection<T> next() {
                var actual = 0;
                var result = new LinkedList<T>();
                if (remedy.get() != null) {
                    var item = remedy.getAndSet(null);
                    actual += getSize.apply(item);
                    result.add(item);
                }
                for (; it.hasNext(); ) {
                    var item = it.next();
                    var size = getSize.apply(item);
                    if (actual + size < limit) {
                        result.add(item);
                        actual += size;
                        continue;
                    }
                    if (canOverflow) {
                        result.add(item);
                    } else {
                        remedy.set(item);
                    }
                    break;
                }
                return result;
            }
        };
    }

    public static class Builder<U> {
        private CollectionChunker<U> result = new CollectionChunker<>();

        public Builder<U> setLimit(int value) {
            this.result.limit = value;
            return this;
        }

        public Builder<U> setSizeFn(Function<U, Integer> func) {
            this.result.getSize = func;
            return this;
        }

        public Builder<U> setCanOverflowFlag(boolean value) {
            this.result.canOverflow = value;
            return this;
        }

        public CollectionChunker<U> build() {

            return this.result;
        }
    }
}
