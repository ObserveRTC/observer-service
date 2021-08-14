package org.observertc.webrtc.observer.repositories;

import com.hazelcast.map.IMap;
import com.hazelcast.multimap.MultiMap;
import org.observertc.webrtc.observer.ObserverHazelcast;
import org.observertc.webrtc.observer.configs.ConfigEntryDispatcher;
import org.observertc.webrtc.observer.configs.ConfigType;
import org.observertc.webrtc.observer.configs.ObserverConfig;
import org.observertc.webrtc.observer.dto.*;
import org.observertc.webrtc.observer.evaluators.ListenClientEntryChanges;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;
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
    private static final String HAZELCAST_SFUS_MAP_NAME = "observertc-sfus";
    private static final String HAZELCAST_SFU_TRANSPORTS_MAP_NAME = "observertc-sfu-transports";
    private static final String HAZELCAST_SFU_RTP_STREAMS_MAP_NAME = "observertc-sfu-rtp-streams";

    private static final String HAZELCAST_INBOUND_TO_OUTBOUND_TRACK_IDS_MAP_NAME = "observertc-inbound-track-ids-to-outbound-track-ids";

    private static final String HAZELCAST_SFU_STREAM_TO_MEDIA_TRACK_ID_MAP_NAME = "observertc-sfu-stream-to-media-tracks";

    public static final String HAZELCAST_WEAKLOCKS_MAP_NAME = "observertc-weaklocks";
    public static final String HAZELCAST_CONFIGURATIONS_MAP_NAME = "observertc-configurations";


    @Inject
    ObserverHazelcast observerHazelcast;

    @Inject
    ListenClientEntryChanges listenClientEntryChanges;

//    @Inject
//    RemovePeerConnectionEntities removePeerConnectionEntities;
//
//    @Inject
//    RemoveMediaTrackEntities removeMediaTrackEntities;


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

    // media tracks
    private IMap<UUID, MediaTrackDTO> mediaTracks;

    // SFU
    private IMap<UUID, SfuDTO> SFUs;
    private IMap<UUID, SfuTransportDTO> sfuTransports;
    private IMap<UUID, SfuRtpStreamDTO> sfuRtpStreams;

    // track bindings
    private IMap<UUID, UUID> inboundTrackIdsToOutboundTrackIds;

    // sfu stream bindings
    private IMap<UUID, UUID> sfuStreamToCalls;
    private MultiMap<UUID, UUID> callToSfuStreams;

    // sfu transport bindings
    private IMap<UUID, UUID> sfuTransportToCalls;
    private MultiMap<UUID, UUID> callToSfuTransports;

    private IMap<UUID, UUID> sfuStreamsToMediaTracks;

    // other necessary maps
    private IMap<String, WeakLockDTO> weakLocks;
    private IMap<ConfigType, ConfigDTO> configurations;

    private ObserverConfig.RepositoryConfig config;

    public HazelcastMaps(ObserverConfig observerConfig) {
        this.config = observerConfig.repositories;
    }

    @PostConstruct
    void setup() {
        this.calls = observerHazelcast.getInstance().getMap(HAZELCAST_CALLS_MAP_NAME);
        this.callToClientIds = observerHazelcast.getInstance().getMultiMap(HAZELCAST_CALL_TO_CLIENT_IDS_MAP_NAME);
        this.serviceRoomToCallIds = observerHazelcast.getInstance().getMap(HAZELCAST_SERVICE_ROOM_TO_CALL_ID_MAP_NAME);

        this.clients = observerHazelcast.getInstance().getMap(HAZELCAST_CLIENTS_MAP_NAME);
        this.clientToPeerConnectionIds = observerHazelcast.getInstance().getMultiMap(HAZELCAST_CLIENT_TO_PEER_CONNECTION_IDS_MAP_NAME);

        this.peerConnections = observerHazelcast.getInstance().getMap(HAZELCAST_PEER_CONNECTIONS_MAP_NAME);
        this.peerConnectionToInboundMediaTrackIds = observerHazelcast.getInstance().getMultiMap(HAZELCAST_PEER_CONNECTIONS_INBOUND_TRACK_IDS_MAP_NAME);
        this.peerConnectionToOutboundMediaTrackIds = observerHazelcast.getInstance().getMultiMap(HAZELCAST_PEER_CONNECTIONS_OUTBOUND_TRACK_IDS_MAP_NAME);

        this.mediaTracks = observerHazelcast.getInstance().getMap(HAZELCAST_MEDIA_TRACKS_MAP_NAME);

        this.SFUs = observerHazelcast.getInstance().getMap(HAZELCAST_SFUS_MAP_NAME);
        this.sfuTransports = observerHazelcast.getInstance().getMap(HAZELCAST_SFU_TRANSPORTS_MAP_NAME);
        this.sfuRtpStreams = observerHazelcast.getInstance().getMap(HAZELCAST_SFU_RTP_STREAMS_MAP_NAME);

        this.inboundTrackIdsToOutboundTrackIds = observerHazelcast.getInstance().getMap(HAZELCAST_INBOUND_TO_OUTBOUND_TRACK_IDS_MAP_NAME);

        this.sfuStreamsToMediaTracks = observerHazelcast.getInstance().getMap(HAZELCAST_SFU_STREAM_TO_MEDIA_TRACK_ID_MAP_NAME);

        this.configurations = observerHazelcast.getInstance().getMap(HAZELCAST_CONFIGURATIONS_MAP_NAME);
        this.weakLocks = observerHazelcast.getInstance().getMap(HAZELCAST_WEAKLOCKS_MAP_NAME);
        // setup expirations
        observerHazelcast.getInstance()
                .getConfig()
                .getMapConfig(HAZELCAST_CLIENTS_MAP_NAME)
                .setMaxIdleSeconds(this.config.clientMaxIdleTime);

        observerHazelcast.getInstance()
                .getConfig()
                .getMapConfig(HAZELCAST_PEER_CONNECTIONS_MAP_NAME)
                .setMaxIdleSeconds(this.config.peerConnectionsMaxIdleTime);

        observerHazelcast.getInstance()
                .getConfig()
                .getMapConfig(HAZELCAST_MEDIA_TRACKS_MAP_NAME)
                .setMaxIdleSeconds(this.config.mediaTracksMaxIdleTime);

        observerHazelcast.getInstance()
                .getConfig()
                .getMapConfig(HAZELCAST_SFUS_MAP_NAME)
                .setMaxIdleSeconds(this.config.sfuMaxIdleTime);

        observerHazelcast.getInstance()
                .getConfig()
                .getMapConfig(HAZELCAST_SFU_TRANSPORTS_MAP_NAME)
                .setMaxIdleSeconds(this.config.sfuTransportMaxIdleTime);

        observerHazelcast.getInstance()
                .getConfig()
                .getMapConfig(HAZELCAST_SFU_RTP_STREAMS_MAP_NAME)
                .setMaxIdleSeconds(this.config.sfuRtpStreamMaxIdleTime);


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

    public IMap<UUID, SfuDTO> getSFUs() {
        return this.SFUs;
    }
    public IMap<UUID, SfuTransportDTO> getSFUTransports() {
        return this.sfuTransports;
    }
    public IMap<UUID, SfuRtpStreamDTO> getSFURtpStreams() {
        return this.sfuRtpStreams;
    }

//    public IMap<UUID, TrackLinkDTO> getInboundLinkedTracks() { return this.l; }
    public IMap<UUID, UUID> getInboundTrackIdsToOutboundTrackIds() { return this.inboundTrackIdsToOutboundTrackIds; }

    public IMap<UUID, UUID> getSfuStreamsToMediaTracks() { return this.sfuStreamsToMediaTracks; }

    public IMap<String, WeakLockDTO> getWeakLocks() {return this.weakLocks;}

    public IMap<ConfigType, ConfigDTO> getConfigurations() { return this.configurations; }
}
