package org.observertc.observer.repositories;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import com.hazelcast.multimap.MultiMap;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.observertc.observer.ObserverHazelcast;
import org.observertc.observer.configs.ObserverConfig;
import org.observertc.observer.dto.*;

import javax.annotation.PostConstruct;
import java.util.UUID;

@Singleton
public class HazelcastMaps {

    private static final String HAZELCAST_CALLS_MAP_NAME = "observertc-calls";
    private static final String HAZELCAST_CALL_TO_CLIENT_IDS_MAP_NAME = "observertc-call-to-clients";
    private static final String HAZELCAST_SERVICE_ROOM_TO_CALL_ID_MAP_NAME = "observertc-service-room-to-call";

    // client related
    // public, because observer configure the map with expiration
    public static final String HAZELCAST_CLIENTS_MAP_NAME = "observertc-clients";
    private static final String HAZELCAST_CLIENT_TO_PEER_CONNECTION_IDS_MAP_NAME = "observertc-client-to-peerconnections";

    // pc related
    private static final String HAZELCAST_PEER_CONNECTIONS_MAP_NAME = "observertc-peerconnections";
    private static final String HAZELCAST_PEER_CONNECTIONS_INBOUND_TRACK_IDS_MAP_NAME = "observertc-peerconnections-to-inbound-tracks";
    private static final String HAZELCAST_PEER_CONNECTIONS_OUTBOUND_TRACK_IDS_MAP_NAME = "observertc-peerconnections-to-outbound-tracks";

    // MediaTrack
    private static final String HAZELCAST_MEDIA_TRACKS_MAP_NAME = "observertc-peerconnection-media-tracks";

    // SFU Transports
    private static final String HAZELCAST_SFU_MAP_NAME = "observertc-sfu";
    private static final String HAZELCAST_SFU_TRANSPORTS_MAP_NAME = "observertc-sfu-transports";
    private static final String HAZELCAST_SFU_RTP_PADS_MAP_NAME = "observertc-sfu-rtp-pads";

    private static final String HAZELCAST_SFU_SINK_ID_TO_RTP_PAD_IDS = "observertc-sfu-sink-id-to-rtp-pad-ids";
    private static final String HAZELCAST_SFU_STREAM_ID_TO_RTP_PAD_IDS = "observertc-sfu-stream-id-to-rtp-pad-ids";
    private static final String HAZELCAST_SFU_STREAMS_MAP_NAME = "observertc-sfu-streams";
    private static final String HAZELCAST_SFU_SINKS_MAP_NAME = "observertc-sfu-sinks";

    private static final String HAZELCAST_INBOUND_TO_OUTBOUND_TRACK_IDS_MAP_NAME = "observertc-inbound-track-ids-to-outbound-track-ids";

    public static final String HAZELCAST_WEAKLOCKS_MAP_NAME = "observertc-weaklocks";

    public static final String HAZELCAST_GENERALENTRIES = "observertc-client-messages";

    public static final String HAZELCAST_SYNC_TASK_STATES_MAP_NAME = "observertc-distributed-tasks-states";
    public static final String HAZELCAST_ETC_MAP_NAME = "observertc-distributed-map";
    public static final String HAZELCAST_REQUESTS_MAP_NAME = "observertc-requests-map-name";

    @Inject
    ObserverHazelcast observerHazelcast;

    // calls
    private IMap<UUID, CallDTO> calls;
    private MultiMap<UUID, UUID> callToClientIds;
    private IMap<String, UUID> serviceRoomToCallIds;

    // clients
    private IMap<UUID, ClientDTO> clients;
    private MultiMap<UUID, UUID> clientToPeerConnectionIds;

    // peer connections
    private IMap<UUID, PeerConnectionDTO> peerConnections;
    private MultiMap<UUID, UUID> peerConnectionToInboundMediaTrackIds;
    private MultiMap<UUID, UUID> peerConnectionToOutboundMediaTrackIds;
//    private IMap<UUID, UUID> rtpStreamIdsToOutboundTrackIds;

    // media tracks
    private IMap<UUID, MediaTrackDTO> mediaTracks;

    // SFU
    private IMap<UUID, SfuDTO> SFUs;
    private IMap<UUID, SfuTransportDTO> sfuTransports;
    private IMap<UUID, SfuRtpPadDTO> sfuRtpPads;
    private IMap<UUID, SfuStreamDTO> sfuStreams;
    private IMap<UUID, SfuSinkDTO> sfuSinks;
    private MultiMap<UUID, UUID> sfuStreamIdToRtpPadIds;
    private MultiMap<UUID, UUID> sfuSinkIdToRtpPadIds;


//    private MultiMap<UUID, UUID> rtpStreamIdToSfuPadIds;
    // track bindings
    private IMap<UUID, UUID> inboundTrackIdsToOutboundTrackIds;

    // other necessary maps
    private IMap<String, WeakLockDTO> weakLocks;
    private IMap<String, String> syncTaskStates;
    private IMap<String, byte[]> requests;
    private IMap<String, String> etcMap;
    private ObserverConfig.RepositoryConfig config;

    private IMap<UUID, GeneralEntryDTO> generalEntries;

    public HazelcastMaps(ObserverConfig observerConfig) {
        this.config = observerConfig.repository;
    }

    @PostConstruct
    void setup() {
        final HazelcastInstance hazelcast = observerHazelcast.getInstance();
        this.calls = hazelcast.getMap(HAZELCAST_CALLS_MAP_NAME);
        this.callToClientIds = hazelcast.getMultiMap(HAZELCAST_CALL_TO_CLIENT_IDS_MAP_NAME);
        this.serviceRoomToCallIds = hazelcast.getMap(HAZELCAST_SERVICE_ROOM_TO_CALL_ID_MAP_NAME);

        this.clients = hazelcast.getMap(HAZELCAST_CLIENTS_MAP_NAME);
        this.clientToPeerConnectionIds = hazelcast.getMultiMap(HAZELCAST_CLIENT_TO_PEER_CONNECTION_IDS_MAP_NAME);

        this.peerConnections = hazelcast.getMap(HAZELCAST_PEER_CONNECTIONS_MAP_NAME);
        this.peerConnectionToInboundMediaTrackIds = hazelcast.getMultiMap(HAZELCAST_PEER_CONNECTIONS_INBOUND_TRACK_IDS_MAP_NAME);
        this.peerConnectionToOutboundMediaTrackIds = hazelcast.getMultiMap(HAZELCAST_PEER_CONNECTIONS_OUTBOUND_TRACK_IDS_MAP_NAME);
//        this.rtpStreamIdsToOutboundTrackIds = hazelcast.getMap(HAZELCAST_RTP_STREAM_IDS_TO_OUTBOUND_TRACKS_MAP_NAME);
        this.mediaTracks = hazelcast.getMap(HAZELCAST_MEDIA_TRACKS_MAP_NAME);
        this.inboundTrackIdsToOutboundTrackIds = hazelcast.getMap(HAZELCAST_INBOUND_TO_OUTBOUND_TRACK_IDS_MAP_NAME);

        this.SFUs = hazelcast.getMap(HAZELCAST_SFU_MAP_NAME);
        this.sfuTransports = hazelcast.getMap(HAZELCAST_SFU_TRANSPORTS_MAP_NAME);
        this.sfuRtpPads = hazelcast.getMap(HAZELCAST_SFU_RTP_PADS_MAP_NAME);
        this.sfuStreams = hazelcast.getMap(HAZELCAST_SFU_STREAMS_MAP_NAME);
        this.sfuSinks = hazelcast.getMap(HAZELCAST_SFU_SINKS_MAP_NAME);
        this.sfuSinkIdToRtpPadIds = hazelcast.getMultiMap(HAZELCAST_SFU_SINK_ID_TO_RTP_PAD_IDS);
        this.sfuStreamIdToRtpPadIds = hazelcast.getMultiMap(HAZELCAST_SFU_STREAM_ID_TO_RTP_PAD_IDS);

//        this.rtpStreamIdToSfuPadIds = hazelcast.getMultiMap(HAZELCAST_RTP_STREAM_ID_TO_SFU_PAD_IDS_MAP_NAME);

        this.weakLocks = hazelcast.getMap(HAZELCAST_WEAKLOCKS_MAP_NAME);
        this.syncTaskStates = hazelcast.getMap(HAZELCAST_SYNC_TASK_STATES_MAP_NAME);
        this.requests = hazelcast.getMap(HAZELCAST_REQUESTS_MAP_NAME);
        this.etcMap = hazelcast.getMap(HAZELCAST_ETC_MAP_NAME);

        this.generalEntries = hazelcast.getMap(HAZELCAST_GENERALENTRIES);
        // setup expirations
        hazelcast
                .getConfig()
                .getMapConfig(HAZELCAST_CLIENTS_MAP_NAME)
                .setMaxIdleSeconds(this.config.clientMaxIdleTimeInS);

        hazelcast
                .getConfig()
                .getMapConfig(HAZELCAST_PEER_CONNECTIONS_MAP_NAME)
                .setMaxIdleSeconds(this.config.peerConnectionsMaxIdleTime);

        hazelcast
                .getConfig()
                .getMapConfig(HAZELCAST_MEDIA_TRACKS_MAP_NAME)
                .setMaxIdleSeconds(this.config.mediaTracksMaxIdleTimeInS);

        hazelcast
                .getConfig()
                .getMapConfig(HAZELCAST_SFU_MAP_NAME)
                .setMaxIdleSeconds(this.config.sfuMaxIdleTimeInS);

        hazelcast
                .getConfig()
                .getMapConfig(HAZELCAST_SFU_TRANSPORTS_MAP_NAME)
                .setMaxIdleSeconds(this.config.sfuTransportMaxIdleTimeInS);

        hazelcast
                .getConfig()
                .getMapConfig(HAZELCAST_SFU_RTP_PADS_MAP_NAME)
                .setMaxIdleSeconds(this.config.sfuRtpPadMaxIdleTimeInS);

        var bindingLifetime = Math.max(this.config.mediaTracksMaxIdleTimeInS, this.config.sfuRtpPadMaxIdleTimeInS);
        hazelcast
                .getConfig()
                .getMapConfig(HAZELCAST_SFU_STREAMS_MAP_NAME)
                .setMaxIdleSeconds(bindingLifetime);

        hazelcast
                .getConfig()
                .getMapConfig(HAZELCAST_SFU_SINKS_MAP_NAME)
                .setMaxIdleSeconds(bindingLifetime);

        hazelcast
                .getConfig()
                .getMapConfig(HAZELCAST_SYNC_TASK_STATES_MAP_NAME)
                .setMaxIdleSeconds(3600); // one hour

        hazelcast
                .getConfig()
                .getMapConfig(HAZELCAST_REQUESTS_MAP_NAME)
                .setMaxIdleSeconds(3600); // one hour
    }

    public IMap<UUID, CallDTO> getCalls(){
        return this.calls;
    }
    public MultiMap<UUID, UUID> getCallToClientIds() { return this.callToClientIds; }
    public IMap<String, UUID> getServiceRoomToCallIds() { return this.serviceRoomToCallIds; }

    public IMap<UUID, ClientDTO> getClients() { return this.clients; }
    public MultiMap<UUID, UUID> getClientToPeerConnectionIds() { return this.clientToPeerConnectionIds; }

    public IMap<UUID, PeerConnectionDTO> getPeerConnections() { return this.peerConnections; }
    public MultiMap<UUID, UUID> getPeerConnectionToInboundTrackIds() { return this.peerConnectionToInboundMediaTrackIds; }
    public MultiMap<UUID, UUID> getPeerConnectionToOutboundTrackIds() { return this.peerConnectionToOutboundMediaTrackIds; }

    public IMap<UUID, MediaTrackDTO> getMediaTracks() {
        return this.mediaTracks;
    }
    public IMap<UUID, UUID> getInboundTrackIdsToOutboundTrackIds() { return this.inboundTrackIdsToOutboundTrackIds; }

    public IMap<UUID, SfuDTO> getSFUs() {
        return this.SFUs;
    }

    public IMap<UUID, SfuTransportDTO> getSFUTransports() {
        return this.sfuTransports;
    }

    public IMap<UUID, SfuRtpPadDTO> getSFURtpPads() {
        return this.sfuRtpPads;
    }

    public MultiMap<UUID, UUID> getSfuStreamIdToRtpPadIds() { return this.sfuStreamIdToRtpPadIds; }
    public MultiMap<UUID, UUID> getSfuSinkIdToRtpPadIds() { return this.sfuSinkIdToRtpPadIds; }

    public IMap<UUID, SfuStreamDTO> getSfuStreams() { return this.sfuStreams; }
    public IMap<UUID, SfuSinkDTO> getSfuSinks() { return this.sfuSinks; }

    public IMap<UUID, GeneralEntryDTO> getGeneralEntries() { return this.generalEntries; }

    public IMap<String, WeakLockDTO> getWeakLocks() {return this.weakLocks;}
    public IMap<String, String> getSyncTaskStates() { return this.syncTaskStates; }
    public IMap<String, byte[]> getRequests() { return this.requests; }

    public IMap<String, String> getEtcMap() { return this.etcMap; }

}
