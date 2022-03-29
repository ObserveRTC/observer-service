package org.observertc.observer.components;

import io.micronaut.context.annotation.Prototype;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.subjects.PublishSubject;
import io.reactivex.rxjava3.subjects.Subject;
import org.observertc.observer.components.depots.*;
import org.observertc.observer.reports.Report;
import org.observertc.observer.repositories.tasks.FetchSfuRelationsTask;
import org.observertc.observer.samples.ObservedSfuSamples;
import org.observertc.observer.samples.SfuSampleVisitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;

@Prototype
public class SfuSamplesAnalyzer implements Consumer<ObservedSfuSamples> {
    private static final Logger logger = LoggerFactory.getLogger(SfuSamplesAnalyzer.class);

    @Inject
    Provider<FetchSfuRelationsTask> fetchSfuRelationsTaskProvider;

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
        for (var observedSfuSample : observedSfuSamples) {
            var sfuSample = observedSfuSample.getSfuSample();
            SfuSampleVisitor.streamTransports(sfuSample).forEach(sfuTransport -> {
                this.sfuTransportReportsDepot
//                        .setCallId(
                        .setObservedSfuSample(observedSfuSample)
                        .setSfuTransport(sfuTransport)
                        .assemble();
            });

            SfuSampleVisitor.streamInboundRtpPads(sfuSample).forEach(sfuInboundRtpPad -> {
                UUID sfuStreamId = sfuInboundRtpPad.streamId;
                if (Objects.nonNull(sfuStreamId)) {
                    var sfuStream = sfuStreams.get(sfuStreamId);
                    if (Objects.nonNull(sfuStream)) {
                        this.sfuInboundRtpPadReportsDepot
                                .setCallId(sfuStream.callId)
                                .setTrackId(sfuStream.trackId)
                                .setClientId(sfuStream.clientId)
                                ;
                    }
                }
                this.sfuInboundRtpPadReportsDepot
                        .setObservedSfuSample(observedSfuSample)
                        .setSfuInboundRtpPad(sfuInboundRtpPad)
                        .assemble();
            });

            SfuSampleVisitor.streamOutboundRtpPads(sfuSample).forEach(sfuOutboundRtpPad -> {
                UUID sfuSinkId = sfuOutboundRtpPad.sinkId;
                if (Objects.nonNull(sfuSinkId)) {
                    var sfuStream = sfuSinks.get(sfuSinkId);
                    if (Objects.nonNull(sfuStream)) {
                        this.sfuOutboundRtpPadReportsDepot
                                .setCallId(sfuStream.callId)
                                .setTrackId(sfuStream.trackId)
                                .setClientId(sfuStream.clientId)
                        ;
                    }
                }
                this.sfuOutboundRtpPadReportsDepot
                        .setObservedSfuSample(observedSfuSample)
                        .setSfuOutboundRtpPad(sfuOutboundRtpPad)
                        .assemble();
            });

            SfuSampleVisitor.streamSctpStreams(sfuSample).forEach(sctpChannel -> {
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
