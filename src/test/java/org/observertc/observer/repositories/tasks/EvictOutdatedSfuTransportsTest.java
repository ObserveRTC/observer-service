package org.observertc.observer.repositories.tasks;

import io.micronaut.context.BeanProvider;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.observertc.observer.common.Sleeper;
import org.observertc.observer.dto.SfuTransportDTO;
import org.observertc.observer.repositories.EntryListenerBuilder;
import org.observertc.observer.repositories.HamokStorages;
import org.observertc.observer.utils.DTOMapGenerator;

import java.time.Instant;
import java.util.HashSet;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;
import java.util.stream.Collectors;

@MicronautTest
class EvictOutdatedSfuTransportsTest {
    org.observertc.observer.utils.DTOMapGenerator DTOMapGenerator = new DTOMapGenerator().generateSingleSfuCase();

    @Inject
    HamokStorages hamokStorages;

    @Inject
    BeanProvider<EvictOutdatedSfuTransports> evictOutdatedSfuTransportsTaskProvider;

    @BeforeEach
    void setup() {
        DTOMapGenerator.saveTo(hamokStorages);
    }

    @AfterEach
    void teardown() {
        DTOMapGenerator.deleteFrom(hamokStorages);
    }

    @Test
    void shouldEvict_1() throws InterruptedException, ExecutionException, TimeoutException {
        var refreshedSfuTransports = DTOMapGenerator.getSfuTransports().keySet().stream().collect(Collectors.toMap(
                Function.identity(),
                sfuTransportId -> Instant.now().toEpochMilli()
        ));
        var completed = new CompletableFuture<Void>();
        var evictedSfuTransportIds = new HashSet<UUID>();
        this.hamokStorages.getSFUTransports().addLocalEntryListener(EntryListenerBuilder.<UUID, SfuTransportDTO>create()
                .onEntryEvicted(event -> {
                    var sfuTransportDTO = event.getOldValue();
                    evictedSfuTransportIds.add(sfuTransportDTO.transportId);
                    if (evictedSfuTransportIds.size() == refreshedSfuTransports.size()) {
                        completed.complete(null);
                    }
                }).build());
        var thresholdInMs = 1000;
        this.hamokStorages.getRefreshedSfuTransports().putAll(refreshedSfuTransports);
        new Sleeper(() -> thresholdInMs * 2).run();
        this.evictOutdatedSfuTransportsTaskProvider.get().withExpirationThresholdInMs(thresholdInMs).execute();
        completed.get(thresholdInMs * 10, TimeUnit.MILLISECONDS);
    }
}