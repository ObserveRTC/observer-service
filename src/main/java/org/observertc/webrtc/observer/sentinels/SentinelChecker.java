package org.observertc.webrtc.observer.sentinels;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micronaut.scheduling.annotation.Scheduled;
import org.observertc.webrtc.observer.common.Utils;
import org.observertc.webrtc.observer.ObserverConfig;
import org.observertc.webrtc.observer.configs.ObserverConfigDispatcher;
import org.observertc.webrtc.observer.configs.stores.SentinelsStore;
import org.observertc.webrtc.observer.entities.CallEntity;
import org.observertc.webrtc.observer.entities.PeerConnectionEntity;
import org.observertc.webrtc.observer.evaluatorsPurgatory.pcSampleToReportsV2.monitors.RtpMonitorAbstract;
import org.observertc.webrtc.observer.repositories.CallsRepository;
import org.observertc.webrtc.observer.repositories.HazelcastMaps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

@Singleton
public class SentinelChecker {
    private static final int MAX_CONSECUTIVE_ERROR = 3;
    private static final String SENTINEL_CHECK_EXECUTION_TIME_METRIC_NAME = "observertc_sentinel_check_timer";
    private static final Logger logger = LoggerFactory.getLogger(SentinelChecker.class);

    private volatile boolean run = true;
    private volatile int consecutiveFailure = 0;
    private Timer runTimer;

    @Inject
    CallsRepository callsRepository;

    @Inject
    SentinelsStore sentinelsStore;

    @Inject
    SentinelMonitor sentinelMonitor;

    @Inject
    MeterRegistry meterRegistry;

    @Inject
    ObserverConfigDispatcher configDispatcher;

    @Inject
    Provider<FetchSentinelEntitiesTask> sentinelEntitiesTaskProvider;

    @Inject
    HazelcastMaps hazelcastMaps;

    private Instant lastRun = null;

    @PostConstruct
    void setup() {
        this.runTimer = this.meterRegistry.timer(SENTINEL_CHECK_EXECUTION_TIME_METRIC_NAME);
    }

    @Scheduled(initialDelay = "1m", fixedDelay = "1m")
    void run() throws Throwable {
        if (!this.run) {
            return;
        }
        ObserverConfig observerConfig = configDispatcher.getConfig();

        Instant called = Instant.now();
        if (Objects.nonNull(this.lastRun) && Duration.between(this.lastRun, called).getSeconds() < (observerConfig.sentinelsCheckingPeriodInMin * 60 - 1)) {
            return;
        }
        this.lastRun = called;

        try {
            this.doRun(observerConfig);
            this.consecutiveFailure = 0;
        } catch (Throwable t) {
            if (MAX_CONSECUTIVE_ERROR <= ++this.consecutiveFailure) {
                logger.error("Error happened {} times consecutively meanwhile tried to perform a check for sentinels. The checking task will be shut down", this.consecutiveFailure);
                this.run = false;
            }
            logger.warn("There is an error occurred while performing sentinel checks", t);
        } finally {
            Duration executionTime = Duration.between(called, Instant.now());
            this.runTimer.record(executionTime);
        }
    }

    private void doRun(ObserverConfig observerConfig) throws Throwable {
        boolean inboundIsMonitored = false;
        if (Objects.nonNull(observerConfig) && Objects.nonNull(observerConfig.inboundRtpMonitor) && Objects.nonNull(observerConfig.inboundRtpMonitor.enabled)) {
            inboundIsMonitored = observerConfig.inboundRtpMonitor.enabled;
        }

        boolean outboundIsMonitored = false;
        if (Objects.nonNull(observerConfig) && Objects.nonNull(observerConfig.outboundRtpMonitor) && Objects.nonNull(observerConfig.outboundRtpMonitor.enabled)) {
            outboundIsMonitored = observerConfig.outboundRtpMonitor.enabled;
        }

        boolean remoteInboundIsMonitored = false;
        if (Objects.nonNull(observerConfig) && Objects.nonNull(observerConfig.remoteInboundRtpMonitor) && Objects.nonNull(observerConfig.remoteInboundRtpMonitor.enabled)) {
            remoteInboundIsMonitored = observerConfig.remoteInboundRtpMonitor.enabled;
        }
        Set<String> touchedRTPKeys = new HashSet<>();
        Map<UUID, CallEntity> callEntities = this.callsRepository.fetchLocallyStoredCalls();
        Set<String> sentinelNames = this.sentinelsStore.findAll().keySet();
        var task = sentinelEntitiesTaskProvider.get();
        if (!task.whereSentinelNames(sentinelNames).execute().succeeded()) {
            return;
        }
        Map<String, SentinelEntity> sentinelEntities = task.getResult();
        Set<String> mediaUnits = new HashSet<>();
        Set<String> browserIds = new HashSet<>();
        Set<String> userNames = new HashSet<>();
        Set<Long> SSRCs = new HashSet<>();
        Set<UUID> pcUUIDs = new HashSet<>();
        Set<UUID> callUUIDs = new HashSet<>();
        List<Double> RttAvgs = new LinkedList<>();
        for (SentinelEntity sentinelEntity : sentinelEntities.values()) {
            long bytesReceived = 0;
            long packetsReceived = 0;
            long packetsLost = 0;
            long packetsSent = 0;
            long bytesSent = 0;
            browserIds.clear();
            userNames.clear();
            mediaUnits.clear();
            SSRCs.clear();
            pcUUIDs.clear();
            callUUIDs.clear();
            RttAvgs.clear();
            for (CallEntity callEntity : callEntities.values()) {
                boolean callIsWatched = sentinelEntity.testCall(callEntity);
                for (PeerConnectionEntity pcEntity : callEntity.peerConnections.values()) {
                    boolean pcIsWatched = sentinelEntity.testPeerConnection(pcEntity);
                    if (!callIsWatched && !pcIsWatched) {
                        continue;
                    }
                    Utils.acceptIfValueNonNull(pcEntity.peerConnection.browserId, browserIds::add);
                    Utils.acceptIfValueNonNull(pcEntity.peerConnection.providedUserName, userNames::add);
                    Utils.acceptIfValueNonNull(pcEntity.peerConnection.mediaUnitId, mediaUnits::add);
                    Utils.acceptIfValueNonNull(callEntity.call.callUUID, callUUIDs::add);
                    Utils.acceptIfValueNonNull(pcEntity.pcUUID, pcUUIDs::add);
                    Utils.acceptIfValueNonNull(pcEntity.SSRCs, SSRCs::addAll);


                    if (!inboundIsMonitored && !outboundIsMonitored && !remoteInboundIsMonitored) {
                        // if no metric is enabled then we probably should not go through the SSRCs
                        continue;
                    }

                    for (Long SSRC : pcEntity.SSRCs) {
                        String key = RtpMonitorAbstract.getKey(pcEntity.pcUUID, SSRC);
                        touchedRTPKeys.add(key);
                        if (inboundIsMonitored) {
                            InboundRtpTrafficDTO inboundRtpTrafficDTO = hazelcastMaps.getInboundRtpDTOs().get(key);
                            if (Objects.nonNull(inboundRtpTrafficDTO)) {
                                if (0 < inboundRtpTrafficDTO.lastPacketsReceived) {
                                    bytesReceived += inboundRtpTrafficDTO.lastBytesReceived - inboundRtpTrafficDTO.firstBytesReceived;
                                }
                                if (0 < inboundRtpTrafficDTO.lastPacketsReceived) {
                                    packetsReceived += inboundRtpTrafficDTO.lastPacketsReceived - inboundRtpTrafficDTO.firstPacketsReceived;
                                }
                                if (0 < inboundRtpTrafficDTO.lastPacketsLost) {
                                    packetsLost += inboundRtpTrafficDTO.lastPacketsLost - inboundRtpTrafficDTO.firstPacketsLost;
                                }
                            }
                        }

                        if (outboundIsMonitored) {
                            OutboundRtpTrafficDTO outboundRtpTrafficDTO = hazelcastMaps.getOutboundRtpDTOs().get(key);
                            if (Objects.nonNull(outboundRtpTrafficDTO)) {
                                if (0 < outboundRtpTrafficDTO.lastBytesSent) {
                                    bytesSent += outboundRtpTrafficDTO.lastBytesSent - outboundRtpTrafficDTO.firstBytesSent;
                                }
                                if (0 < outboundRtpTrafficDTO.lastPacketsSent) {
                                    packetsSent += outboundRtpTrafficDTO.lastPacketsSent - outboundRtpTrafficDTO.firstPacketsSent;
                                }
                            }
                        }

                        if (remoteInboundIsMonitored) {
                            RemoteInboundRtpTrafficDTO remoteInboundRtpTrafficDTO = hazelcastMaps.getRemoteInboundTrafficDTOs().get(key);
                            if (Objects.nonNull(remoteInboundRtpTrafficDTO)) {
                                if (0. < remoteInboundRtpTrafficDTO.rttAvg) {
                                    RttAvgs.add(remoteInboundRtpTrafficDTO.rttAvg);
                                }
                            }

                        }

                    }

                }
            }

//            logger.info("Sentinel {} is checked all call and counted SSRCs: {} PCs: {} calls: {}", sentinelEntity.getName(), numOfSSRCs, numOfPCs, numOfCalls);
            final String sentinelName = sentinelEntity.getName();
            if (sentinelEntity.isExposed()) {
                final int numOfBrowserIds = browserIds.size();
                final int numOfUserNames = userNames.size();
                int numOfSSRCs = SSRCs.size();
                int numOfPCs = pcUUIDs.size();
                int numOfCalls = callUUIDs.size();
                var sentinelMetrics = this.sentinelMonitor.getSentinelMetrics(sentinelName)
                        .setNumberOfCalls(numOfCalls)
                        .setNumberOfBrowserIds(numOfBrowserIds)
                        .setNumberOfSSRCs(numOfSSRCs)
                        .setNumberOfUserNames(numOfUserNames)
                        .setNumberOfPeerConnections(numOfPCs)
                        .incrementMediaUnits(mediaUnits);

                if (inboundIsMonitored) {
                    sentinelMetrics
                            .setBytesReceived(bytesReceived)
                            .setPacketsReceived(packetsReceived)
                            .setPacketsLost(packetsLost)
                    ;
                }

                if (outboundIsMonitored) {
                    sentinelMetrics
                            .setBytesSent(bytesSent)
                            .setPacketsSent(packetsSent)
                    ;
                }

                if (remoteInboundIsMonitored) {
                    sentinelMetrics
                            .setRoundTripTimeDistributions(RttAvgs);
                }
            }

            if (sentinelEntity.isReported()) {
                logger.warn("Sentinel Metric Reporting is not supported in this version. Only metric exposing!");
            }
            if (remoteInboundIsMonitored) { // making the RTT clean
                Map<String, RemoteInboundRtpTrafficDTO> map = this.hazelcastMaps.getRemoteInboundTrafficDTOs().getAll(touchedRTPKeys);
                map.values().stream().forEach(v -> v.rttAvg = -1.);
                this.hazelcastMaps.getRemoteInboundTrafficDTOs().putAll(map);
            }
        }
    }
}
