package org.observertc.webrtc.observer.sentinels;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import org.observertc.webrtc.observer.ObserverConfig;
import org.observertc.webrtc.observer.entities.CallEntity;
import org.observertc.webrtc.observer.entities.SentinelEntity;
import org.observertc.webrtc.observer.repositories.CallsRepository;
import org.observertc.webrtc.observer.repositories.SentinelsRepository;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Singleton
public class SentinelChecker {

    private volatile boolean run = false;

    @Inject
    CallsRepository callsRepository;

    @Inject
    SentinelsRepository sentinelsRepository;

    @Inject
    MeterRegistry meterRegistry;

    @Inject
    ObserverConfig observerConfig;

    private Instant lastRun = null;

    @PostConstruct
    void setup() {

    }

    // under development!
//    @Scheduled(initialDelay = "1m", fixedDelay = "1m")
    void run() throws Throwable {

        Instant now = Instant.now();
        if (Objects.nonNull(this.lastRun) && Duration.between(this.lastRun, now).getSeconds() < observerConfig.sentinelInvasionPeriodInMin * 60) {
            return;
        }
        this.lastRun = now;

        Map<UUID, CallEntity> callEntities = this.callsRepository.fetchLocallyStoredCalls();
        Map<String, SentinelEntity> sentinelEntities = this.sentinelsRepository.fetchAll();
        for (SentinelEntity sentinelEntity : sentinelEntities.values()) {
            int numOfSSRCs = 0;
            int numOfPCs = 0;
            int numOfCalls = 0;
            for (CallEntity callEntity : callEntities.values()) {
                boolean watched = sentinelEntity.test(callEntity);
                if (!watched) {
                    continue;
                }
                ++numOfCalls;
                numOfSSRCs += callEntity.SSRCs.size();
                numOfPCs += callEntity.peerConnections.size();
            }
            if (sentinelEntity.streamMetrics()) {
                meterRegistry.gauge("observertc_monitors_ssrcs", List.of(Tag.of("monitor", sentinelEntity.getName())), numOfSSRCs);
                meterRegistry.gauge("observertc_monitors_pcs", List.of(Tag.of("monitor", sentinelEntity.getName())), numOfPCs);
                meterRegistry.gauge("observertc_monitors_calls", List.of(Tag.of("monitor", sentinelEntity.getName())), numOfCalls);
            }
        }
    }
}
