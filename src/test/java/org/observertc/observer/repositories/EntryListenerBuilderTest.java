package org.observertc.observer.repositories;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.observertc.observer.dto.GeneralEntryDTO;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@MicronautTest
class EntryListenerBuilderTest {

    @Inject
    HamokStorages hazelcastMaps;

    @Test
    void shouldAddUpdateRemove() throws InterruptedException, ExecutionException, TimeoutException {
        var added = new CompletableFuture<Void>();
        var updated = new CompletableFuture<Void>();
        var removed = new CompletableFuture<Void>();
        var entryKey = UUID.randomUUID();
        var expected = GeneralEntryDTO.builder()
                .withKey("key")
                .withTimestamp(Instant.now().toEpochMilli())
                .withValue("value")
                .build();
        var entryListener = EntryListenerBuilder.<String, GeneralEntryDTO>create("Test")
                .onEntryAdded(entryEvent -> {
                    var actual = entryEvent.getValue();
                    boolean equals = expected.equals(actual);
                    Assertions.assertTrue(equals);
                    added.complete(null);
                })
                .onEntryUpdated(entryEvent -> {
                    var actual = entryEvent.getValue();
                    boolean equals = expected.equals(actual);
                    Assertions.assertTrue(equals);
                    updated.complete(null);
                })
                .onEntryRemoved(entryEvent -> {
                    var actual = entryEvent.getOldValue();
                    boolean equals = expected.equals(actual);
                    Assertions.assertTrue(equals);
                    removed.complete(null);
                })
                .build();

        hazelcastMaps.getGeneralEntries().addLocalEntryListener(entryListener);

        hazelcastMaps.getGeneralEntries().put(entryKey, expected);
        added.get(10, TimeUnit.SECONDS);

        expected.timestamp = Instant.now().toEpochMilli();
        hazelcastMaps.getGeneralEntries().put(entryKey, expected);
        updated.get(10, TimeUnit.SECONDS);

        hazelcastMaps.getGeneralEntries().remove(entryKey);
        removed.get(10, TimeUnit.SECONDS);
    }

    @Test
    void shouldExpire() throws InterruptedException, ExecutionException, TimeoutException {
        var expired = new CompletableFuture<Void>();
        var entryKey = UUID.randomUUID();
        var expected = GeneralEntryDTO.builder()
                .withKey("key")
                .withTimestamp(Instant.now().toEpochMilli())
                .withValue("value")
                .build();
        var entryListener = EntryListenerBuilder.<String, GeneralEntryDTO>create("Test")
                .onEntryExpired(entryEvent -> {
                    var actual = entryEvent.getOldValue();
                    boolean equals = expected.equals(actual);
                    Assertions.assertTrue(equals);
                    expired.complete(null);
                })
                .build();

        hazelcastMaps.getGeneralEntries().addLocalEntryListener(entryListener);

        hazelcastMaps.getGeneralEntries().put(entryKey, expected, 100, TimeUnit.MILLISECONDS);
        expired.get(10, TimeUnit.SECONDS);
    }
}