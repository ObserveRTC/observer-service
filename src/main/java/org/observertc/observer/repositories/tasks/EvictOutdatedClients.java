package org.observertc.observer.repositories.tasks;

import io.micronaut.context.annotation.Prototype;
import jakarta.inject.Inject;
import org.observertc.observer.common.ChainedTask;
import org.observertc.observer.common.JsonUtils;
import org.observertc.observer.metrics.RepositoryMetrics;
import org.observertc.observer.repositories.HamokStorages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import java.time.Instant;
import java.util.HashSet;
import java.util.UUID;

@Prototype
public class EvictOutdatedClients extends ChainedTask<Void> {

    private static final Logger logger = LoggerFactory.getLogger(EvictOutdatedClients.class);

    @Inject
    HamokStorages hazelcastMaps;

    @Inject
    RepositoryMetrics exposedMetrics;

    /**
     * Hazelcast by default uses system.currentTimeMillis unless configuration overrides it.
     */
//    private final Long NOW = System.currentTimeMillis();

    private final Long NOW = Instant.now().toEpochMilli();
    private Long expiredThresholdInMs = 0L;

    @PostConstruct
    void setup() {
        this.withStatsConsumer(this.exposedMetrics::processTaskStats);
        new Builder<Void>(this)
                .addActionStage("First stage", () -> {

                })
                .addBreakCondition(voidInput -> {
                    if (this.expiredThresholdInMs < 1) {
                        return true;
                    }
                    return false;
                })
                .addActionStage("Evict not refreshed clients", () -> {
                    var clientIds = this.hazelcastMaps.getRefreshedClients().localKeySet();
                    var refreshedClients = this.hazelcastMaps.getRefreshedClients().getAll(clientIds);
                    var evictedClientIds = new HashSet<UUID>();
                    refreshedClients.forEach((clientId, lastRefreshed) -> {
                        if (NOW - lastRefreshed < this.expiredThresholdInMs) {
                            return;
                        }
                        this.hazelcastMaps.getClients().evict(clientId);
                        evictedClientIds.add(clientId);
                    });
                    if (0 < evictedClientIds.size()) {
                        logger.info("Evicted {} client ids from {} local stored clients. evictedClientIds: {}",
                                evictedClientIds.size(), clientIds.size(), JsonUtils.objectToString(evictedClientIds));
                    }
                })
                .build();
    }

    public EvictOutdatedClients withExpirationThresholdInMs(long value) {
        if (value < 1) {
            logger.warn("Expiration threshold cannot be less than 1");
            return this;
        }
        this.expiredThresholdInMs = value;
        return this;
    }
}
