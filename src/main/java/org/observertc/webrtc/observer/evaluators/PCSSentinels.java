package org.observertc.webrtc.observer.evaluators;

import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.binder.BaseUnits;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.subjects.PublishSubject;
import io.reactivex.rxjava3.subjects.Subject;
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
import java.util.concurrent.atomic.AtomicInteger;
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
            String sentinelName = signaledPC.sentinelName;
            signaledPC.touched = now;
            if (Objects.isNull(sentinelName)) {
                continue;
            }
            Measurement measurement = new Measurement();
            this.pcVisitor.accept(measurement, sample.peerConnectionSample);
            this.pcMeasurements.put(sample.peerConnectionUUID, measurement);
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
            if (60 < Duration.between(signaledPC.touched, now).getSeconds()) {
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
            metrics.packetLost.set(measurement.packetLost);
            metrics.streamNums.set(measurement.SSRCs.size());
            measurement.RttInMs.forEach(metrics.RTT::record);
            metrics.userMediaErrors.set(measurement.userMediaErrors);
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
            this.signaledPCs.put(pcUUID, new SignaledPC(sentinelName));
        }
    }

    private PeerConnectionSampleVisitor<Measurement> makeVisitor() {
        BiConsumer<Measurement, Long> streamUpdater = (measurement, SSRC) -> {
            if (Objects.isNull(SSRC)) {
                return;
            }
            measurement.SSRCs.add(SSRC);
        };
        return new AbstractPeerConnectionSampleVisitor<Measurement>() {

            @Override
            public void visitRemoteInboundRTP(Measurement measurement, PeerConnectionSample sample, PeerConnectionSample.RemoteInboundRTPStreamStats subject) {
                Double RTTInS = subject.roundTripTime;
                if (Objects.nonNull(RTTInS)) {
                    measurement.RttInMs.add(RTTInS * 1000.);
                }
                streamUpdater.accept(measurement, subject.ssrc);
                if (Objects.nonNull(subject.packetsLost)) {
                    measurement.packetLost += subject.packetsLost;
                }
            }

            @Override
            public void visitInboundRTP(Measurement measurement, PeerConnectionSample sample, PeerConnectionSample.InboundRTPStreamStats subject) {
                streamUpdater.accept(measurement, subject.ssrc);
                if (Objects.nonNull(subject.packetsLost)) {
                    measurement.packetLost += subject.packetsLost;
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
        Set<Long> SSRCs = new HashSet<>();
        int packetLost = 0;
        int userMediaErrors = 0;
        List<Double> RttInMs = new LinkedList<>();

        void eat(Measurement other) {
            this.SSRCs.addAll(other.SSRCs);
            this.packetLost += other.packetLost;
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
        AtomicInteger packetLost;
        AtomicInteger streamNums;
        AtomicInteger userMediaErrors;


        public Metrics(String sentinelName) {
            this.RTT = DistributionSummary.builder("rtt_stats")
                    .baseUnit(BaseUnits.MILLISECONDS)
                    .tag("sentinel", sentinelName)
                    .register(meterRegistry);

            this.packetLost = meterRegistry.gauge("packetLost_stats", List.of(Tag.of("sentinel", sentinelName)), new AtomicInteger(0));
            this.streamNums = meterRegistry.gauge("streamNums_stats", List.of(Tag.of("sentinel", sentinelName)), new AtomicInteger(0));
            this.userMediaErrors = meterRegistry.gauge("streamNums_stats", List.of(Tag.of("sentinel", sentinelName)), new AtomicInteger(0));
        }
    }
}
