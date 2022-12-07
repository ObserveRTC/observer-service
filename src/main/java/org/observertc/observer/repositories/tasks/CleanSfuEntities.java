package org.observertc.observer.repositories.tasks;

import jakarta.inject.Inject;
import org.observertc.observer.ServerTimestamps;
import org.observertc.observer.common.ChainedTask;
import org.observertc.observer.common.Task;
import org.observertc.observer.common.Utils;
import org.observertc.observer.configs.ObserverConfig;
import org.observertc.observer.repositories.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.function.Function;
import java.util.stream.Collectors;

public class CleanSfuEntities extends ChainedTask<Void> {

    private static final Logger logger = LoggerFactory.getLogger(CleanSfuEntities.class);

    @Inject
    ObserverConfig observerConfig;

    @Inject
    ServerTimestamps serverTimestamps;

    @Inject
    SfusRepository sfusRepository;

    @Inject
    SfuTransportsRepository sfuTransportsRepository;

    @Inject
    SfuInboundRtpPadsRepository sfuInboundRtpPadsRepository;

    @Inject
    SfuOutboundRtpPadsRepository sfuOutboundRtpPadsRepository;

    @Inject
    SfuSctpChannelsRepository sfuSctpChannelsRepository;

    private Long lastSfusClean = null;
    private Long lastSfuTransportsCleaned = null;
    private Long lastSfuInboundRtpPadsClean = null;
    private Long lastSfuOutboundRtpPadsClean = null;
    private Long lastSfuSctpChannelsClean = null;


    public Task<Void> createTask() {
        var result = ChainedTask.<Void>builder()
                .withName(this.getClass().getSimpleName())
                .addActionStage("Clean Sfus", this::cleanSfus)
                .addActionStage("Clean Sfu Transports", this::cleanSfuTransports)
                .addActionStage("Clean Sfu Inbound Rtp Pads", this::cleanSfuInboundRtpPads)
                .addActionStage("Clean Sfu Outbound Rtp Pads", this::cleanSfuOutboundRtpPads)
                .addActionStage("Clean Sfu Sctp Channels", this::cleanSfuSctpChannels)
                .addActionStage("Commit Sfu Entity changes", () -> {
                    this.sfuSctpChannelsRepository.save();
                    this.sfuInboundRtpPadsRepository.save();
                    this.sfuOutboundRtpPadsRepository.save();
                    this.sfuTransportsRepository.save();
                    this.sfusRepository.save();
                });
        return result.build();
    }



    private void cleanSfus() {
        var serverTimestamp = this.serverTimestamps.instant().toEpochMilli();
        var sfuMaxIdleTimeInMs = this.observerConfig.repository.sfuMaxIdleTimeInS * 1000;
        var thresholdInMs = serverTimestamp - sfuMaxIdleTimeInMs;
        if (this.lastSfusClean != null && thresholdInMs <  this.lastSfusClean) {
            return;
        }
        this.lastSfusClean = serverTimestamp;

        var expiredSfus = Utils.firstNotNull(this.sfusRepository.getAllLocallyStored(), Collections.<String, Sfu>emptyMap()).values()
                .stream()
                .filter(obj -> {
                    var serverTouched = obj.getServerTouch();
                    if (serverTouched == null) {
                        logger.warn("Cannot compare last server timestamp for Sfu {}, because the serverTouched is null", obj.getSfuId());
                        return false;
                    }
                    return serverTouched < thresholdInMs;
                })
                .collect(Collectors.toMap(
                        sfu -> sfu.getSfuId(),
                        Function.identity()
                ));
        if (expiredSfus.size() < 1) {
            return;
        }
        logger.info("Found {} expired Sfus", expiredSfus.size());
        this.sfusRepository.deleteAll(expiredSfus.keySet());
    }


    private void cleanSfuTransports() {
        var serverTimestamp = this.serverTimestamps.instant().toEpochMilli();
        var transportsMaxIdleInMs = this.observerConfig.repository.sfuTransportMaxIdleTimeInS * 1000;
        var thresholdInMs = serverTimestamp - transportsMaxIdleInMs;
        if (this.lastSfuTransportsCleaned != null && thresholdInMs <  this.lastSfuTransportsCleaned) {
            return;
        }
        this.lastSfuTransportsCleaned = serverTimestamp;

        var expiredSfuTransports = Utils.firstNotNull(this.sfuTransportsRepository.getAllLocallyStored(), Collections.<String, SfuTransport>emptyMap()).values()
                .stream()
                .filter(obj -> {
                    var serverTouched = obj.getServerTouch();
                    if (serverTouched == null) {
                        logger.warn("Cannot compare last server timestamp for Sfu Transport {}, because the serverTouched is null", obj.getSfuTransportId());
                        return false;
                    }
                    return serverTouched < thresholdInMs;
                })
                .collect(Collectors.toMap(
                        sfuTransport -> sfuTransport.getSfuTransportId(),
                        Function.identity()
                ));
        if (expiredSfuTransports.size() < 1) {
            return;
        }
        logger.info("Found {} expired SfuTransport", expiredSfuTransports.size());
        this.sfuTransportsRepository.deleteAll(expiredSfuTransports.keySet());
    }

    private void cleanSfuInboundRtpPads() {
        var serverTimestamp = this.serverTimestamps.instant().toEpochMilli();
        var sfuInboundRtpPadMaxIdleTimeInS = this.observerConfig.repository.sfuInboundRtpPadMaxIdleTimeInS * 1000;
        var thresholdInMs = serverTimestamp - sfuInboundRtpPadMaxIdleTimeInS;
        if (this.lastSfuInboundRtpPadsClean != null && thresholdInMs <  this.lastSfuInboundRtpPadsClean) {
            return;
        }
        this.lastSfuInboundRtpPadsClean = serverTimestamp;

        var expiredSfuInboundRtpPads = Utils.firstNotNull(this.sfuInboundRtpPadsRepository.getAllLocallyStored(), Collections.<String, SfuInboundRtpPad>emptyMap()).values()
                .stream()
                .filter(obj -> {
                    var serverTouched = obj.getServerTouch();
                    if (serverTouched == null) {
                        logger.warn("Cannot compare last server timestamp for Sfu Inbound Rtp Pad {}, because the serverTouched is null", obj.getRtpPadId());
                        return false;
                    }
                    return serverTouched < thresholdInMs;
                })
                .collect(Collectors.toMap(
                        sfuInboundRtpPad -> sfuInboundRtpPad.getRtpPadId(),
                        Function.identity()
                ));
        if (expiredSfuInboundRtpPads.size() < 1) {
            return;
        }
        logger.info("Found {} expired Sfu Inbound Rtp Pads", expiredSfuInboundRtpPads.size());
        this.sfuInboundRtpPadsRepository.deleteAll(expiredSfuInboundRtpPads.keySet());
    }


    private void cleanSfuOutboundRtpPads() {
        var serverTimestamp = this.serverTimestamps.instant().toEpochMilli();
        var sfuOutboundRtpPadMaxIdleTimeInS = this.observerConfig.repository.sfuOutboundRtpPadMaxIdleTimeInS * 1000;
        var thresholdInMs = serverTimestamp - sfuOutboundRtpPadMaxIdleTimeInS;
        if (this.lastSfuOutboundRtpPadsClean != null && thresholdInMs <  this.lastSfuOutboundRtpPadsClean) {
            return;
        }
        this.lastSfuOutboundRtpPadsClean = serverTimestamp;

        var expiredSfuOutboundRtpPads = Utils.firstNotNull(this.sfuOutboundRtpPadsRepository.getAllLocallyStored(), Collections.<String, SfuOutboundRtpPad>emptyMap()).values()
                .stream()
                .filter(obj -> {
                    var serverTouched = obj.getServerTouch();
                    if (serverTouched == null) {
                        logger.warn("Cannot compare last server timestamp for Sfu Outbound Rtp Pad {}, because the serverTouched is null", obj.getRtpPadId());
                        return false;
                    }
                    return serverTouched < thresholdInMs;
                })
                .collect(Collectors.toMap(
                        sfuInboundRtpPad -> sfuInboundRtpPad.getRtpPadId(),
                        Function.identity()
                ));
        if (expiredSfuOutboundRtpPads.size() < 1) {
            return;
        }
        logger.info("Found {} expired Sfu Outbound Rtp Pads", expiredSfuOutboundRtpPads.size());
        this.sfuInboundRtpPadsRepository.deleteAll(expiredSfuOutboundRtpPads.keySet());
    }


    private void cleanSfuSctpChannels() {
        var serverTimestamp = this.serverTimestamps.instant().toEpochMilli();
        var sfuSctpChannelMaxIdleTimeInS = this.observerConfig.repository.sfuSctpChannelMaxIdleTimeInS * 1000;
        logger.debug("Clean outbound track process has been started");
        var thresholdInMs = serverTimestamp - sfuSctpChannelMaxIdleTimeInS;
        if (this.lastSfuSctpChannelsClean != null && thresholdInMs <  this.lastSfuSctpChannelsClean) {
            return;
        }
        this.lastSfuSctpChannelsClean = serverTimestamp;

        var expiredSfuSctpChannels = Utils.firstNotNull(this.sfuSctpChannelsRepository.getAllLocallyStored(), Collections.<String, SfuSctpChannel>emptyMap()).values()
                .stream()
                .filter(obj -> {
                    var serverTouched = obj.getServerTouch();
                    if (serverTouched == null) {
                        logger.warn("Cannot compare last server timestamp for OutboundTrack {}, because the serverTouched is null", obj.getSfuSctpChannelId());
                        return false;
                    }
                    return serverTouched < thresholdInMs;
                })
                .collect(Collectors.toMap(
                        sfuSctpChannel -> sfuSctpChannel.getSfuSctpChannelId(),
                        Function.identity()
                ));
        if (expiredSfuSctpChannels.size() < 1) {
            return;
        }
        logger.info("Found {} expired Sfu Sctp Channel", expiredSfuSctpChannels.size());
        this.sfuSctpChannelsRepository.deleteAll(expiredSfuSctpChannels.keySet());
    }
    
}
