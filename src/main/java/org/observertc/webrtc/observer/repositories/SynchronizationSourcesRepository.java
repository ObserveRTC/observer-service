package org.observertc.webrtc.observer.repositories;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;

import javax.inject.Inject;
import java.util.Map;
import java.util.UUID;

@Deprecated
public class SynchronizationSourcesRepository {
    @Inject
    HazelcastInstance hazelcastInstance;



    private IMap<Long, UUID> getSsrcMap(UUID serviceUUID) {
        String mapName = String.format("SSRC-%s", serviceUUID.toString());
        IMap<Long, UUID> result = this.hazelcastInstance.getMap(mapName);
        return result;
    }

    public void addSsrc(UUID serviceUUID, Long SSRC, UUID callUUID) {
        this.getSsrcMap(serviceUUID).put(SSRC, callUUID);
    }

    public void addSsrc(UUID serviceUUID, Map<Long, UUID> entries) {
        this.getSsrcMap(serviceUUID).putAll(entries);
    }

}
