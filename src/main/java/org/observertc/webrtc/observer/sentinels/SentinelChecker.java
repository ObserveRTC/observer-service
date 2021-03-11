package org.observertc.webrtc.observer.sentinels;

import io.micronaut.scheduling.annotation.Scheduled;
import org.observertc.webrtc.observer.ObserverConfig;
import org.observertc.webrtc.observer.entities.CallEntity;
import org.observertc.webrtc.observer.entities.SentinelEntity;
import org.observertc.webrtc.observer.monitors.SentinelMonitor;
import org.observertc.webrtc.observer.repositories.CallsRepository;
import org.observertc.webrtc.observer.repositories.SentinelsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Singleton
public class SentinelChecker {
    private static final int MAX_CONSECUTIVE_ERROR = 3;
    private static final Logger logger = LoggerFactory.getLogger(SentinelChecker.class);

    private volatile boolean run = true;
    private volatile int consecutiveFailure = 0;

    @Inject
    CallsRepository callsRepository;

    @Inject
    SentinelsRepository sentinelsRepository;

    @Inject
    SentinelMonitor sentinelMonitor;

//    @Inject
//    MeterRegistry meterRegistry;

    @Inject
    ObserverConfig observerConfig;

    private Instant lastRun = null;

    @PostConstruct
    void setup() {

    }

    @Scheduled(initialDelay = "1m", fixedDelay = "1m")
    void run() throws Throwable {
        if (!this.run) {
            return;
        }

        Instant now = Instant.now();
        if (Objects.nonNull(this.lastRun) && Duration.between(this.lastRun, now).getSeconds() < (observerConfig.sentinelsCheckingPeriodInMin * 60 - 1)) {
            return;
        }
        this.lastRun = now;

        try {
            this.doRun();
            this.consecutiveFailure = 0;
        } catch (Throwable t) {
            if (MAX_CONSECUTIVE_ERROR <= ++this.consecutiveFailure) {
                logger.error("Error happened {} times consecutively meanwhile tried to perform a check for sentinels. The checking task will be shut down", this.consecutiveFailure);
                this.run = false;
            }
            logger.warn("There is an error occurred while performing sentinel checks", t);
        }
    }

    private void doRun() throws Throwable {
        Map<UUID, CallEntity> callEntities = this.callsRepository.fetchLocallyStoredCalls();
        Map<String, SentinelEntity> sentinelEntities = this.sentinelsRepository.fetchAllEntities();

        for (SentinelEntity sentinelEntity : sentinelEntities.values()) {
            int numOfSSRCs = 0;
            int numOfPCs = 0;
            int numOfCalls = 0;
            int browserIds = 0;
            Set<String> mediaUnits = new HashSet<>();
            for (CallEntity callEntity : callEntities.values()) {
                boolean watched = sentinelEntity.test(callEntity);
                logger.info("Sentinel {} is checked call {}, and it is {} watched", sentinelEntity.getName(), callEntity.call.callUUID, watched ? "" : "not");
                if (!watched) {
                    continue;
                }
                // mediaunits
                ++numOfCalls;
                browserIds += callEntity.peerConnections.values().stream().map(pc -> pc.peerConnection.browserId).filter(Objects::nonNull).collect(Collectors.toSet()).size();
                numOfSSRCs += callEntity.SSRCs.size();
                numOfPCs += callEntity.peerConnections.size();
                callEntity.peerConnections.values().stream().map(pc -> pc.peerConnection.mediaUnitId).filter(Objects::nonNull).forEach(mediaUnits::add);
            }
            logger.info("Sentinel {} is checked all call and counted SSRCs: {} PCs: {} calls: {}", sentinelEntity.getName(), numOfSSRCs, numOfPCs, numOfCalls);
            final String sentinelName = sentinelEntity.getName();
            if (sentinelEntity.streamMetrics()) {
                this.sentinelMonitor.getSentinelMetrics(sentinelName)
                        .setNumberOfCalls(numOfCalls)
                        .setNumberOfBrowserIds(browserIds)
                        .setNumberOfSSRCs(numOfSSRCs)
                        .setNumberOfPeerConnections(numOfPCs);
            }
            if (sentinelEntity.mediaUnits()) {
                this.sentinelMonitor.getSentinelMetrics(sentinelName)
                        .incrementMediaUnits(mediaUnits);
            }
        }
    }
}
