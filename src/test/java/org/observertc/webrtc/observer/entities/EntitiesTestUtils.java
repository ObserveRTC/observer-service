package org.observertc.webrtc.observer.entities;

import org.jeasy.random.EasyRandom;
import org.observertc.webrtc.observer.dto.CallDTO;
import org.observertc.webrtc.observer.dto.DTOTestUtils;
import org.observertc.webrtc.observer.dto.PeerConnectionDTO;
import org.observertc.webrtc.observer.dto.SentinelDTO;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.*;
import java.util.stream.Collectors;

@Singleton
public class EntitiesTestUtils {

    private static EasyRandom generator = new EasyRandom();

    @Inject
    DTOTestUtils dtoTestUtils;

    public PeerConnectionEntity generatePeerConnectionEntity() {
        return this.generatePeerConnectionEntity(null, null);
    }

    public PeerConnectionEntity generatePeerConnectionEntity(UUID callUUID, UUID serviceUUID) {
        PeerConnectionDTO pcDTO = dtoTestUtils.generatePeerConnectionDTO(callUUID, serviceUUID);
        Set<Long> SSRCs = generator.longs(new Random().nextInt(10) + 1).boxed().collect(Collectors.toSet());
        return PeerConnectionEntity.builder()
                .withPCDTO(pcDTO)
                .withSSRCs(SSRCs)
                .build();
    }

    public SentinelEntity generateSentinelEntityWithoutFilter() {
        SentinelDTO sentinelDTO = dtoTestUtils.generateSentinelDTO();
        return SentinelEntity.builder()
                .withSentinelDTO(sentinelDTO)
                .build();
    }
    public CallEntity generateCallEntity() {
        return this.generateCallEntity(1);
    }

    public CallEntity generateCallEntity(int pcNum) {
        CallDTO callDTO = dtoTestUtils.generateCallDTO();
        Map<UUID, PeerConnectionEntity> pcs = new HashMap<>();
        for (int i =0; i < pcNum; ++i) {
            PeerConnectionEntity pcEntity = this.generatePeerConnectionEntity(callDTO.callUUID, callDTO.serviceUUID);
            pcs.put(pcEntity.pcUUID, pcEntity);
        }

        return CallEntity.builder()
                .withCallDTO(callDTO)
                .withPeerConnections(pcs)
                .build();
    }

}
