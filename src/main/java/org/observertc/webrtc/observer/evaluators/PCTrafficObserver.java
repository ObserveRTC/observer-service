package org.observertc.webrtc.observer.evaluators;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.subjects.PublishSubject;
import io.reactivex.rxjava3.subjects.Subject;
import org.observertc.webrtc.observer.dto.v20200114.PeerConnectionSample;
import org.observertc.webrtc.observer.models.PCTrafficType;
import org.observertc.webrtc.observer.samples.ObservedPCS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Singleton;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

@Singleton
public class PCTrafficObserver implements Consumer<List<ObservedPCS>> {

    private static final Logger logger = LoggerFactory.getLogger(PCTrafficObserver.class);

    private final Map<UUID, ICEConnections> connections = new HashMap<>();
    private final Subject<PCTrafficState> pcConnectionStateSubject = PublishSubject.create();

    public PCTrafficObserver() {

    }

    @PostConstruct
    void setup() {

    }

    @PreDestroy
    void teardown() {

    }

    public Observable<PCTrafficState> observablePCConnectionStates() {
        return this.pcConnectionStateSubject;
    }

    @Override
    public void accept(List<ObservedPCS> samples) throws Throwable {
        Map<UUID, List<ConnectionTypes>> journal = new HashMap<>();
        for (ObservedPCS sample : samples) {
            try {
                this.processSample(sample, journal);
            } catch (Throwable t) {
                logger.error("Unexpected error occurred in execution", t);
            }
        }
        if (0 < journal.size()) {
            this.processJournal(journal);
        }
    }

    public void processSample(ObservedPCS sample, Map<UUID, List<ConnectionTypes>> journal) {
        if (Objects.isNull(sample) ||
            Objects.isNull(sample.peerConnectionSample) ||
                Objects.isNull(sample.peerConnectionSample.iceStats) ||
                Objects.isNull(sample.peerConnectionSample.iceStats.localCandidates) ||
                Objects.isNull(sample.peerConnectionSample.iceStats.remoteCandidates) ||
                Objects.isNull(sample.peerConnectionSample.iceStats.candidatePairs))
        {
            return;
        }
        ICEConnections iceConnections = this.connections.get(sample.peerConnectionUUID);
        Instant now = Instant.now();
        if (Objects.isNull(iceConnections)) {
            iceConnections = new ICEConnections();
            this.connections.put(sample.peerConnectionUUID, iceConnections);
        } else if (Duration.between(iceConnections.updated, now).getSeconds() < 30) {
            return;
        }

        PeerConnectionSample.ICEStats iceStats = sample.peerConnectionSample.iceStats;
        Map<String, PeerConnectionSample.ICELocalCandidate> localCandidates = new HashMap<>();
        Map<String, PeerConnectionSample.ICERemoteCandidate> remoteCandidates = new HashMap<>();
        Map<String, PeerConnectionSample.ICECandidatePair> candidatePairs = new HashMap<>();
        for (int i = 0; i < iceStats.localCandidates.length; ++i) {
            PeerConnectionSample.ICELocalCandidate localCandidate = iceStats.localCandidates[i];
            localCandidates.put(localCandidate.id, localCandidate);
        }
        for (int i = 0; i < iceStats.remoteCandidates.length; ++i) {
            PeerConnectionSample.ICERemoteCandidate remoteCandidate = iceStats.remoteCandidates[i];
            remoteCandidates.put(remoteCandidate.id, remoteCandidate);
        }
        for (int i = 0; i < iceStats.candidatePairs.length; ++i) {
            PeerConnectionSample.ICECandidatePair candidatePair = iceStats.candidatePairs[i];
            candidatePairs.put(candidatePair.id, candidatePair);
        }
        Iterator<PeerConnectionSample.ICECandidatePair> it = candidatePairs.values().iterator();
        while (it.hasNext()) {
            PeerConnectionSample.ICECandidatePair candidatePair = it.next();
            PeerConnectionSample.ICELocalCandidate localCandidate = localCandidates.get(candidatePair.localCandidateId);
            PeerConnectionSample.ICERemoteCandidate remoteCandidate = remoteCandidates.get(candidatePair.remoteCandidateId);
            if (Objects.isNull(localCandidate) ||
                Objects.isNull(remoteCandidate) ||
                Objects.isNull(candidatePair.state) ||
                !candidatePair.nominated
            )
            {
                continue;
            }
            if (!candidatePair.state.equals(PeerConnectionSample.ICEState.SUCCEEDED)) {
                continue;
            }
            ConnectionTypes oldConnectionType = iceConnections.connectionTypes.get(candidatePair.id);
            ConnectionTypes newConnectionType = ConnectionTypes.UNKNOWN;
            if (Objects.nonNull(localCandidate.candidateType) &&
                Objects.nonNull(remoteCandidate.candidateType)) {
                if (localCandidate.candidateType.equals(PeerConnectionSample.CandidateType.RELAY) ||
                    remoteCandidate.candidateType.equals(PeerConnectionSample.CandidateType.RELAY)) {
                    newConnectionType = ConnectionTypes.RELAYED;
                    logger.info("RELAYED connection is detected: localCandidate: {}, remoteCandidate: {}", localCandidate, remoteCandidate);
                } else {
                    newConnectionType = ConnectionTypes.PEER_TO_PEER;
                }
            }

            // Detect changes, and add it to a journal for further processing
            if (Objects.isNull(oldConnectionType) || !oldConnectionType.equals(newConnectionType)) {
                if (!newConnectionType.equals(ConnectionTypes.UNKNOWN)) {
                    iceConnections.connectionTypes.put(candidatePair.id, newConnectionType);
                    List<ConnectionTypes> pcJournal = journal.get(sample.peerConnectionUUID);
                    if (Objects.isNull(pcJournal)) {
                        pcJournal = new LinkedList<>();
                        journal.put(sample.peerConnectionUUID, pcJournal);
                    }
                    pcJournal.add(newConnectionType);
                } else {
                    logger.warn("Cannot determine the connection type for the following configuration: LocalCandidate: {}, RemoteCanddiate: {} CandidatePair: {}", localCandidate, remoteCandidate, candidatePair);
                }
                iceConnections.connectionTypes.put(candidatePair.id, newConnectionType);
            }
        }
    }

    private void processJournal(Map<UUID, List<ConnectionTypes>> journal) {
        Iterator<Map.Entry<UUID, List<ConnectionTypes>>> it = journal.entrySet().iterator();
        while(it.hasNext()) {
            Map.Entry<UUID, List<ConnectionTypes>> entry = it.next();
            UUID peerConnectionUUID = entry.getKey();
            List<ConnectionTypes> connectionTypes = entry.getValue();
            Iterator<ConnectionTypes> jt = connectionTypes.listIterator();
            int state = 0;
            while(jt.hasNext()) {
                ConnectionTypes connectionType = jt.next();
                switch (connectionType) {
                    case PEER_TO_PEER:
                        state |= 1;
                        break;
                    case RELAYED:
                        state |= 2;
                        break;
                    case UNKNOWN:
                    default:
                        break;
                }
            }
            PCTrafficType trafficType = PCTrafficType.UNKNOWN;
            switch (state) {
                case 1:
                    trafficType = PCTrafficType.PEER_TO_PEER;
                    break;
                case 2:
                    trafficType = PCTrafficType.RELAYED;
                    break;
                case 3:
                    trafficType = PCTrafficType.MIXED;
                    break;
                case 0: // no changes, or all are unknown
                default:
                    break;
            }
            PCTrafficState pcTrafficState = PCTrafficState.of(peerConnectionUUID, trafficType);
            this.pcConnectionStateSubject.onNext(pcTrafficState);
        }
    }

    private enum ConnectionTypes {
        PEER_TO_PEER,
        RELAYED,
        UNKNOWN
        ;

        String target;
    }

    private class ICEConnections {
        public UUID peerConnectionUUID;
        public Map<String, ConnectionTypes> connectionTypes = new HashMap<>();
        public final Instant created = Instant.now();
        public Instant updated = Instant.now();

        @Override
        public int hashCode() {
            if (Objects.isNull(this.peerConnectionUUID)) {
                return 1;
            }
            return peerConnectionUUID.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (Objects.isNull(obj)) return false;
            if (!getClass().equals(obj.getClass())) return false;

            ICEConnections other = (ICEConnections) obj;
            return this.peerConnectionUUID.equals(other.peerConnectionUUID);
        }
    }


}
