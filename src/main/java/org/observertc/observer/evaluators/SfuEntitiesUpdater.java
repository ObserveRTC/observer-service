package org.observertc.observer.evaluators;

import io.micronaut.context.annotation.Prototype;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.subjects.PublishSubject;
import io.reactivex.rxjava3.subjects.Subject;
import jakarta.inject.Inject;
import org.observertc.observer.BackgroundTasksExecutor;
import org.observertc.observer.ServerTimestamps;
import org.observertc.observer.configs.ObserverConfig;
import org.observertc.observer.evaluators.eventreports.*;
import org.observertc.observer.metrics.EvaluatorMetrics;
import org.observertc.observer.repositories.*;
import org.observertc.observer.repositories.tasks.CleanSfuEntities;
import org.observertc.observer.repositories.tasks.CommitSfuEntities;
import org.observertc.observer.samples.ObservedSfuSamples;
import org.observertc.schemas.dtos.Models;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.time.Instant;
import java.util.LinkedList;
import java.util.function.Consumer;

@Prototype
public class SfuEntitiesUpdater implements Consumer<ObservedSfuSamples> {
    private static final Logger logger = LoggerFactory.getLogger(SfuEntitiesUpdater.class);
    private static final String METRIC_COMPONENT_NAME = SfuEntitiesUpdater.class.getSimpleName();

    @Inject
    EvaluatorMetrics exposedMetrics;

    @Inject
    SfusRepository sfusRepository;

    @Inject
    SfuTransportsRepository sfuTransportsRepository;

    @Inject
    SfuInboundRtpPadsRepository sfuInboundRtpPadsRepository;

    @Inject
    SfuOutboundRtpPadsRepository sfuOutboundRtpPadsRepository;

    @Inject
    SfuSctpChannelsRepository sfuSctpStreamsRepository;

    @Inject
    SfuJoinedReports sfuJoinedReports;

    @Inject
    SfuTransportOpenedReports sfuTransportOpenedReports;

    @Inject
    SfuInboundRtpPadAddedReports sfuInboundRtpPadAddedReports;

    @Inject
    SfuOutboundRtpPadAddedReports sfuOutboundRtpPadAddedReports;

    @Inject
    SfuSctpStreamAddedReports sfuSctpStreamAddedReports;

    @Inject
    ServerTimestamps serverTimestamps;

    @Inject
    ObserverConfig observerConfig;

    @Inject
    BackgroundTasksExecutor backgroundTasksExecutor;

    @Inject
    CleanSfuEntities cleanSfuEntities;

    @Inject
    CommitSfuEntities commitSfuEntities;

    @PostConstruct
    void setup() {
        this.backgroundTasksExecutor.addPeriodicTask(
                this.getClass().getSimpleName(),
                this.cleanSfuEntities::createTask,
                60 * 1000
        );
    }

    @PreDestroy
    void teardown() {
        this.backgroundTasksExecutor.removePeriodicTask(this.getClass().getSimpleName());
    }

    private Subject<ObservedSfuSamples> output = PublishSubject.create();

    public Observable<ObservedSfuSamples> observableClientSamples() {
        return this.output;
    }

    public void accept(ObservedSfuSamples observedSfuSamples) {
        if (observedSfuSamples == null) {
            return;
        }
        if (observedSfuSamples.isEmpty()) {
            this.output.onNext(observedSfuSamples);
            return;
        }
        Instant started = Instant.now();
        try {
            this.commitSfuEntities.lock();
            this.process(observedSfuSamples);
        } finally {
            this.exposedMetrics.addTaskExecutionTime(METRIC_COMPONENT_NAME, started, Instant.now());
            this.commitSfuEntities.unlock();
            if (!this.commitSfuEntities.executeSync()) {
                logger.warn("Committing SFU entities was unsuccesful");;
            }
        }
    }

    private void process(ObservedSfuSamples observedSfuSamples) {
        if (observedSfuSamples.isEmpty()) {
            return;
        }
        var SFUs = this.sfusRepository.fetchRecursively(observedSfuSamples.getSfuIds());
        var newSfuModels = new LinkedList<Models.Sfu>();
        var newSfuTransportModels = new LinkedList<Models.SfuTransport>();
        var newSfuInboundRtpPadModels = new LinkedList<Models.SfuInboundRtpPad>();
        var newSfuOutboundRtpPadModels = new LinkedList<Models.SfuOutboundRtpPad>();
        var newSfuSctpChannelModels = new LinkedList<Models.SfuSctpChannel>();
        var serverTimestamp = this.serverTimestamps.instant().getEpochSecond();

        for (var observedSfu : observedSfuSamples.observedSfus()) {
            var sfu = SFUs.get(observedSfu.getSfuId());
            if (sfu == null) {
                try {
                    sfu = this.sfusRepository.add(
                            observedSfu.getServiceId(),
                            observedSfu.getMediaUnitId(),
                            observedSfu.getSfuId(),
                            observedSfu.getMinTimestamp(),
                            observedSfu.getTimeZoneId(),
                            observedSfu.getMarker()
                    );
                    newSfuModels.add(sfu.getModel());
                } catch (AlreadyCreatedException ex) {
                    logger.warn("SFU {} for mediaUnit {} service: {} is already created",
                        observedSfu.getSfuId(),
                        observedSfu.getMediaUnitId(),
                        observedSfu.getServiceId()
                    );
                    sfu = SFUs.get(observedSfu.getSfuId());
                }
                if (sfu == null) {
                    logger.warn("SFU {} for mediaUnit {} service: {} is not found",
                            observedSfu.getSfuId(),
                            observedSfu.getMediaUnitId(),
                            observedSfu.getServiceId()
                    );
                    continue;
                }
            }
            sfu.touch(observedSfu.getMaxTimestamp(), serverTimestamp);

            for (var observedSfuTransport : observedSfu.observedSfuTransports()) {
                var sfuTransport = sfu.getSfuTransport(observedSfuTransport.getSfuTransportId());
                if (sfuTransport == null) {
                    try {
                        sfuTransport = sfu.addSfuTransport(
                                observedSfuTransport.getSfuTransportId(),
                                observedSfuTransport.getInternal(),
                                observedSfuTransport.getMinTimestamp(),
                                observedSfuTransport.getMarker()
                        );
                        newSfuTransportModels.add(sfuTransport.getModel());
                    } catch (AlreadyCreatedException ex) {
                        logger.warn("SfuTransport {} for SFU {} for mediaUnit {} service: {} is already created",
                                observedSfuTransport.getSfuTransportId(),
                                observedSfu.getSfuId(),
                                observedSfu.getMediaUnitId(),
                                observedSfu.getServiceId()
                        );
                        sfuTransport = sfu.getSfuTransport(observedSfuTransport.getSfuTransportId());
                    }
                    if (sfuTransport == null) {
                        logger.warn("SFUTransport {} for SFU {} for mediaUnit {} service: {} is not found",
                                observedSfuTransport.getSfuTransportId(),
                                observedSfu.getSfuId(),
                                observedSfu.getMediaUnitId(),
                                observedSfu.getServiceId()
                        );
                        continue;
                    }
                }
                sfuTransport.touch(observedSfuTransport.getMaxTimestamp(), serverTimestamp);

                for (var observedSfuInboundRtpPad : observedSfuTransport.observedSfuInboundRtpPads()) {
                    var sfuInboundRtpPad = sfuTransport.getInboundRtpPad(observedSfuInboundRtpPad.getPadId());
                    if (sfuInboundRtpPad == null) {
                        try {
                            sfuInboundRtpPad = sfuTransport.addInboundRtpPad(
                                    observedSfuInboundRtpPad.getPadId(),
                                    observedSfuInboundRtpPad.getSsrc(),
                                    observedSfuInboundRtpPad.getSfuStreamId(),
                                    observedSfuInboundRtpPad.getMinTimestamp(),
                                    observedSfuInboundRtpPad.getMarker()
                            );
                            newSfuInboundRtpPadModels.add(sfuInboundRtpPad.getModel());
                        } catch (AlreadyCreatedException ex) {
                            logger.warn("SfuInboundRtpPad {} on transport {} for SFU {} for mediaUnit {} service: {} is already created",
                                    observedSfuInboundRtpPad.getPadId(),
                                    observedSfuTransport.getSfuTransportId(),
                                    observedSfu.getSfuId(),
                                    observedSfu.getMediaUnitId(),
                                    observedSfu.getServiceId()
                            );
                            sfuInboundRtpPad = sfuTransport.getInboundRtpPad(observedSfuInboundRtpPad.getPadId());
                        }
                        if (sfuInboundRtpPad == null) {
                            logger.warn("SfuInboundRtpPad {} on transport {} for SFU {} for mediaUnit {} service: {} is not found",
                                    observedSfuInboundRtpPad.getPadId(),
                                    observedSfuTransport.getSfuTransportId(),
                                    observedSfu.getSfuId(),
                                    observedSfu.getMediaUnitId(),
                                    observedSfu.getServiceId()
                            );
                            continue;
                        }
                    }
                    sfuInboundRtpPad.touch(observedSfuTransport.getMaxTimestamp(), serverTimestamp);
                    var sfuMediaStream = sfuInboundRtpPad.getSfuStream();
                    if (sfuMediaStream != null && !sfuMediaStream.hasSfuInboundRtpPadId(sfuInboundRtpPad.getRtpPadId())) {
                        sfuMediaStream.addSfuInboundRtpPadId(sfuInboundRtpPad.getRtpPadId());
                    }
                }

                for (var observedSfuOutboundRtpPad : observedSfuTransport.observedSfuOutboundRtpPads()) {
                    var sfuOutboundRtpPad = sfuTransport.getOutboundRtpPad(observedSfuOutboundRtpPad.getPadId());
                    if (sfuOutboundRtpPad == null) {
                        try {
                            sfuOutboundRtpPad = sfuTransport.addOutboundRtpPad(
                                    observedSfuOutboundRtpPad.getPadId(),
                                    observedSfuOutboundRtpPad.getSsrc(),
                                    observedSfuOutboundRtpPad.getSfuStreamId(),
                                    observedSfuOutboundRtpPad.getSfuSinkId(),
                                    observedSfuOutboundRtpPad.getMinTimestamp(),
                                    observedSfuOutboundRtpPad.getMarker()
                            );
                            newSfuOutboundRtpPadModels.add(sfuOutboundRtpPad.getModel());
                        } catch (AlreadyCreatedException ex) {
                            logger.warn("sfuOutboundRtpPad {} on transport {} for SFU {} for mediaUnit {} service: {} is already created",
                                    observedSfuOutboundRtpPad.getPadId(),
                                    observedSfuTransport.getSfuTransportId(),
                                    observedSfu.getSfuId(),
                                    observedSfu.getMediaUnitId(),
                                    observedSfu.getServiceId()
                            );
                            sfuOutboundRtpPad = sfuTransport.getOutboundRtpPad(observedSfuOutboundRtpPad.getPadId());
                        }
                        if (sfuOutboundRtpPad == null) {
                            logger.warn("sfuOutboundRtpPad {} on transport {} for SFU {} for mediaUnit {} service: {} is not found",
                                    observedSfuOutboundRtpPad.getPadId(),
                                    observedSfuTransport.getSfuTransportId(),
                                    observedSfu.getSfuId(),
                                    observedSfu.getMediaUnitId(),
                                    observedSfu.getServiceId()
                            );
                            continue;
                        }
                    }
                    sfuOutboundRtpPad.touch(observedSfuTransport.getMaxTimestamp(), serverTimestamp);
                    var sfuMediaSink = sfuOutboundRtpPad.getSfuSink();
                    if (sfuMediaSink != null) {
                        if (!sfuMediaSink.hasSfuOutboundRtpPadId(sfuOutboundRtpPad.getRtpPadId())) {
                            try {
                                sfuMediaSink.addSfuOutboundRtpPadId(sfuOutboundRtpPad.getRtpPadId());
                            } catch (AlreadyCreatedException ex) {
                                logger.warn("Sfu Outbound Rtp Pad {} already added for media sink {}, sfu: {}, service {}",
                                        sfuOutboundRtpPad.getRtpPadId(),
                                        sfuMediaSink.getSfuSinkId(),
                                        observedSfu.getSfuId(),
                                        observedSfu.getServiceId()
                                );
                            }
                        }
                        var sfuMediaStream = sfuMediaSink.getMediaStream();
                        if (sfuMediaStream != null && !sfuMediaStream.hasMediaSink(sfuMediaSink.getSfuSinkId())) {
                            try {
                                sfuMediaStream.addSfuMediaSink(sfuMediaSink.getSfuSinkId());
                            } catch (AlreadyCreatedException ex) {
                                logger.warn("Sfu Media Sink {} already added for media stream {}, sfu: {}, service {}",
                                        sfuMediaSink.getSfuSinkId(),
                                        sfuMediaStream.getSfuStreamId(),
                                        observedSfu.getSfuId(),
                                        observedSfu.getServiceId()
                                );
                            }
                        }
                    }
                }

                for (var observedSfuSctpChannel : observedSfuTransport.observedSfuSctpChannels()) {
                    var sfuSctpChannel = sfuTransport.getSctpChannel(observedSfuSctpChannel.getSfuSctpChannelId());
                    if (sfuSctpChannel == null) {
                        try {
                            sfuSctpChannel = sfuTransport.addSctpChannel(
                                    observedSfuSctpChannel.getSfuSctpStreamId(),
                                    observedSfuSctpChannel.getSfuSctpChannelId(),
                                    observedSfuSctpChannel.getMinTimestamp(),
                                    observedSfuSctpChannel.getMarker()
                            );
                            newSfuSctpChannelModels.add(sfuSctpChannel.getModel());
                        } catch (AlreadyCreatedException ex) {
                            logger.warn("sfuSctpChannel {} on transport {} for SFU {} for mediaUnit {} service: {} is already created",
                                    observedSfuSctpChannel.getSfuSctpChannelId(),
                                    observedSfuTransport.getSfuTransportId(),
                                    observedSfu.getSfuId(),
                                    observedSfu.getMediaUnitId(),
                                    observedSfu.getServiceId()
                            );
                            sfuSctpChannel = sfuTransport.getSctpChannel(observedSfuSctpChannel.getSfuSctpChannelId());
                        }
                        if (sfuSctpChannel == null) {
                            logger.warn("sfuOutboundRtpPad {} on transport {} for SFU {} for mediaUnit {} service: {} is not found",
                                    observedSfuSctpChannel.getSfuSctpChannelId(),
                                    observedSfuTransport.getSfuTransportId(),
                                    observedSfu.getSfuId(),
                                    observedSfu.getMediaUnitId(),
                                    observedSfu.getServiceId()
                            );
                            continue;
                        }
                    }
                    sfuSctpChannel.touch(observedSfuTransport.getMaxTimestamp(), serverTimestamp);
                }
            }
        }

        if (0 < newSfuModels.size()) {
            this.sfuJoinedReports.accept(newSfuModels);
        }
        if (0 < newSfuTransportModels.size()) {
            this.sfuTransportOpenedReports.accept(newSfuTransportModels);
        }
        if (0 < newSfuInboundRtpPadModels.size()) {
            this.sfuInboundRtpPadAddedReports.accept(newSfuInboundRtpPadModels);
        }
        if (0 < newSfuOutboundRtpPadModels.size()) {
            this.sfuOutboundRtpPadAddedReports.accept(newSfuOutboundRtpPadModels);
        }
        if (0 < newSfuSctpChannelModels.size()) {
            this.sfuSctpStreamAddedReports.accept(newSfuSctpChannelModels);
        }
        if (0 < observedSfuSamples.size()) {
            synchronized (this) {
                this.output.onNext(observedSfuSamples);
            }
        }
    }
}
