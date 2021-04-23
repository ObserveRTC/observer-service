package org.observertc.webrtc.observer.dto;

import org.jeasy.random.EasyRandom;
import org.observertc.webrtc.observer.configs.CallFilterConfig;
import org.observertc.webrtc.observer.configs.CollectionFilterConfig;
import org.observertc.webrtc.observer.configs.SentinelConfig;

import javax.inject.Singleton;
import java.util.*;

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

    public SentinelConfig generateSentinelDTO() {
        return generator.nextObject(SentinelConfig.class);
    }

    public CallFilterConfig generateSentinelFilterDTO() {
        return generator.nextObject(CallFilterConfig.class);
    }

    public CollectionFilterConfig generateCollectionFilter() {
        return generator.nextObject(CollectionFilterConfig.class);
    }

    public CollectionFilterConfig generateSSRCCollectionFilter() {
        Random random = new Random();
        CollectionFilterConfig result = generator.nextObject(CollectionFilterConfig.class);
        List<String> SSRCsList = new LinkedList<>();
        for (int i = 0; i < random.nextInt(10); ++i)
            SSRCsList.add(Long.toString(random.nextLong()));
        result.anyMatch = SSRCsList.toArray(new String[0]);

        SSRCsList.clear();
        for (int i = 0; i < random.nextInt(10); ++i)
            SSRCsList.add(Long.toString(random.nextLong()));
        result.allMatch = SSRCsList.toArray(new String[0]);
        return result;
    }

    public WeakLockDTO generateWeakLockDTO() {
        return generator.nextObject(WeakLockDTO.class);
    }
}
