package org.observertc.webrtc.observer.repositories;

import org.jeasy.random.EasyRandom;
import org.observertc.webrtc.observer.dto.CallDTO;
import org.observertc.webrtc.observer.dto.PeerConnectionDTO;
import org.observertc.webrtc.observer.entities.CallEntity;
import org.observertc.webrtc.observer.entities.PeerConnectionEntity;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Singleton
public class HazelcastMapTestUtils {

    private static EasyRandom generator = new EasyRandom();

    @Inject
    HazelcastMaps hazelcastMaps;

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
        if (Objects.isNull(pcDTO)) return false;
        if (!pcEntity.peerConnection.equals(pcDTO)) return false;

        if (!isSSRCsStoredToPC(pcEntity.serviceUUID, pcEntity.pcUUID, pcEntity.SSRCs)) return false;
        return true;
    }

    public boolean isPeerConnectionEntityDeleted(PeerConnectionEntity pcEntity) {
        PeerConnectionDTO pcDTO = hazelcastMaps.getPcDTOs().get(pcEntity.pcUUID);
        if (Objects.nonNull(pcDTO)) return false;
        if (!isSSRCsDeletedFromPC(pcEntity.serviceUUID, pcEntity.pcUUID, pcEntity.SSRCs)) return false;
        return true;
    }

    public boolean isSSRCsStoredToPC(UUID serviceUUID, UUID pcUUID, Set<Long> SSRCs) {
        Set<UUID> storedPCs = SSRCs.stream().flatMap(SSRC -> hazelcastMaps.getSSRCsToPCMap(serviceUUID).get(SSRC).stream()).collect(Collectors.toSet());
        for (Long SSRC : SSRCs) {
            Collection<UUID> pcs = hazelcastMaps.getSSRCsToPCMap(serviceUUID).get(SSRC);
            if (!pcs.contains(pcUUID)) {
                return false;
            }
        }

        Collection<Long> storedSSRCs = hazelcastMaps.getPCsToSSRCMap(serviceUUID).get(pcUUID);
        boolean allSSRCsAreStored = SSRCs.stream().allMatch(storedSSRCs::contains);
        if (!allSSRCsAreStored) return false;
        boolean noOtherSSRCsAreStored = storedSSRCs.stream().allMatch(SSRCs::contains);
        if (!noOtherSSRCsAreStored) return false;
        return true;
    }

    public boolean isSSRCsDeletedFromPC(UUID serviceUUID, UUID pcUUID, Set<Long> SSRCs) {
        boolean pcIsNotBound = SSRCs.stream()
                .allMatch(SSRC -> !hazelcastMaps.getSSRCsToPCMap(serviceUUID).get(SSRC).contains(pcUUID));
        if (!pcIsNotBound) return false;
        Collection<Long> storedSSRCs = hazelcastMaps.getPCsToSSRCMap(serviceUUID).get(pcUUID);
        if (Objects.isNull(storedSSRCs) || storedSSRCs.size() < 1) return true;

        boolean allSSRCsAreDeleted = SSRCs.stream().allMatch(SSRC -> !storedSSRCs.contains(SSRC));
        if (!allSSRCsAreDeleted) return false;
        return true;
    }

    public boolean isCallEntityStored(CallEntity callEntity) {
        CallDTO callDTO = hazelcastMaps.getCallDTOs().get(callEntity.call.callUUID);
        if (Objects.isNull(callDTO)) return false;
        if (!callEntity.call.equals(callDTO)) return false;
        if (!callEntity.peerConnections.values().stream().allMatch(this::isPeerConnectionEntityStored)) return false;
        return true;
    }

    public boolean isCallEntityDeleted(CallEntity callEntity) {
        CallDTO callDTO = hazelcastMaps.getCallDTOs().get(callEntity.call.callUUID);
        return Objects.isNull(callDTO) &&
                callEntity.peerConnections.values().stream().allMatch(this::isPeerConnectionEntityDeleted);
    }


}
