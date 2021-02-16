package org.observertc.webrtc.observer.repositories.tasks;

import org.jeasy.random.EasyRandom;
import org.observertc.webrtc.observer.dto.CallDTO;
import org.observertc.webrtc.observer.dto.PeerConnectionDTO;
import org.observertc.webrtc.observer.entities.CallEntity;
import org.observertc.webrtc.observer.entities.PeerConnectionEntity;
import org.observertc.webrtc.observer.repositories.HazelcastMaps;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.*;
import java.util.stream.Collectors;

@Singleton
public class TestUtils {

    private static EasyRandom generator = new EasyRandom();

    @Inject
    HazelcastMaps hazelcastMaps;

    public PeerConnectionEntity generatePeerConnectionEntity() {
        return this.generatePeerConnectionEntity(null, null);
    }

    public PeerConnectionEntity generatePeerConnectionEntity(UUID callUUID, UUID serviceUUID) {
        PeerConnectionDTO pcDTO = generator.nextObject(PeerConnectionDTO.class);
        if (Objects.nonNull(callUUID)) {
            pcDTO.callUUID = callUUID;
        }
        if (Objects.nonNull(serviceUUID)) {
            pcDTO.serviceUUID = serviceUUID;
        }
        Set<Long> SSRCs = generator.longs(new Random().nextInt(10) + 1).boxed().collect(Collectors.toSet());
        return PeerConnectionEntity.builder()
                .withPCDTO(pcDTO)
                .withSSRCs(SSRCs)
                .build();
    }

    public CallEntity generateCallEntity() {
        CallDTO callDTO = generator.nextObject(CallDTO.class);
        Map<UUID, PeerConnectionEntity> pcs = new HashMap<>();
        for (int i =0, c = 1; i < c; ++i) {
            PeerConnectionEntity pcEntity = this.generatePeerConnectionEntity(callDTO.callUUID, callDTO.serviceUUID);
            pcs.put(pcEntity.pcUUID, pcEntity);
        }

        return CallEntity.builder()
                .withCallDTO(callDTO)
                .withPeerConnections(pcs)
                .build();
    }

    public void insertCallEntity(CallEntity callEntity) {
        hazelcastMaps.getCallDTOs().put(callEntity.call.callUUID, callEntity.call);
        callEntity.peerConnections.values().forEach(pcEntity -> {
            hazelcastMaps.getCallToPCUUIDs().put(callEntity.call.callUUID, pcEntity.pcUUID);
            insertPeerConnectionEntity(pcEntity);
        });
        if (Objects.nonNull(callEntity.call.callName)) {
            hazelcastMaps.getCallNames(callEntity.call.serviceUUID).put(callEntity.call.callName, callEntity.call.callUUID);
        }
    }

    public void insertPeerConnectionEntity(PeerConnectionEntity pcEntity) {
        hazelcastMaps.getPcDTOs().put(pcEntity.pcUUID, pcEntity.peerConnection);
        for (Long SSRC : pcEntity.SSRCs) {
            hazelcastMaps.getSSRCsToPCMap(pcEntity.serviceUUID).put(SSRC, pcEntity.pcUUID);
            hazelcastMaps.getPCsToSSRCMap(pcEntity.serviceUUID).put(pcEntity.pcUUID, SSRC);
        }
    }


    public boolean isPeerConnectionEntityStored(PeerConnectionEntity pcEntity) {
        PeerConnectionDTO pcDTO = hazelcastMaps.getPcDTOs().get(pcEntity.pcUUID);
        Collection<Long> SSRCs = hazelcastMaps.getPCsToSSRCMap(pcEntity.serviceUUID).get(pcEntity.pcUUID);
        return Objects.nonNull(pcDTO) &&
                Objects.nonNull(SSRCs) &&
                pcEntity.peerConnection.equals(pcDTO) &&
                pcEntity.SSRCs.stream().allMatch(SSRCs::contains) &&
                SSRCs.stream().allMatch(pcEntity.SSRCs::contains);
    }

    public boolean isPeerConnectionEntityDeleted(PeerConnectionEntity pcEntity) {
        PeerConnectionDTO pcDTO = hazelcastMaps.getPcDTOs().get(pcEntity.pcUUID);
        Collection<Long> SSRCs = hazelcastMaps.getPCsToSSRCMap(pcEntity.serviceUUID).get(pcEntity.pcUUID);
        Set<UUID> SSRCPCs = pcEntity.SSRCs.stream().map(hazelcastMaps.getSSRCsToPCMap(pcEntity.serviceUUID)::get).flatMap(Collection::stream).collect(Collectors.toSet());
        return Objects.isNull(pcDTO) &&
                Objects.nonNull(SSRCs) &&
                SSRCs.size() == 0 &&
                SSRCPCs.contains(pcEntity.pcUUID) == false
                ;
    }

    public boolean isCallEntityStored(CallEntity callEntity) {
        CallDTO callDTO = hazelcastMaps.getCallDTOs().get(callEntity.call.callUUID);
        return Objects.nonNull(callDTO) &&
                callEntity.call.equals(callDTO) &&
                callEntity.peerConnections.values().stream().allMatch(this::isPeerConnectionEntityStored);
    }

    public boolean isCallEntityDeleted(CallEntity callEntity) {
        CallDTO callDTO = hazelcastMaps.getCallDTOs().get(callEntity.call.callUUID);
        return Objects.isNull(callDTO) &&
                callEntity.peerConnections.values().stream().allMatch(this::isPeerConnectionEntityDeleted);
    }

}
