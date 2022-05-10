package org.observertc.observer.evaluators;

import io.micronaut.context.BeanProvider;
import io.micronaut.context.annotation.Prototype;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.subjects.PublishSubject;
import io.reactivex.rxjava3.subjects.Subject;
import jakarta.inject.Inject;
import org.observertc.observer.configs.ObserverConfig;
import org.observertc.observer.evaluators.depots.*;
import org.observertc.observer.reports.Report;
import org.observertc.observer.repositories.tasks.FetchSfuRelationsTask;
import org.observertc.observer.samples.ObservedSfuSamples;
import org.observertc.observer.samples.SfuSampleVisitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;

@Prototype
public class SfuSamplesAnalyser implements Consumer<ObservedSfuSamples> {
    private static final Logger logger = LoggerFactory.getLogger(SfuSamplesAnalyser.class);

    @Inject
    BeanProvider<FetchSfuRelationsTask> fetchSfuRelationsTaskProvider;

    @Inject
    ObserverConfig.EvaluatorsConfig.SfuSamplesAnalyserConfig config;

    private Subject<List<Report>> output = PublishSubject.create();
    private final SfuTransportReportsDepot sfuTransportReportsDepot = new SfuTransportReportsDepot();
    private final SfuInboundRtpPadReportsDepot sfuInboundRtpPadReportsDepot = new SfuInboundRtpPadReportsDepot();
    private final SfuOutboundRtpPadReportsDepot sfuOutboundRtpPadReportsDepot = new SfuOutboundRtpPadReportsDepot();
    private final SfuSctpStreamReportsDepot sfuSctpStreamReportsDepot = new SfuSctpStreamReportsDepot();
    private final SfuExtensionReportsDepot sfuExtensionReportsDepot = new SfuExtensionReportsDepot();

    public Observable<List<Report>> observableReports() {
        return this.output;
    }

    public void accept(ObservedSfuSamples observedSfuSamples) {
        if (observedSfuSamples.isEmpty()) {
            return;
        }
        var task = this.fetchSfuRelationsTaskProvider.get()
                .whereSfuRtpPadIds(observedSfuSamples.getRtpPadIds())
                ;
        if (!task.execute().succeeded()) {
            logger.warn("Interrupted execution of component due to unsuccessful task execution");
            return;
        }
        var taskResult = task.getResult();
        var sfuStreams = taskResult.sfuStreams;
        var sfuSinks = taskResult.sfuSinks;
        var internalInboundRtpPadMatches = taskResult.internalInboundRtpPadMatches;
        for (var observedSfuSample : observedSfuSamples) {
            var sfuSample = observedSfuSample.getSfuSample();
            SfuSampleVisitor.streamTransports(sfuSample).forEach(sfuTransport -> {
                if (Boolean.TRUE.equals(sfuTransport.noReport)) {
                    return;
                }
                this.sfuTransportReportsDepot
//                        .setCallId(
                        .setObservedSfuSample(observedSfuSample)
                        .setSfuTransport(sfuTransport)
                        .assemble();
            });

            SfuSampleVisitor.streamInboundRtpPads(sfuSample).forEach(sfuInboundRtpPad -> {
                if (Boolean.TRUE.equals(sfuInboundRtpPad.noReport)) {
                    return;
                }

                UUID sfuStreamId = sfuInboundRtpPad.streamId;
                if (Objects.nonNull(sfuStreamId)) {
                    var sfuStream = sfuStreams.get(sfuStreamId);
                    if (Objects.nonNull(sfuStream)) {
                        this.sfuInboundRtpPadReportsDepot
                                .setCallId(sfuStream.callId)
                                .setTrackId(sfuStream.trackId)
                                .setClientId(sfuStream.clientId)
                                ;
                    } else if (config.dropUnmatchedInboundReports) {
                        this.sfuInboundRtpPadReportsDepot.clean();
                        return;
                    }
                }
                var inboundRtpPadMatch = internalInboundRtpPadMatches.get(sfuInboundRtpPad.padId);
                if (Boolean.TRUE.equals(sfuInboundRtpPad.internal)) {
                    if (inboundRtpPadMatch != null) {
                        this.sfuInboundRtpPadReportsDepot
                                .setRemoteSfuId(inboundRtpPadMatch.outboundSfuId)
                                .setRemoteTransportId(inboundRtpPadMatch.outboundTransportId)
                                .setRemoteSinkId(inboundRtpPadMatch.outboundSinkId)
                                .setRemoteRtpPadId(inboundRtpPadMatch.outboundRtpPadId)
                        ;
                    } else if (config.dropUnmatchedInternalInboundReports) {
                        this.sfuInboundRtpPadReportsDepot.clean();
                        return;
                    }

                }
                this.sfuInboundRtpPadReportsDepot
                        .setObservedSfuSample(observedSfuSample)
                        .setSfuInboundRtpPad(sfuInboundRtpPad)
                        .assemble();
            });

            SfuSampleVisitor.streamOutboundRtpPads(sfuSample).forEach(sfuOutboundRtpPad -> {
                if (Boolean.TRUE.equals(sfuOutboundRtpPad.noReport)) {
                    return;
                }
                UUID sfuSinkId = sfuOutboundRtpPad.sinkId;
                if (Objects.nonNull(sfuSinkId)) {
                    var sfuSink = sfuSinks.get(sfuSinkId);
                    if (Objects.nonNull(sfuSink)) {
                        this.sfuOutboundRtpPadReportsDepot
                                .setCallId(sfuSink.callId)
                                .setTrackId(sfuSink.trackId)
                                .setClientId(sfuSink.clientId)
                        ;
                    } else if (config.dropUnmatchedOutboundReports) {
                        return;
                    }
                }
                this.sfuOutboundRtpPadReportsDepot
                        .setObservedSfuSample(observedSfuSample)
                        .setSfuOutboundRtpPad(sfuOutboundRtpPad)
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
        }
        var reports = new LinkedList<Report>();
        this.sfuTransportReportsDepot.get().stream().map(Report::fromSfuTransportReport).forEach(reports::add);
        this.sfuInboundRtpPadReportsDepot.get().stream().map(Report::fromSfuInboundRtpPadReport).forEach(reports::add);
        this.sfuOutboundRtpPadReportsDepot.get().stream().map(Report::fromSfuOutboundRtpPadReport).forEach(reports::add);
        this.sfuSctpStreamReportsDepot.get().stream().map(Report::fromSfuSctpStreamReport).forEach(reports::add);
        this.sfuExtensionReportsDepot.get().stream().map(Report::fromSfuExtensionReport).forEach(reports::add);
        if (0 < reports.size()) {
            this.output.onNext(reports);
        }
    }

}
