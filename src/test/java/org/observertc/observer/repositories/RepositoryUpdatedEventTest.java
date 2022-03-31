package org.observertc.observer.repositories;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.UUID;

class RepositoryUpdatedEventTest {
    @Test
    void structuralTest() {
        var oldValue = UUID.randomUUID();
        var newValue = UUID.randomUUID();
        var event = RepositoryUpdatedEvent.<UUID>make(oldValue, newValue);

        Assertions.assertEquals(oldValue, event.getOldValue());
        Assertions.assertEquals(newValue, event.getNewValue());
    }
}