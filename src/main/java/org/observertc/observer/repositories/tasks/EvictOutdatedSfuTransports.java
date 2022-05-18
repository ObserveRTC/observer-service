package org.observertc.observer.repositories.tasks;

import io.micronaut.context.annotation.Prototype;
import jakarta.inject.Inject;
import org.observertc.observer.common.ChainedTask;
import org.observertc.observer.common.JsonUtils;
import org.observertc.observer.metrics.RepositoryMetrics;
import org.observertc.observer.repositories.HazelcastMaps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import java.time.Instant;
import java.util.HashSet;
import java.util.UUID;

@Prototype
public class EvictOutdatedSfuTransports extends ChainedTask<Void> {

    private static final Logger logger = LoggerFactory.getLogger(EvictOutdatedSfuTransports.class);

    @Inject
    HazelcastMaps hazelcastMaps;

    @Inject
    RepositoryMetrics exposedMetrics;


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
                .addActionStage("Evict not refreshed sfu transports", () -> {
                    var sfuTransportIds = this.hazelcastMaps.getRefreshedSfuTransports().localKeySet();
                    var refreshedSfuTransports = this.hazelcastMaps.getRefreshedSfuTransports().getAll(sfuTransportIds);
                    var evictedTransportIds = new HashSet<UUID>();
                    refreshedSfuTransports.forEach((sfuTransportId, lastRefreshed) -> {
                        if (NOW - lastRefreshed < this.expiredThresholdInMs) {
                            return;
                        }
                        this.hazelcastMaps.getSFUTransports().evict(sfuTransportId);
                        evictedTransportIds.add(sfuTransportId);
                    });
                    if (0 < evictedTransportIds.size()) {
                        logger.info("Evicted {} sfu transport ids from {} local stored sfu transport. evictedTransportIds: {}",
                                evictedTransportIds.size(), sfuTransportIds.size(), JsonUtils.objectToString(evictedTransportIds));
                    }
                })
                .build();
    }

    public EvictOutdatedSfuTransports withExpirationThresholdInMs(long value) {
        if (value < 1) {
            logger.warn("Expiration threshold cannot be less than 1");
            return this;
        }
        this.expiredThresholdInMs = value;
        return this;
    }
}
