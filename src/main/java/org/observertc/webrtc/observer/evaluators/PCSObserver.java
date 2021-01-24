package org.observertc.webrtc.observer.evaluators;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.subjects.PublishSubject;
import io.reactivex.rxjava3.subjects.Subject;
import org.observertc.webrtc.observer.ObserverConfig;
import org.observertc.webrtc.observer.dto.AbstractPeerConnectionSampleVisitor;
import org.observertc.webrtc.observer.dto.PeerConnectionSampleVisitor;
import org.observertc.webrtc.observer.dto.v20200114.PeerConnectionSample;
import org.observertc.webrtc.observer.samples.ObservedPCS;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

@Singleton
public class PCSObserver implements Consumer<List<ObservedPCS>> {

    private final Subject<Map<UUID, PCState>> expiredPCsSubject = PublishSubject.create();
    private final Subject<Map<UUID, PCState>> activePCsSubject = PublishSubject.create();
    private final Map<UUID, PCState> pcStates = new HashMap<>();
    private final PeerConnectionSampleVisitor<PCState> SSRCExtractor;

    @Inject
    ObserverConfig.EvaluatorsConfig config;

    @PostConstruct
    void setup() {

    }

    public Observable<Map<UUID, PCState>> getObservableExpiredPCs() {
        return this.expiredPCsSubject;
    }

    public Observable<Map<UUID, PCState>> getObservableActivePCs() {
        return this.activePCsSubject;
    }

    public PCSObserver() {
        this.SSRCExtractor = this.makeSSRCExtractor();
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

            this.SSRCExtractor.accept(pcState, observedPCS.peerConnectionSample);
            if (pcState.SSRCs.size() < 1 && Objects.isNull(pcState.callName)) {
                pcState.callName = this.config.impairablePCsCallName;
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
        return new AbstractPeerConnectionSampleVisitor<PCState>() {
            @Override
            public void visitRemoteInboundRTP(PCState pcState, PeerConnectionSample sample, PeerConnectionSample.RemoteInboundRTPStreamStats subject) {
                pcState.SSRCs.add(subject.ssrc);
            }

            @Override
            public void visitInboundRTP(PCState pcState, PeerConnectionSample sample, PeerConnectionSample.InboundRTPStreamStats subject) {
                pcState.SSRCs.add(subject.ssrc);
            }

            @Override
            public void visitOutboundRTP(PCState pcState, PeerConnectionSample sample, PeerConnectionSample.OutboundRTPStreamStats subject) {
                pcState.SSRCs.add(subject.ssrc);
            }
        };
    }
}
