package org.observertc.observer.common;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Duration;

class TimeLimitedMapTest {

    @Test
    void shouldRemoveAfterTime() throws InterruptedException {
        var map = new TimeLimitedMap<Integer, String>(Duration.ofMillis(100));
        map.put(1, "A");
        map.put(2, "B");

        Thread.sleep(200);
        map.get(2);

        Assertions.assertNull(map.get(1));
    }



}