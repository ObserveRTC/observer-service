package org.observertc.webrtc.observer.repositories.tasks;

import io.micronaut.context.annotation.Prototype;
import org.observertc.webrtc.observer.common.ChainedTask;
import org.observertc.webrtc.observer.common.TaskStage;
import org.observertc.webrtc.observer.dto.CallDTO;
import org.observertc.webrtc.observer.entities.CallEntity;
import org.observertc.webrtc.observer.entities.PeerConnectionEntity;
import org.observertc.webrtc.observer.repositories.HazelcastMaps;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;

@Prototype
public class FetchCallsTask extends ChainedTask<Map<UUID, CallEntity>> {

    private Set<UUID> callUUIDs = new HashSet<>();
    private boolean fetchPeerConnections = true;

    @Inject
    HazelcastMaps hazelcastMaps;

    @Inject
    FetchPCsTask fetchPCsTask;


    @PostConstruct
    void setup() {
        new Builder<>(this)
            .addStage(TaskStage.builder("Find Call By UUID")
                    .<Map<UUID, CallDTO>>withSupplier(() -> {
                        if (callUUIDs.size() < 1) {
                            this.getLogger().info("Call tried to find by provided callUUIDs, but the there is no");
                            return null;
                        }
                        return hazelcastMaps.getCallDTOs().getAll(callUUIDs);
                    })
                    .<Set<UUID>, Map<UUID, CallDTO>>withFunction(passedCallUUIDs -> {
                        if (Objects.isNull(passedCallUUIDs)) {
                            this.getLogger().info("Call tried to find by passed callUUID, but the callUUID is null");
                            return null;
                        }
                        callUUIDs.addAll(passedCallUUIDs);
                        if (callUUIDs.size() < 1) {
                            return null;
                        }
                        return hazelcastMaps.getCallDTOs().getAll(callUUIDs);
                    })
                    .build()
            )
            .<Map<UUID, CallDTO>>addBreakCondition((callDTOMap, resultHolder) -> {
                if (Objects.isNull(callDTOMap) || callDTOMap.size() < 1) {
                    resultHolder.set(Collections.EMPTY_MAP);
                    return true;
                }
                return false;
            })
            .<Map<UUID, CallDTO>, Map<UUID, CallEntity.Builder>> addFunctionalStage("Convert CallDTOs to CallEntity.Builder", callDTOMap -> {
                Map<UUID, CallEntity.Builder> dataCarriers = callDTOMap.entrySet().stream().collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> {
                            CallEntity.Builder builder = CallEntity.builder();
                            builder.callDTO = entry.getValue();
                            return builder;
                        }
                ));
                return dataCarriers;
            })
            .<Map<UUID, CallEntity.Builder>, Map<UUID, CallEntity.Builder>> addFunctionalStage("Fetch Peer Connections", callEntityBuilders -> {
                if (!fetchPeerConnections) {
                    return callEntityBuilders;
                }
                Set<UUID> peerConnectionUUIDs = new HashSet<>();
                for (CallEntity.Builder builder : callEntityBuilders.values()) {
                    Collection<UUID> pcUUIDs = hazelcastMaps.getCallToPCUUIDs().get(builder.callDTO.callUUID);
                    peerConnectionUUIDs.addAll(pcUUIDs);
                }
                fetchPCsTask.wherePCUuid(peerConnectionUUIDs).execute();
                if (!fetchPCsTask.succeeded()) {
                    getLogger().warn("Fetching peer connection was not completed successfully");
                    return callEntityBuilders;
                }
                Map<UUID, PeerConnectionEntity> pcEntities = fetchPCsTask.getResult();
                for (PeerConnectionEntity pcEntity : pcEntities.values()) {
                    CallEntity.Builder builder = callEntityBuilders.get(pcEntity.callUUID);
                    if (Objects.isNull(builder)) {
                        this.getLogger().warn("Call Entity Builder has not been set for peer connection {}", pcEntity);
                        continue;
                    }
                    builder.peerConnections.put(pcEntity.pcUUID, pcEntity);
                }
                return callEntityBuilders;
            })
            .<Map<UUID, CallEntity.Builder>>addTerminalFunction("Completed", callEntityBuilders -> {
                return callEntityBuilders.entrySet().stream()
                        .map(entry -> Map.entry(entry.getKey(), entry.getValue().build()))
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
            })
        .build();
    }

    public FetchCallsTask whereCallUUID(UUID... values) {
        if (Objects.isNull(values) || values.length < 1) {
            return this;
        }
        this.callUUIDs.addAll(Arrays.asList(values));
        return this;
    }

    public FetchCallsTask whereCallUUIDs(Set<UUID> callUUIDs) {
        if (Objects.isNull(callUUIDs) || callUUIDs.size() < 1) {
            return this;
        }
        this.callUUIDs.addAll(callUUIDs);
        return this;
    }

    public FetchCallsTask doNotFetchPeerConnections() {
        this.fetchPeerConnections = false;
        return this;
    }



    @Override
    protected void validate() {

    }



//    private class DataCarrier {
//
//        public CallDTO call;
//        public Set<Long> SSRCs = new HashSet<>();
//        public Map<UUID, NewPeerConnectionEntity> pcEntities = new HashMap<>();
//    }
}
