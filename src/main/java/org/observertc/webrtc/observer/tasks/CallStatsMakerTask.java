package org.observertc.webrtc.observer.tasks;

import io.micronaut.context.annotation.Prototype;
import org.observertc.webrtc.observer.models.CallEntity;
import org.observertc.webrtc.observer.models.ICEConnectionEntity;
import org.observertc.webrtc.observer.models.PeerConnectionEntity;
import org.observertc.webrtc.observer.monitorstats.CallStats;
import org.observertc.webrtc.observer.repositories.hazelcast.*;
import org.observertc.webrtc.schemas.reports.CandidateType;
import org.observertc.webrtc.schemas.reports.ICEState;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Note: that it collects stats for only locally owned media units and other things
 */
@Prototype
public class CallStatsMakerTask extends TaskAbstract<CallStats> {
    private enum State {
        CREATED,
        COLLECT,
        ANALYZE,
        EXECUTED,
        ROLLEDBACK
    }

    @Inject
    PeerConnectionICEConnectionsRepository peerConnectionICEConnectionsRepository;

    @Inject
    PeerConnectionsRepository peerConnectionsRepository;

    @Inject
    ICEConnectionsRepository iceConnectionsRepository;

    @Inject
    CallSynchronizationSourcesRepository callSynchronizationSourcesRepository;

    @Inject
    CallPeerConnectionsRepository callPeerConnectionsRepository;

    @Inject
    CallEntitiesRepository callEntitiesRepository;

    private final CallStats result = new CallStats();
    private State state = State.CREATED;

    private Map<UUID, Collection<CallEntity>> locallyStoredCalls;
    private Map<UUID, Collection<UUID>> locallyStoredCallsPcKeys;
    private Map<UUID, PeerConnectionEntity> peerConnections;
    private Map<String, ICEConnectionEntity> iceConnections;
    private Map<UUID, Collection<String>> peerIceConnectionKeys;
    private Map<UUID, Integer> callStreamsNum;
    @PostConstruct
    void setup() {

    }

    @Override
    protected CallStats perform() throws Throwable {
        switch (this.state) {
            default:
            case CREATED:
                this.state = State.COLLECT;
            case COLLECT:
                this.collect();
                this.state = State.ANALYZE;
            case ANALYZE:
                this.analyze();
                this.state = State.EXECUTED;
            case EXECUTED:
            case ROLLEDBACK:
        }
        return this.result;
    }

    @Override
    protected void rollback(Throwable t) {
        // no need for that
        switch (this.state) {
            default:
            case CREATED:
            case COLLECT:
            case ANALYZE:
            case EXECUTED:
                this.state = State.ROLLEDBACK;
            case ROLLEDBACK:
        }
    }

    /**
     * The key here is that we collect calls stored locally.
     * If every instance gather infromation of the calls which is owned and stored
     * locally, than, and only than, we will have an additive result for tha calls from every instance
     */
    private void collect() {

        this.locallyStoredCalls = this.callEntitiesRepository.getLocalEntries();
        Set<UUID> callUUIDs = this.locallyStoredCalls.keySet();
        this.locallyStoredCallsPcKeys = this.callPeerConnectionsRepository.findAll(locallyStoredCalls.keySet());
        Set<UUID> pcUUIDs = locallyStoredCallsPcKeys.values().stream().flatMap(Collection::stream).collect(Collectors.toSet());
        this.peerConnections = this.peerConnectionsRepository.findAll(pcUUIDs);
        this.peerIceConnectionKeys = this.peerConnectionICEConnectionsRepository.findAll(pcUUIDs);
        Set<String> iceKeys = peerIceConnectionKeys.values().stream().flatMap(Collection::stream).collect(Collectors.toSet());
        this.iceConnections = this.iceConnectionsRepository.findAll(iceKeys);
        this.callStreamsNum = this.callSynchronizationSourcesRepository.findAll(callUUIDs)
                .entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> e.getValue().size()
                ));

    }

    private List<ICEConnectionEntity> getICEConnections(UUID pcUUID) {
        Collection<String> iceKeys = this.peerIceConnectionKeys.get(pcUUID);
        List<ICEConnectionEntity> result = iceKeys.stream().map(this.iceConnections::get).collect(Collectors.toList());
        return result;
    }

    private void analyze() {
        if (this.locallyStoredCalls.size() < 1) {
            return;
        }
        for (Collection<CallEntity> callEntities : this.locallyStoredCalls.values()) {
            for (CallEntity callEntity : callEntities) {
                this.analyze(callEntity);
            }
        }
    }

    private void analyze(CallEntity callEntity) {
        Collection<UUID> pcUUIDs = this.locallyStoredCallsPcKeys.get(callEntity.callUUID);
        List<PeerConnectionEntity> peerConnectionEntities = pcUUIDs.stream()
                .map(this.peerConnections::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        Set<String> mediaUnits = new HashSet<>();
        List<ConnectionType> connectionTypes = new LinkedList<>();
        for (PeerConnectionEntity peerConnectionEntity : peerConnectionEntities) {
            mediaUnits.add(peerConnectionEntity.mediaUnitId);
            List<ICEConnectionEntity> iceConnections = this.getICEConnections(peerConnectionEntity.peerConnectionUUID);
            ConnectionType connectionType = this.getConnectionType(iceConnections);
            connectionTypes.add(connectionType);
        }
        if (connectionTypes.stream().anyMatch(type -> type.equals(ConnectionType.UNKNOWN))) {
            ++this.result.unknownTypeOfCalls;
            this.result.unknownTypeOfStreams += this.callStreamsNum.get(callEntity.callUUID);
            return;
        }

        if (connectionTypes.stream().allMatch(type -> type.equals(ConnectionType.PEER_TO_PEER))) {
            ++this.result.p2pCalls;
            this.result.p2pStreams += this.callStreamsNum.get(callEntity.callUUID);
            return;
        }

        boolean callIsOwnedByTheMediaUnit = mediaUnits.size() == 1;
        for (String mediaUnitId : mediaUnits) {
            CallStats.MediaUnitStats mediaUnitStats = this.result.mediaUnitStats.get(mediaUnitId);
            if (Objects.isNull(mediaUnitStats)) {
                mediaUnitStats = new CallStats.MediaUnitStats();
                this.result.mediaUnitStats.put(mediaUnitId, mediaUnitStats);
            }
            ++mediaUnitStats.totalCalls;
            if (callIsOwnedByTheMediaUnit) {
                ++mediaUnitStats.ownedCalls;
            } else {
                ++mediaUnitStats.sharedCalls;
            }
            mediaUnitStats.concurrentStreams += this.callStreamsNum.get(callEntity.callUUID);
        }
    }

    private enum ConnectionType {
        PEER_TO_PEER,
        MEDIAUNIT_OWNED,
        UNKNOWN
    }
    private ConnectionType getConnectionType(@NotNull Collection<ICEConnectionEntity> iceConnectionEntities) {
        boolean p2p = true;
        boolean checked = false;
        for (ICEConnectionEntity iceConnectionEntity : iceConnectionEntities) {
           if (!iceConnectionEntity.nominated) {
               continue;
           }
           if (Objects.isNull(iceConnectionEntity.state)) {
               continue;
           }
           if (Objects.isNull(iceConnectionEntity.localCandidateType)) {
               continue;
           }
           if (Objects.isNull(iceConnectionEntity.remoteCandidateType)) {
               continue;
           }
           if (!iceConnectionEntity.state.equals(ICEState.SUCCEEDED)) {
               continue;
           }
           if (!iceConnectionEntity.localCandidateType.equals(CandidateType.HOST)) {
               p2p = false;
           }
           if (!iceConnectionEntity.localCandidateType.equals(CandidateType.HOST)) {
               p2p = false;
           }
           checked = true;
        }
        if (!checked) {
            return ConnectionType.UNKNOWN;
        }
        if (p2p) {
            return ConnectionType.PEER_TO_PEER;
        }
        return ConnectionType.MEDIAUNIT_OWNED;
    }

}
