package org.observertc.observer.common;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Instant;

@MicronautTest
class SleeperTest {

    @Test
    void shouldSleepEnough() {
        var started = Instant.now().toEpochMilli();

        new Sleeper(() -> 500).run();

        var ended = Instant.now().toEpochMilli();
        Assertions.assertTrue(500 <= ended - started);
    }

}