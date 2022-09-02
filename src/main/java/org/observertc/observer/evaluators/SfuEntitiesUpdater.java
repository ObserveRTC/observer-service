package org.observertc.observer.evaluators;

import io.micronaut.context.annotation.Prototype;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.subjects.PublishSubject;
import io.reactivex.rxjava3.subjects.Subject;
import jakarta.inject.Inject;
import org.observertc.observer.evaluators.eventreports.*;
import org.observertc.observer.metrics.EvaluatorMetrics;
import org.observertc.observer.repositories.*;
import org.observertc.observer.samples.ObservedSfuSamples;
import org.observertc.observer.samples.SfuSampleVisitor;
import org.observertc.schemas.dtos.Models;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.function.BiFunction;
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
    SfuSctpStreamsRepository sfuSctpStreamsRepository;

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

    private Subject<ObservedSfuSamples> output = PublishSubject.create();

    public Observable<ObservedSfuSamples> observableClientSamples() {
        return this.output;
    }

    public void accept(ObservedSfuSamples observedSfuSamples) {
        Instant started = Instant.now();
        try {
            this.process(observedSfuSamples);
        } finally {
            this.exposedMetrics.addTaskExecutionTime(METRIC_COMPONENT_NAME, started, Instant.now());
        }
    }

    private void process(ObservedSfuSamples observedSfuSamples) {
        if (observedSfuSamples.isEmpty()) {
            return;
        }
        var sfus = this.fetchExistingSfus(observedSfuSamples);
        var sfuTransports = this.fetchExistingSfuTransports(observedSfuSamples);
        var sfuInboundRtpPads = this.fetchExistingInboundRtpPads(observedSfuSamples);
        var sfuOutboundRtpPads = this.fetchExistingOutboundRtpPads(observedSfuSamples);
        var sfuSctpStreams = this.fetchExistingSfuSctpStreams(observedSfuSamples);
        var newSfuModels = new LinkedList<Models.Sfu>();
        var newSfuTransportModels = new LinkedList<Models.SfuTransport>();
        var newSfuInboundRtpPadModels = new LinkedList<Models.SfuInboundRtpPad>();
        var newSfuOutboundRtpPadModels = new LinkedList<Models.SfuOutboundRtpPad>();
        var newSfuSctpStreamModels = new LinkedList<Models.SfuSctpStream>();

        for (var observedSfuSample : observedSfuSamples) {
            var sfuSample = observedSfuSample.getSfuSample();
            var timestamp = sfuSample.timestamp;
            var marker = sfuSample.marker;
            var sfu = sfus.get(sfuSample.sfuId);
            if (sfu == null) {
                sfu = this.sfusRepository.add(
                        observedSfuSample.getServiceId(),
                        observedSfuSample.getMediaUnitId(),
                        sfuSample.sfuId,
                        sfuSample.timestamp,
                        observedSfuSample.getTimeZoneId(),
                        marker
                );
                sfus.put(sfu.getSfuId(), sfu);
                newSfuModels.add(sfu.getModel());
            }

            Sfu finalSfu = sfu;
            BiFunction<String, Boolean, SfuTransport> getOrCreateTransport = (sfuTransportId, internal) -> {
                if (sfuTransportId == null) {
                    return null;
                }
                var result = finalSfu.getSfuTransport(sfuTransportId);
                if (result == null) {
                    result = finalSfu.addSfuTransport(
                            sfuTransportId,
                            internal,
                            timestamp,
                            marker
                    );
                    sfuTransports.put(result.getSfuTransportId(), result);
                    newSfuTransportModels.add(result.getModel());
                } else {
                    result.touch(timestamp);
                }
                return result;
            };
            SfuSampleVisitor.streamTransports(sfuSample).forEach(sfuTransport -> {
                getOrCreateTransport.apply(sfuTransport.transportId, Boolean.TRUE.equals(sfuTransport.internal));
            });
            SfuSampleVisitor.streamInboundRtpPads(sfuSample).forEach(sfuInboundRtpPad -> {
                var sfuTransport = getOrCreateTransport.apply(sfuInboundRtpPad.transportId, Boolean.TRUE.equals(sfuInboundRtpPad.internal));
                var sfuInboundRtpPadObject = sfuInboundRtpPads.get(sfuInboundRtpPad.padId);
                if (sfuInboundRtpPadObject == null) {
                    sfuInboundRtpPadObject = sfuTransport.addInboundRtpPad(
                            sfuInboundRtpPad.padId,
                            sfuInboundRtpPad.ssrc,
                            sfuInboundRtpPad.streamId,
                            timestamp,
                            marker
                    );
                    sfuInboundRtpPads.put(sfuInboundRtpPadObject.getRtpPadId(), sfuInboundRtpPadObject);
                    newSfuInboundRtpPadModels.add(sfuInboundRtpPadObject.getModel());
                } else {
                    sfuInboundRtpPadObject.touch(timestamp);
                }
            });
            SfuSampleVisitor.streamOutboundRtpPads(sfuSample).forEach(sfuOutboundRtpPad -> {
                var sfuTransport = getOrCreateTransport.apply(sfuOutboundRtpPad.transportId, Boolean.TRUE.equals(sfuOutboundRtpPad.internal));
                var sfuOutboundRtpPadObject = sfuOutboundRtpPads.get(sfuOutboundRtpPad.padId);
                if (sfuOutboundRtpPadObject == null) {
                    sfuOutboundRtpPadObject = sfuTransport.addOutboundRtpPad(
                            sfuOutboundRtpPad.padId,
                            sfuOutboundRtpPad.ssrc,
                            sfuOutboundRtpPad.streamId,
                            sfuOutboundRtpPad.sinkId,
                            timestamp,
                            marker
                    );
                    sfuOutboundRtpPads.put(sfuOutboundRtpPadObject.getRtpPadId(), sfuOutboundRtpPadObject);
                    newSfuOutboundRtpPadModels.add(sfuOutboundRtpPadObject.getModel());
                } else {
                    sfuOutboundRtpPadObject.touch(timestamp);
                }
            });
            SfuSampleVisitor.streamSctpStreams(sfuSample).forEach(sfuSctpStreamSample -> {
                var sfuTransport = getOrCreateTransport.apply(sfuSctpStreamSample.transportId, Boolean.TRUE.equals(sfuSctpStreamSample.internal));
                var sfuSctpStream = sfuTransport.getSctpStream(sfuSctpStreamSample.streamId);
                if (sfuSctpStream == null) {
                    sfuSctpStream = sfuTransport.addSctpStream(
                            sfuSctpStreamSample.streamId,
                            timestamp,
                            marker
                    );
                    newSfuSctpStreamModels.add(sfuSctpStream.getModel());
                    sfuSctpStreams.put(sfuSctpStream.getSfuSctpStreamId(), sfuSctpStream);
                } else {
                    sfuSctpStream.touch(timestamp);
                }
            });
        }
        this.sfusRepository.save();
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
        if (0 < newSfuSctpStreamModels.size()) {
            this.sfuSctpStreamAddedReports.accept(newSfuSctpStreamModels);
        }
        if (0 < observedSfuSamples.size()) {
            synchronized (this) {
                this.output.onNext(observedSfuSamples);
            }
        }
    }

    private Map<String, Sfu> fetchExistingSfus(ObservedSfuSamples samples) {
        var result = new HashMap<String, Sfu>();
        var existing = this.sfusRepository.getAll(samples.getSfuIds());
        if (existing != null && 0 < existing.size()) {
            result.putAll(existing);
        }
        return result;
    }

    private Map<String, SfuTransport> fetchExistingSfuTransports(ObservedSfuSamples samples) {
        var result = new HashMap<String, SfuTransport>();
        var existing = this.sfuTransportsRepository.getAll(samples.getTransportIds());
        if (existing != null && 0 < existing.size()) {
            result.putAll(existing);
        }
        return result;
    }

    private Map<String, SfuInboundRtpPad> fetchExistingInboundRtpPads(ObservedSfuSamples samples) {
        var result = new HashMap<String, SfuInboundRtpPad>();
        var existing = this.sfuInboundRtpPadsRepository.getAll(samples.getInboundRtpPadIds());
        if (existing != null && 0 < existing.size()) {
            result.putAll(existing);
        }
        return result;
    }

    private Map<String, SfuOutboundRtpPad> fetchExistingOutboundRtpPads(ObservedSfuSamples samples) {
        var result = new HashMap<String, SfuOutboundRtpPad>();
        var existing = this.sfuOutboundRtpPadsRepository.getAll(samples.getOutboundRtpPadIds());
        if (existing != null && 0 < existing.size()) {
            result.putAll(existing);
        }
        return result;
    }

    private Map<String, SfuSctpStream> fetchExistingSfuSctpStreams(ObservedSfuSamples samples) {
        var result = new HashMap<String, SfuSctpStream>();
        var existing = this.sfuSctpStreamsRepository.getAll(samples.getSctpStreamIds());
        if (existing != null && 0 < existing.size()) {
            result.putAll(existing);
        }
        return result;
    }
}
