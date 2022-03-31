package org.observertc.observer.repositories;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

class RepositoryExpiredEventTest {

    @Test
    void structuralTest() {
        var touched = Instant.now().toEpochMilli();
        var value = UUID.randomUUID();
        var event = RepositoryExpiredEvent.<UUID>make(value, touched);

        Assertions.assertEquals(touched, event.estimatedLastTouch());
        Assertions.assertEquals(value, event.getValue());
    }
}