package org.observertc.observer.common;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Map;

class FlatIteratorTest {
    @Test
    void shouldIterateAll() {
        var subject = Map.of("one", List.of(1,2,3), "two", List.of(4, 5));
        var iterated = new HashSet<Integer>();
        for (var it = new FlatIterator<Integer>(subject.values().iterator(), list -> list.iterator()); it.hasNext(); ) {
            iterated.add(it.next());
        }
        Assertions.assertTrue(iterated.contains(1));
        Assertions.assertTrue(iterated.contains(2));
        Assertions.assertTrue(iterated.contains(3));
        Assertions.assertTrue(iterated.contains(4));
        Assertions.assertTrue(iterated.contains(5));
    }
}