package org.observertc.observer.evaluators;

import io.micronaut.context.annotation.Prototype;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.subjects.PublishSubject;
import io.reactivex.rxjava3.subjects.Subject;
import jakarta.inject.Inject;
import org.observertc.observer.configs.ObserverConfig;
import org.observertc.observer.evaluators.depots.*;
import org.observertc.observer.metrics.EvaluatorMetrics;
import org.observertc.observer.reports.Report;
import org.observertc.observer.repositories.*;
import org.observertc.observer.samples.ObservedSfuSamples;
import org.observertc.observer.samples.SfuSampleVisitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;

@Prototype
public class SfuSamplesAnalyser implements Consumer<ObservedSfuSamples> {
    private static final Logger logger = LoggerFactory.getLogger(SfuSamplesAnalyser.class);
    private static final String METRIC_COMPONENT_NAME = SfuSamplesAnalyser.class.getSimpleName();

    @Inject
    EvaluatorMetrics exposedMetrics;

//    @Inject
//    BeanProvider<MatchInternalSfuRtpPads> matchInternalSfuRtpPads;

    @Inject
    SfuInboundRtpPadsRepository sfuInboundRtpPadsRepository;

    @Inject
    SfuOutboundRtpPadsRepository sfuOutboundRtpPadsRepository;

    @Inject
    SfuMediaSinksRepository sfuMediaSinksRepository;

    @Inject
    SfuMediaStreamsRepository sfuMediaStreamsRepository;

    @Inject
    ObserverConfig.EvaluatorsConfig.SfuSamplesAnalyserConfig config;

    private Subject<List<Report>> output = PublishSubject.create();
    private final SfuTransportReportsDepot sfuTransportReportsDepot = new SfuTransportReportsDepot();
    private final SfuInboundRtpPadReportsDepot sfuInboundRtpPadReportsDepot = new SfuInboundRtpPadReportsDepot();
    private final SfuOutboundRtpPadReportsDepot sfuOutboundRtpPadReportsDepot = new SfuOutboundRtpPadReportsDepot();
    private final SfuSctpStreamReportsDepot sfuSctpStreamReportsDepot = new SfuSctpStreamReportsDepot();
    private final SfuExtensionReportsDepot sfuExtensionReportsDepot = new SfuExtensionReportsDepot();
    private final CustomSfuEventReportsDepot customSfuEventReportsDepot = new CustomSfuEventReportsDepot();

    public Observable<List<Report>> observableReports() {
        return this.output;
    }

    public void accept(ObservedSfuSamples observedSfuSamples) {
        if (observedSfuSamples == null) {
            return;
        }
        if (observedSfuSamples.isEmpty()) {
            this.output.onNext(Collections.emptyList());
            return;
        }
        Instant started = Instant.now();
        try {
            this.process(observedSfuSamples);
        } finally {
            this.exposedMetrics.addTaskExecutionTime(METRIC_COMPONENT_NAME, started, Instant.now());
        }
    }

    private Map<String, SfuOutboundRtpPad> getInternalSfuInboundRtpPadToSfuOutboundRtpPad(Map<String, SfuInboundRtpPad> sfuInboundRtpPads) {
        var streamIdToInternalInboundRtpPads = sfuInboundRtpPads.values().stream()
                .filter(sfuInboundRtpPad -> sfuInboundRtpPad.isInternal() && sfuInboundRtpPad.getSfuStreamId() != null)
                .collect(groupingBy(SfuInboundRtpPad::getSfuStreamId));
        if (streamIdToInternalInboundRtpPads.size() < 1) {
            return Collections.emptyMap();
        }
        var mediaStreams = this.sfuMediaStreamsRepository.getAll(streamIdToInternalInboundRtpPads.keySet());
        if (mediaStreams == null || mediaStreams.size() < 1) {
            return Collections.emptyMap();
        }
        var mediaSinkIds = mediaStreams.values().stream()
                .map(SfuMediaStream::getSfuMediaSinkIds)
                .filter(Objects::nonNull)
                .flatMap(s -> s.stream())
                .collect(Collectors.toSet());
        var mediaSinks = this.sfuMediaSinksRepository.getAll(mediaSinkIds);
        if (mediaSinks == null || mediaSinks.size() < 1) {
            return Collections.emptyMap();
        }
        var sfuOutboundRtpPadIds = mediaSinks.values().stream()
                .map(SfuMediaSink::getSfuOutboundSfuRtpPadIds)
                .flatMap(s -> s.stream())
                .collect(Collectors.toSet());
        if (sfuOutboundRtpPadIds == null || sfuOutboundRtpPadIds.size() < 1) {
            return Collections.emptyMap();
        }
        var sfuOutboundRtpPads = this.sfuOutboundRtpPadsRepository.getAll(sfuOutboundRtpPadIds);
        var result = new HashMap<String, SfuOutboundRtpPad>();
        for (var entry : streamIdToInternalInboundRtpPads.entrySet()) {
            var mediaStreamId = entry.getKey();
            var mediaStream = mediaStreams.get(mediaStreamId);
            if (mediaStream == null || mediaStream.getSfuMediaSinkIds() == null) {
                continue;
            }
            var internalInboundRtpPads = entry.getValue();
            for (var sfuInternalInboundRtpPad : internalInboundRtpPads) {
                SfuOutboundRtpPad foundSfuOutboundRtpPad = null;
                for (var mediaSinkId : mediaStream.getSfuMediaSinkIds()) {
                    var mediaSink = mediaSinks.get(mediaSinkId);
                    if (mediaSink == null) {
                        continue;
                    }
                    var outboundRtpPadIds = mediaSink.getSfuOutboundSfuRtpPadIds();
                    if (outboundRtpPadIds == null || outboundRtpPadIds.size() < 1) {
                        continue;
                    }
                    for (var sfuOutboundRtpPad : sfuOutboundRtpPads.values()) {
                        if (sfuOutboundRtpPad.getSSRC() != null && sfuOutboundRtpPad.getSSRC().equals(sfuInternalInboundRtpPad.getSSRC())) {
                            foundSfuOutboundRtpPad = sfuOutboundRtpPad;
                            break;
                        }
                    }
                    if (foundSfuOutboundRtpPad != null) {
                        break;
                    }
                }
                if (foundSfuOutboundRtpPad != null) {
                    result.put(sfuInternalInboundRtpPad.getRtpPadId(), foundSfuOutboundRtpPad);
                }
            }
        }
        return result;
    }

    private void process(ObservedSfuSamples observedSfuSamples) {
        if (observedSfuSamples.isEmpty()) {
            return;
        }
        var sfuInboundRtpPads = this.sfuInboundRtpPadsRepository.fetchRecursively(observedSfuSamples.getInboundRtpPadIds());
        var sfuOutboundRtpPads = this.sfuOutboundRtpPadsRepository.fetchRecursively(observedSfuSamples.getOutboundRtpPadIds());
        var internalSfuInboundRtpPadToSfuOutboundRtpPad = this.getInternalSfuInboundRtpPadToSfuOutboundRtpPad(sfuInboundRtpPads);
        for (var observedSfuSample : observedSfuSamples) {
            var sfuSample = observedSfuSample.getSfuSample();
            SfuSampleVisitor.streamTransports(sfuSample).forEach(sfuTransport -> {
                if (Boolean.TRUE.equals(sfuTransport.noReport)) {
                    return;
                }
                this.sfuTransportReportsDepot
                        .setObservedSfuSample(observedSfuSample)
                        .setSfuTransport(sfuTransport)
                        .assemble();
            });

            SfuSampleVisitor.streamInboundRtpPads(sfuSample).forEach(sfuInboundRtpPadSample -> {
                if (Boolean.TRUE.equals(sfuInboundRtpPadSample.noReport)) {
                    return;
                }
                var sfuInboundRtpPad = sfuInboundRtpPads.get(sfuInboundRtpPadSample.padId);
                if (sfuInboundRtpPad != null) {
                    SfuMediaStream sfuMediaStream = sfuInboundRtpPad != null ? sfuInboundRtpPad.getSfuStream() : null;
                    if (sfuMediaStream != null) {
                        this.sfuInboundRtpPadReportsDepot
                                .setCallId(sfuMediaStream.getCallId())
                                .setClientId(sfuMediaStream.getClientId())
                                .setTrackId(sfuMediaStream.getTrackId())
                        ;
                    }
                    if (sfuInboundRtpPad.isInternal() && sfuInboundRtpPad.getSfuStreamId() != null) {
                        var internalSfuOutboundRtpPad = internalSfuInboundRtpPadToSfuOutboundRtpPad.get(sfuInboundRtpPad.getRtpPadId());
                        if (internalSfuOutboundRtpPad != null) {
                            this.sfuInboundRtpPadReportsDepot
                                    .setRemoteSfuId(internalSfuOutboundRtpPad.getSfuId())
                                    .setRemoteTransportId(internalSfuOutboundRtpPad.getSfuTransportId())
                                    .setRemoteSinkId(internalSfuOutboundRtpPad.getSfuSinkId())
                                    .setRemoteRtpPadId(internalSfuOutboundRtpPad.getRtpPadId())
                            ;
                        } else if (config.dropUnmatchedInternalInboundReports) {
                            this.sfuInboundRtpPadReportsDepot.clean();
                            return;
                        }
                    }
                }

                this.sfuInboundRtpPadReportsDepot
                        .setObservedSfuSample(observedSfuSample)
                        .setSfuInboundRtpPad(sfuInboundRtpPadSample)
                        .assemble();
            });

            SfuSampleVisitor.streamOutboundRtpPads(sfuSample).forEach(sfuOutboundRtpPadSample -> {
                if (Boolean.TRUE.equals(sfuOutboundRtpPadSample.noReport)) {
                    return;
                }
                var sfuOutboundRtpPad = sfuOutboundRtpPads.get(sfuOutboundRtpPadSample.padId);
                SfuMediaSink sfuMediaSink = sfuOutboundRtpPad != null ? sfuOutboundRtpPad.getSfuSink() : null;
                if (sfuMediaSink != null) {
                    this.sfuInboundRtpPadReportsDepot
                            .setCallId(sfuMediaSink.getCallId())
                            .setClientId(sfuMediaSink.getClientId())
                            .setTrackId(sfuMediaSink.getTrackId())
                    ;
                }
                this.sfuOutboundRtpPadReportsDepot
                        .setObservedSfuSample(observedSfuSample)
                        .setSfuOutboundRtpPad(sfuOutboundRtpPadSample)
                        .assemble();
            });

            SfuSampleVisitor.streamSctpStreams(sfuSample).forEach(sctpChannel -> {
                if (Boolean.TRUE.equals(sctpChannel.noReport)) {
                    return;
                }
                this.sfuSctpStreamReportsDepot
                        .setObservedSfuSample(observedSfuSample)
                        .setSctpChannel(sctpChannel)
                        .assemble();
            });

            SfuSampleVisitor.streamExtensionStats(sfuSample).forEach(sfuExtensionStats -> {
                this.sfuExtensionReportsDepot
                        .setObservedSfuSample(observedSfuSample)
                        .setExtensionType(sfuExtensionStats.type)
                        .setPayload(sfuExtensionStats.payload)
                        .assemble();
            });

            SfuSampleVisitor.streamCustomSfuEvents(sfuSample).forEach(customSfuEvent -> {
                this.customSfuEventReportsDepot
                        .setObservedSfuSample(observedSfuSample)
                        .setCustomSfuEvent(customSfuEvent)
                        .assemble();
            });
        }
        var reports = new LinkedList<Report>();
        this.sfuTransportReportsDepot.get().stream().map(Report::fromSfuTransportReport).forEach(reports::add);
        this.sfuInboundRtpPadReportsDepot.get().stream().map(Report::fromSfuInboundRtpPadReport).forEach(reports::add);
        this.sfuOutboundRtpPadReportsDepot.get().stream().map(Report::fromSfuOutboundRtpPadReport).forEach(reports::add);
        this.sfuSctpStreamReportsDepot.get().stream().map(Report::fromSfuSctpStreamReport).forEach(reports::add);
        this.sfuExtensionReportsDepot.get().stream().map(Report::fromSfuExtensionReport).forEach(reports::add);
        this.customSfuEventReportsDepot.get().stream().map(Report::fromSfuEventReport).forEach(reports::add);
        if (0 < reports.size()) {
            this.output.onNext(reports);
        }
    }

}
