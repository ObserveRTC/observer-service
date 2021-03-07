package org.observertc.webrtc.observer.repositories;

import com.hazelcast.map.IMap;
import com.hazelcast.multimap.MultiMap;
import org.observertc.webrtc.observer.ObserverHazelcast;
import org.observertc.webrtc.observer.dto.*;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.UUID;

@Singleton
public class HazelcastMaps {

    @Inject
    ObserverHazelcast observerHazelcast;

    private IMap<UUID, CallDTO> callDTOs;

    private IMap<UUID, PeerConnectionDTO> pcDTOs;
    private MultiMap<UUID, UUID> callToPCUUIDs;
    private IMap<String, WeakLockDTO> weakLocks;
    private IMap<String, SentinelDTO> sentinelDTOs;
    private IMap<String, SentinelFilterDTO> sentinelFilterDTOs;
    private MultiMap<String, UUID> serviceToUUIDs;
    private IMap<UUID, String> uuidToService;

    @PostConstruct
    void setup() {
        this.callDTOs = observerHazelcast.getInstance().getMap("observertc-calldtos");
        this.pcDTOs = observerHazelcast.getInstance().getMap("observertc-pcdtos");
        this.callToPCUUIDs = observerHazelcast.getInstance().getMultiMap("observertc-call-to-pcuuids");
        this.weakLocks = observerHazelcast.getInstance().getMap("observertc-weaklocks");
        this.sentinelDTOs = observerHazelcast.getInstance().getMap("observertc-sentinels");
        this.sentinelFilterDTOs = observerHazelcast.getInstance().getMap("observertc-sentinel-filters");
        this.uuidToService = observerHazelcast.getInstance().getMap("observertc-uuid-to-service");
        this.serviceToUUIDs = observerHazelcast.getInstance().getMultiMap("observertc-service-to-uuid");
    }

    public MultiMap<String, UUID> getCallNames(UUID serviceUUID) {
        String mapName = String.format("observertc-callnames-%s", serviceUUID.toString());
        MultiMap<String, UUID> result = observerHazelcast.getInstance().getMultiMap(mapName);
        return result;
    }

    public MultiMap<UUID, Long> getPCsToSSRCMap(UUID serviceUUID) {
        String mapName = String.format("observertc-pcs-to-ssrcs-%s", serviceUUID.toString());
        MultiMap<UUID, Long> result = observerHazelcast.getInstance().getMultiMap(mapName);
        return result;
    }

    public MultiMap<Long, UUID> getSSRCsToPCMap(UUID serviceUUID) {
        String mapName = String.format("observertc-ssrc-to-pc-%s", serviceUUID.toString());
        MultiMap<Long, UUID> result = observerHazelcast.getInstance().getMultiMap(mapName);
        return result;
    }

    public IMap<UUID, CallDTO> getCallDTOs(){
        return this.callDTOs;
    }

    public IMap<UUID, PeerConnectionDTO> getPcDTOs(){
        return this.pcDTOs;
    }

    public MultiMap<UUID, UUID> getCallToPCUUIDs() {
        return this.callToPCUUIDs;
    }

    public IMap<String, WeakLockDTO> getWeakLocks() {return this.weakLocks;}

    public IMap<String, SentinelFilterDTO> getSentinelFilterDTOs() {return this.sentinelFilterDTOs;}

    public IMap<String, SentinelDTO> getSentinelDTOs() {return this.sentinelDTOs;}

    public IMap<UUID, String> getUuidToService() {return this.uuidToService;}

    public MultiMap<String, UUID> getServiceToUUIDs() {return this.serviceToUUIDs;}
}
