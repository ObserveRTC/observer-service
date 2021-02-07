package org.observertc.webrtc.observer.evaluators;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.subjects.PublishSubject;
import io.reactivex.rxjava3.subjects.Subject;
import org.observertc.webrtc.observer.ObserverConfig;
import org.observertc.webrtc.observer.common.IPAddressConverterProvider;
import org.observertc.webrtc.observer.dto.AbstractPeerConnectionSampleVisitor;
import org.observertc.webrtc.observer.dto.PeerConnectionSampleVisitor;
import org.observertc.webrtc.observer.dto.v20200114.PeerConnectionSample;
import org.observertc.webrtc.observer.monitors.ObserverMetrics;
import org.observertc.webrtc.observer.repositories.resolvers.SentinelAddressesResolver;
import org.observertc.webrtc.observer.samples.ObservedPCS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;

@Singleton
public class PCSObserver implements Consumer<List<ObservedPCS>> {
    private static final Logger logger = LoggerFactory.getLogger(PCSObserver.class);
    private final Subject<Map<UUID, PCState>> expiredPCsSubject = PublishSubject.create();
    private final Subject<Map<UUID, PCState>> activePCsSubject = PublishSubject.create();
    private final Subject<Map.Entry<UUID, String>> sentinelSignaledPCs = PublishSubject.create();
    private final Map<UUID, PCState> pcStates = new HashMap<>();
    private final PeerConnectionSampleVisitor<PCState> pcStateProcessor;
    private final Function<String, String> ipAddressConverter;
    @Inject
    ObserverMetrics observerMetrics;

    @Inject
    ObserverConfig.EvaluatorsConfig config;

    @Inject
    SentinelAddressesResolver sentinelAddressesResolver;

    @PostConstruct
    void setup() {

    }

    public Observable<Map<UUID, PCState>> getObservableExpiredPCs() {
        return this.expiredPCsSubject;
    }

    public Observable<Map<UUID, PCState>> getObservableActivePCs() {
        return this.activePCsSubject;
    }

    public Observable<Map.Entry<UUID, String>> getObservableSentinelSignals() {
        return this.sentinelSignaledPCs;
    }

    public PCSObserver(IPAddressConverterProvider ipAddressConverterProvider) {
        this.ipAddressConverter = ipAddressConverterProvider.provide();
        this.pcStateProcessor = this.makeSSRCExtractor();
    }

    @Override
    public void accept(List<ObservedPCS> observedPCSamples) throws Throwable {
        Map<UUID, PCState> activePCs = new HashMap<>();
        Instant now = Instant.now();
        for (ObservedPCS observedPCS : observedPCSamples) {
            if (Objects.isNull(observedPCS.peerConnectionSample)) {
                continue;
            }
            PCState pcState = this.pcStates.get(observedPCS.peerConnectionUUID);
            if (Objects.isNull(pcState)) {
                pcState = this.makePCState(observedPCS);
                if (Objects.isNull(pcState)) {
                    continue;
                }
                this.pcStates.put(observedPCS.peerConnectionUUID, pcState);
            }
            pcState.updated = observedPCS.timestamp;
            pcState.touched = now;

            this.pcStateProcessor.accept(pcState, observedPCS.peerConnectionSample);
            if (pcState.SSRCs.size() < 1 && Objects.isNull(pcState.callName)) {
                pcState.callName = this.config.impairablePCsCallName;
                this.observerMetrics.incrementImpairedPCs(pcState.serviceName, pcState.mediaUnitID);
            }
            activePCs.put(observedPCS.peerConnectionUUID, pcState);
        }

        Map<UUID, PCState> expiredPCs = new HashMap<>();
        Iterator<Map.Entry<UUID, PCState>> it = this.pcStates.entrySet().iterator();
        while(it.hasNext()) {
            Map.Entry<UUID, PCState> entry = it.next();
            UUID pcUUID = entry.getKey();
            if (activePCs.containsKey(pcUUID)) {
                continue;
            }
            PCState pcState = entry.getValue();
            if (Duration.between(pcState.touched, now).getSeconds() < config.peerConnectionMaxIdleTimeInS) {
                continue;
            }
            expiredPCs.put(pcUUID, pcState);
            it.remove();
        }

        if (0 < activePCs.size()) {
            this.activePCsSubject.onNext(activePCs);
        }

        if (0 < expiredPCs.size()) {
            this.expiredPCsSubject.onNext(expiredPCs);
        }
    }


    private PCState makePCState(ObservedPCS observedPCS) {
        PeerConnectionSample pcSample = observedPCS.peerConnectionSample;
        if (Objects.isNull(pcSample)) {
            return null;
        }
        PCState pcState = PCState.of(
                observedPCS.serviceUUID,
                observedPCS.peerConnectionUUID,
                observedPCS.timestamp,
                pcSample.browserId,
                pcSample.callId,
                observedPCS.timeZoneID,
                pcSample.userId,
                observedPCS.mediaUnitId,
                observedPCS.serviceName,
                observedPCS.marker
        );
        return pcState;
    }

    private PeerConnectionSampleVisitor<PCState> makeSSRCExtractor() {
        BiConsumer<PCState, Long> ssrcConverter = (pcState, SSRC) -> {
            if (Objects.isNull(SSRC)) {
                return;
            }

//            if (SSRC < 16) { // because of TokBox uses ssrc 1,2 for probing purpose
//                SSRC = (pcState.peerConnectionUUID.getMostSignificantBits() & 0xFFFFFFFE) + SSRC;
//            } else if( 10000 < SSRC && SSRC < 10004 ) {
//                SSRC = (pcState.peerConnectionUUID.getMostSignificantBits() & 0xFFF00000) + SSRC;
//            }
            if (SSRC < 3 || (10000 <= SSRC && SSRC < 10004)) { // because of TokBox uses fixed SSRC numbers for testing
                return;
            }
            pcState.SSRCs.add(SSRC);
        };

        return new AbstractPeerConnectionSampleVisitor<PCState>() {
            @Override
            public void visitRemoteInboundRTP(PCState pcState, PeerConnectionSample sample, PeerConnectionSample.RemoteInboundRTPStreamStats subject) {
                ssrcConverter.accept(pcState, subject.ssrc);
            }

            @Override
            public void visitInboundRTP(PCState pcState, PeerConnectionSample sample, PeerConnectionSample.InboundRTPStreamStats subject) {
                ssrcConverter.accept(pcState, subject.ssrc);
            }

            @Override
            public void visitOutboundRTP(PCState pcState, PeerConnectionSample sample, PeerConnectionSample.OutboundRTPStreamStats subject) {
                ssrcConverter.accept(pcState, subject.ssrc);
            }

            @Override
            public void visitICELocalCandidate(PCState pcState, PeerConnectionSample sample, PeerConnectionSample.ICELocalCandidate subject) {
                if (Objects.nonNull(subject.ip)) {
                    Optional<String> sentinelHolder = sentinelAddressesResolver.apply(subject.ip);
                    if (sentinelHolder.isPresent()) {
                        sentinelSignaledPCs.onNext(Map.entry(pcState.peerConnectionUUID, sentinelHolder.get()));
                    }
                }
            }

            @Override
            public void visitICERemoteCandidate(PCState pcState, PeerConnectionSample sample, PeerConnectionSample.ICERemoteCandidate subject) {
                if (Objects.nonNull(subject.ip)) {
                    Optional<String> sentinelHolder = sentinelAddressesResolver.apply(subject.ip);
                    if (sentinelHolder.isPresent()) {
                        sentinelSignaledPCs.onNext(Map.entry(pcState.peerConnectionUUID, sentinelHolder.get()));
                    }
                }
            }
        };
    }
}
