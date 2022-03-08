package org.observertc.observer.common;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Instant;

@MicronautTest
class SleeperTest {

    @Test
    void shouldSleepEnough() {
        var started = Instant.now();

        new Sleeper(() -> 2000).run();

        Assertions.assertTrue(2000 <= Instant.now().minusMillis(started.toEpochMilli()).toEpochMilli());
    }

}