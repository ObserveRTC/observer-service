package org.observertc.webrtc.observer.common;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@MicronautTest
class LRULinkedHashMapTest {

    @Test
    void shouldKeepUsedItems_1() {
        var cache = new LRULinkedHashMap<Integer, String>(2, removed -> {
            Assertions.assertEquals(1, removed.getKey());
        });
        cache.put(1, "one");
        cache.put(2, "two");
        cache.get(2);
        cache.put(3, "three");

        Assertions.assertEquals(2, cache.size());
    }

}