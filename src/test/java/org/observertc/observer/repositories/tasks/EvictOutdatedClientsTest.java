package org.observertc.observer.repositories.tasks;

import io.micronaut.context.BeanProvider;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.observertc.observer.common.Sleeper;
import org.observertc.observer.dto.ClientDTO;
import org.observertc.observer.repositories.EntryListenerBuilder;
import org.observertc.observer.repositories.HazelcastMaps;
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

@MicronautTest(environments = "test")
class EvictOutdatedClientsTest {
    org.observertc.observer.utils.DTOMapGenerator DTOMapGenerator = new DTOMapGenerator().generateP2pCase();

    @Inject
    HazelcastMaps hazelcastMaps;

    @Inject
    BeanProvider<EvictOutdatedClients> cleaningCallsTaskBeanProvider;

    @BeforeEach
    void setup() {
        DTOMapGenerator.saveTo(hazelcastMaps);
    }

    @AfterEach
    void teardown() {
        DTOMapGenerator.deleteFrom(hazelcastMaps);
    }

    @Test
    void shouldEvict_1() throws InterruptedException, ExecutionException, TimeoutException {
        var refreshedClients = DTOMapGenerator.getClientDTOs().keySet().stream().collect(Collectors.toMap(
                Function.identity(),
                clientId -> Instant.now().toEpochMilli()
        ));
        var completed = new CompletableFuture<Void>();
        var evictedClientIds = new HashSet<UUID>();
        this.hazelcastMaps.getClients().addLocalEntryListener(EntryListenerBuilder.<UUID, ClientDTO>create()
                .onEntryEvicted(event -> {
                    var clientDTO = event.getOldValue();
                    evictedClientIds.add(clientDTO.clientId);
                    if (evictedClientIds.size() == refreshedClients.size()) {
                        completed.complete(null);
                    }
                }).build());
        var thresholdInMs = 1000;
        this.hazelcastMaps.getRefreshedClients().putAll(refreshedClients);
        new Sleeper(() -> thresholdInMs * 2).run();
        this.cleaningCallsTaskBeanProvider.get().withExpirationThresholdInMs(thresholdInMs).execute();
        completed.get(thresholdInMs * 10, TimeUnit.MILLISECONDS);
    }
}