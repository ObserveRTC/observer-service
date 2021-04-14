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

    @Inject
    ObserverConfigDispatcher observerConfigDispatcher;

    private IMap<UUID, CallDTO> callDTOs;

    private IMap<UUID, PeerConnectionDTO> pcDTOs;
    private MultiMap<String, UUID> remoteIPToPCs;
    private MultiMap<UUID, String> pcToRemoteIPs;
    private MultiMap<UUID, UUID> callToPCUUIDs;
    private IMap<String, WeakLockDTO> weakLocks;
    private IMap<String, SentinelDTO> sentinelDTOs;
    private IMap<String, CallFilterDTO> callFilterDTOs;
    private IMap<String, PeerConnectionFilterDTO> pcFilterDTOs;
    private MultiMap<String, UUID> serviceToUUIDs;
    private IMap<UUID, String> uuidToService;
    private IMap<String, InboundRtpTrafficDTO> inboundRtpTrafficDTOs;
    private IMap<String, OutboundRtpTrafficDTO> outboundRtpTrafficDTOs;
    private IMap<String, RemoteInboundRtpTrafficDTO> remoteInboundRtpTrafficDTOs;
    private IMap<String, ConfigDTO> configurations;

    @PostConstruct
    void setup() {
        this.pcDTOs = observerHazelcast.getInstance().getMap("observertc-pcdtos");
        this.remoteIPToPCs = observerHazelcast.getInstance().getMultiMap("observertc-ip-to-pc");
        this.pcToRemoteIPs = observerHazelcast.getInstance().getMultiMap("observertc-pc-to-ip");

        this.callDTOs = observerHazelcast.getInstance().getMap("observertc-calldtos");
        this.callToPCUUIDs = observerHazelcast.getInstance().getMultiMap("observertc-call-to-pcuuids");
        this.weakLocks = observerHazelcast.getInstance().getMap("observertc-weaklocks");
        this.sentinelDTOs = observerHazelcast.getInstance().getMap("observertc-sentinels");
        this.callFilterDTOs = observerHazelcast.getInstance().getMap("observertc-call-filters");
        this.pcFilterDTOs = observerHazelcast.getInstance().getMap("observertc-pc-filters");
        this.uuidToService = observerHazelcast.getInstance().getMap("observertc-uuid-to-service");
        this.serviceToUUIDs = observerHazelcast.getInstance().getMultiMap("observertc-service-to-uuid");
        this.inboundRtpTrafficDTOs = observerHazelcast.getInstance().getMap("observertc-inbound-rtp-traffics");
        this.outboundRtpTrafficDTOs = observerHazelcast.getInstance().getMap("observertc-outbound-rtp-traffics");
        this.remoteInboundRtpTrafficDTOs = observerHazelcast.getInstance().getMap("observertc-remote-inbound-rtp-traffics");

        this.configurations = observerHazelcast.getInstance().getMap("observertc-configurations");
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

    public IMap<String, CallFilterDTO> getCallFilterDTOs() {return this.callFilterDTOs;}

    public IMap<String, PeerConnectionFilterDTO> getPeerConnectionFilterDTOs() { return this.pcFilterDTOs; }

    public IMap<String, SentinelDTO> getSentinelDTOs() {return this.sentinelDTOs;}

    public IMap<UUID, String> getUuidToService() {return this.uuidToService;}

    public MultiMap<String, UUID> getServiceToUUIDs() {return this.serviceToUUIDs;}

    public MultiMap<UUID, String> getPCsToRemoteIP() {
        return this.pcToRemoteIPs;
    }

    public MultiMap<String, UUID> getRemoteIPToPCs() {
        return this.remoteIPToPCs;
    }

    public IMap<String, InboundRtpTrafficDTO> getInboundRtpDTOs() { return this.inboundRtpTrafficDTOs; }

    public IMap<String, OutboundRtpTrafficDTO> getOutboundRtpDTOs() { return this.outboundRtpTrafficDTOs; }

    public IMap<String, RemoteInboundRtpTrafficDTO> getRemoteInboundTrafficDTOs() { return this.remoteInboundRtpTrafficDTOs; }

    public IMap<String, ConfigDTO> getConfigurations() { return this.configurations; }
}
