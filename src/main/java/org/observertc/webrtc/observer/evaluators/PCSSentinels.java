package org.observertc.webrtc.observer.evaluators;

import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.binder.BaseUnits;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.subjects.PublishSubject;
import io.reactivex.rxjava3.subjects.Subject;
import org.observertc.webrtc.observer.common.ObjectToString;
import org.observertc.webrtc.observer.dto.AbstractPeerConnectionSampleVisitor;
import org.observertc.webrtc.observer.dto.PeerConnectionSampleVisitor;
import org.observertc.webrtc.observer.dto.v20200114.PeerConnectionSample;
import org.observertc.webrtc.observer.samples.ObservedPCS;
import org.observertc.webrtc.observer.tasks.TasksProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.BiConsumer;

@Singleton
public class PCSSentinels implements Consumer<List<ObservedPCS>> {
    private static final Logger logger = LoggerFactory.getLogger(PCSSentinels.class);

    private Subject<Map.Entry<UUID, String>> signaledPCsInput = PublishSubject.create();

    @Inject
    MeterRegistry meterRegistry;

    @Inject
    TasksProvider tasksProvider;

    private volatile boolean signaled = false;
    private Queue<Map.Entry<UUID, String>> messages = new ConcurrentLinkedQueue<>();
    private Map<UUID, SignaledPC> signaledPCs;
    private Map<UUID, Measurement> pcMeasurements = new HashMap<>();
    private Map<String, Metrics> metrics = new HashMap<>();
    private PeerConnectionSampleVisitor<Measurement> pcVisitor;

    public PCSSentinels() {
        this.pcVisitor = this.makeVisitor();
    }

    public Observer<? super Map.Entry<UUID, String>> getMessageObserver() {
        return this.signaledPCsInput;
    }

    @PostConstruct
    void setup() {
        this.signaledPCsInput.subscribe(entry -> {
            messages.add(entry);
            signaled = true;
        });
        this.signaledPCs = new HashMap<>();
    }

    @PreDestroy
    void teardown() {

    }

    @Override
    public void accept(List<ObservedPCS> samples) throws Throwable {
        if (this.signaled) {
            this.consumeMessages();
            this.signaled = false;
        }
        Instant now = Instant.now();
        HashSet<UUID> checkedPCs = new HashSet<>();
        for (ObservedPCS sample : samples) {
            if (checkedPCs.contains(sample.peerConnectionUUID)) {
                continue;
            }
            checkedPCs.add(sample.peerConnectionUUID);
            SignaledPC signaledPC = this.signaledPCs.get(sample.peerConnectionUUID);
            if (Objects.isNull(signaledPC)) {
                continue;
            }
            String sentinelName = signaledPC.sentinelName;
            signaledPC.touched = now;
            if (Objects.isNull(sentinelName)) {
                continue;
            }
            Measurement measurement = new Measurement();
            this.pcVisitor.accept(measurement, sample.peerConnectionSample);
            this.pcMeasurements.put(sample.peerConnectionUUID, measurement);
            if (measurement.log) {
                logger.info("Measurement logging request. sentinel: {}, sample: {}", sentinelName, ObjectToString.toString(sample));
            }
        }
        Map<String, Measurement> aggregatedMeasurements = this.aggregate(now);
        this.update(aggregatedMeasurements);
    }

    private Map<String, Measurement> aggregate(Instant now) {
        Iterator<Map.Entry<UUID, SignaledPC>> it = this.signaledPCs.entrySet().iterator();
        Map<String, Measurement> result = new HashMap<>();
        while(it.hasNext()) {
            Map.Entry<UUID, SignaledPC> entry = it.next();
            UUID pcUUID = entry.getKey();
            SignaledPC signaledPC = entry.getValue();
            if (300 < Duration.between(signaledPC.touched, now).getSeconds()) {
                this.pcMeasurements.remove(pcUUID);
                it.remove();
                continue;
            }
            Measurement measurement = this.pcMeasurements.get(pcUUID);
            if (Objects.isNull(measurement)) {
                continue;
            }
            Measurement aggrMeasurement = result.get(signaledPC.sentinelName);
            if (Objects.isNull(aggrMeasurement)) {
                result.put(signaledPC.sentinelName, measurement);
            } else {
                aggrMeasurement.eat(measurement);
            }
        }
        return result;
    }

    private void update(Map<String, Measurement> aggregatedMeasurements) {
        Iterator<Map.Entry<String, Measurement>> it = aggregatedMeasurements.entrySet().iterator();
        while(it.hasNext()) {
            Map.Entry<String, Measurement> entry = it.next();
            String sentinelName = entry.getKey();
            Measurement measurement = entry.getValue();
            Metrics metrics = this.metrics.get(sentinelName);
            if (Objects.isNull(metrics)) {
                metrics = new Metrics(sentinelName);
                this.metrics.put(sentinelName, metrics);
            }
            meterRegistry.gauge("sentinel_streamNums_stats", List.of(Tag.of("sentinelName", sentinelName)), measurement.streamNums);
            meterRegistry.gauge("sentinel_userMediaErrors_stats", List.of(Tag.of("sentinelName", sentinelName)), measurement.userMediaErrors);
            measurement.fractionalLosts.forEach(metrics.fractionalLost::record);
            measurement.RttInMs.forEach(metrics.RTT::record);
        }
    }

    private void consumeMessages() {
        while (!this.messages.isEmpty()) {
            Map.Entry<UUID, String> entry = this.messages.poll();
            UUID pcUUID = entry.getKey();
            String sentinelName = entry.getValue();
            SignaledPC signaledPC = this.signaledPCs.get(pcUUID);
            if (Objects.nonNull(signaledPC)) {
                continue;
            }
            signaledPC = new SignaledPC(sentinelName);
            this.signaledPCs.put(pcUUID, signaledPC);
        }
    }

    private PeerConnectionSampleVisitor<Measurement> makeVisitor() {
        BiConsumer<Measurement, Long> streamUpdater = (measurement, SSRC) -> {
            if (Objects.isNull(SSRC)) {
                return;
            }
            ++measurement.streamNums;
//            measurement.SSRCs.add(SSRC);
        };
        return new AbstractPeerConnectionSampleVisitor<Measurement>() {

            @Override
            public void visitRemoteInboundRTP(Measurement measurement, PeerConnectionSample sample, PeerConnectionSample.RemoteInboundRTPStreamStats subject) {
                Double RTTInS = subject.roundTripTime;
                if (5. < RTTInS) { // by def. it would mean that 5s RTT, which is impropable. more likely that the RTT is in ms, but let's first log these infos
                    measurement.log = true;
                }
                if (Objects.nonNull(RTTInS)) {
                    measurement.RttInMs.add(RTTInS * 1000.);
                }
                streamUpdater.accept(measurement, subject.ssrc);
            }

            @Override
            public void visitInboundRTP(Measurement measurement, PeerConnectionSample sample, PeerConnectionSample.InboundRTPStreamStats subject) {
                streamUpdater.accept(measurement, subject.ssrc);
                if (Objects.nonNull(subject.packetsLost) && Objects.nonNull(subject.packetsReceived)) {
                    double fractionalLost = ((double) subject.packetsLost) / (((double) subject.packetsReceived) + ((double) subject.packetsLost));
                    measurement.fractionalLosts.add(fractionalLost);
                }
            }

            @Override
            public void visitOutboundRTP(Measurement measurement, PeerConnectionSample sample, PeerConnectionSample.OutboundRTPStreamStats subject) {
                streamUpdater.accept(measurement, subject.ssrc);
            }

            @Override
            public void visitUserMediaError(Measurement measurement, PeerConnectionSample sample, PeerConnectionSample.UserMediaError userMediaError) {
                if (Objects.nonNull(userMediaError.message)) {
                    ++measurement.userMediaErrors;
                }
            }
        };
    }

    private class Measurement {
        int streamNums = 0;
        List<Double> fractionalLosts = new LinkedList<>();
        int userMediaErrors = 0;
        List<Double> RttInMs = new LinkedList<>();
        boolean log = false;

        void eat(Measurement other) {
            this.streamNums += other.streamNums;
            this.fractionalLosts.addAll(other.fractionalLosts);
            this.userMediaErrors += other.userMediaErrors;
            this.RttInMs.addAll(other.RttInMs);
        }
    }

    class SignaledPC {
        final String sentinelName;
        Instant touched = Instant.now();

        SignaledPC(String sentinelName) {
            this.sentinelName = sentinelName;
        }
    }

    class Metrics {
        DistributionSummary RTT;
        DistributionSummary fractionalLost;


        public Metrics(String sentinelName) {
            this.RTT = DistributionSummary.builder("sentinel_rtt_stats")
                    .baseUnit(BaseUnits.MILLISECONDS)
                    .tag("sentinelName", sentinelName)
                    .publishPercentiles(.75, .95)
                    .publishPercentileHistogram()
                    .register(meterRegistry);

            this.fractionalLost = DistributionSummary.builder("sentinel_fractionalLost_stats")
                    .baseUnit(BaseUnits.EVENTS)
                    .tag("sentinelName", sentinelName)
                    .publishPercentiles(.75, .95)
                    .publishPercentileHistogram()
                    .register(meterRegistry);

        }
    }
}
