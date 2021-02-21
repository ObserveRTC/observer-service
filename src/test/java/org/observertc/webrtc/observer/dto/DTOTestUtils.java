package org.observertc.webrtc.observer.dto;

import org.jeasy.random.EasyRandom;

import javax.inject.Singleton;
import java.util.Objects;
import java.util.UUID;

@Singleton
public class DTOTestUtils {

    private static EasyRandom generator = new EasyRandom();

    public PeerConnectionDTO generatePeerConnectionDTO() {
        return this.generatePeerConnectionDTO(null, null);
    }

    public PeerConnectionDTO generatePeerConnectionDTO(UUID callUUID, UUID serviceUUID) {
        PeerConnectionDTO result = generator.nextObject(PeerConnectionDTO.class);
        if (Objects.nonNull(callUUID)) {
            result.callUUID = callUUID;
        }
        if (Objects.nonNull(serviceUUID)) {
            result.serviceUUID = serviceUUID;
        }
        return result;
    }

    public CallDTO generateCallDTO() {
        CallDTO result = generator.nextObject(CallDTO.class);
        return result;
    }

    public SentinelDTO generateSentinelDTO() {
        return generator.nextObject(SentinelDTO.class);
    }
}
