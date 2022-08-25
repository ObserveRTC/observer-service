package org.observertc.observer.repositories;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import com.hazelcast.multimap.MultiMap;
import io.github.balazskreith.hamok.storagegrid.FederatedStorage;
import io.github.balazskreith.hamok.storagegrid.ReplicatedStorage;
import io.github.balazskreith.hamok.storagegrid.SeparatedStorage;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.observertc.observer.configs.ObserverConfig;
import org.observertc.observer.dto.GeneralEntryDTO;
import org.observertc.observer.dto.SfuSinkDTO;
import org.observertc.observer.dto.SfuStreamDTO;
import org.observertc.observer.dto.WeakLockDTO;
import org.observertc.schemas.dtos.Models;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.Set;
import java.util.UUID;

@Singleton
public class HamokStorages {

    private static final Logger logger = LoggerFactory.getLogger(HamokStorages.class);

    private static final String CALLS_STORAGE_ID = "observertc-calls";
    private static final String CALL_TO_CLIENT_IDS_STORAGE_ID = "observertc-call-to-clients";
    private static final String SERVICE_ROOM_TO_CALL_ID_STORAGE_ID = "observertc-service-room-to-call";

    // client related
    // public, because observer configure the map with expiration
    public static final String CLIENTS_STORAGE_ID = "observertc-clients";
    private static final String CLIENT_TO_PEER_CONNECTION_IDS_STORAGE_ID = "observertc-client-to-peerconnections";

    // pc related
    private static final String PEER_CONNECTIONS_STORAGE_ID = "observertc-peerconnections";
    private static final String PEER_CONNECTIONS_INBOUND_TRACK_IDS_STORAGE_ID = "observertc-peerconnections-to-inbound-tracks";
    private static final String PEER_CONNECTIONS_OUTBOUND_TRACK_IDS_STORAGE_ID = "observertc-peerconnections-to-outbound-tracks";

    // MediaTrack
    private static final String MEDIA_TRACKS_STORAGE_ID = "observertc-peerconnection-media-tracks";

    // SFU Transports
    private static final String SFU_STORAGE_ID = "observertc-sfu";
    private static final String SFU_TRANSPORTS_STORAGE_ID = "observertc-sfu-transports";
    private static final String SFU_TO_TRANSPORT_SFU_IDS_STORAGE_ID = "observertc-sfu-to-sfu-transport-ids";
    private static final String SFU_RTP_PADS_STORAGE_ID = "observertc-sfu-rtp-pads";
    private static final String SFU_TRANSPORT_TO_SFU_RTP_PAD_IDS_STORAGE_ID = "observertc-sfu-transport-to-rtp-pad-ids";

    private static final String SFU_SINK_ID_TO_RTP_PAD_IDS = "observertc-sfu-sink-id-to-rtp-pad-ids";
    private static final String SFU_STREAM_ID_TO_RTP_PAD_IDS = "observertc-sfu-stream-id-to-rtp-pad-ids";
    private static final String SFU_STREAMS_STORAGE_ID = "observertc-sfu-streams";
    private static final String SFU_SINKS_STORAGE_ID = "observertc-sfu-sinks";

    private static final String INBOUND_TO_OUTBOUND_TRACK_IDS_STORAGE_ID = "observertc-inbound-track-ids-to-outbound-track-ids";
    private static final String SFU_STREAM_IDS_TO_INTERNAL_OUTBOUND_RTP_PAD_IDS = "observertc-sfu-stream-ids-to-internal-outbound-rtp-pad-ids";
    private static final String SFU_INTERNAL_INBOUND_RTP_PAD_ID_TO_OUTBOUND_RTP_PAD_ID = "observertc-sfu-internal-inbound-rtp-pad-id-to-outbound-rtp-pad-id";

    public static final String ETC_STORAGE_ID = "observertc-distributed-map";
    public static final String REQUESTS_STORAGE_ID = "observertc-requests-map-name";

    public static final String REFRESHED_CLIENTS = "observertc-refreshed-clients";
    public static final String REFRESHED_SFU_TRANSPORTS = "observertc-refreshed-sfu-transports";

    @Inject
    HamokService service;

    // service + room -> calls
    private ReplicatedStorage<String, String> serviceRoomToCallIds;

    // calls
    private ReplicatedStorage<String, Models.Call> calls;
    private FederatedStorage<String, Set<String>> callToClientIds;

    // clients
    private SeparatedStorage<String, Models.Client> clients;

    // peer connections
    private SeparatedStorage<String, Models.PeerConnection> peerConnections;
//    private IMap<UUID, UUID> rtpStreamIdsToOutboundTrackIds;

    // media tracks
    private SeparatedStorage<String, Models.InboundTrack> inboundTracks;
    private SeparatedStorage<String, Models.OutboundTrack> outboundTracks;

    // SFU
    private ReplicatedStorage<String, Models.Sfu> SFUs;
    private FederatedStorage<String, Set<String>> sfuToSfuTransportIds;

    private SeparatedStorage<String, Models.SfuTransport> sfuTransports;

    private SeparatedStorage<String, Models.SfuInboundRtpPad> sfuInboundRtpPads;
    private SeparatedStorage<String, Models.SfuOutboundRtpPad> sfuOutboundRtpPads;
    
    private FederatedStorage<String, Models.SfuSource> sfuStreams;
    private FederatedStorage<String, Models.SfuSink> sfuSinks;

//    private SeparatedStorage<UUID, UUID> sfuStreamIdToInternalOutboundRtpPadIds;
//    private IMap<UUID, UUID> sfuInternalInboundRtpPadIdToOutboundRtpPadId;

//    private MultiMap<UUID, UUID> rtpStreamIdToSfuPadIds;
    // track bindings
    private SeparatedStorage<String, String> inboundTrackIdsToOutboundTrackIds;

    private ObserverConfig.RepositoryConfig config;

    public HamokStorages(ObserverConfig observerConfig) {
        this.config = observerConfig.repository;
    }

    @PostConstruct
    void setup() {
        final HazelcastInstance hazelcast = observerHazelcast.getInstance();

        // setup expirations
        hazelcast
                .getConfig()
                .getMapConfig(CLIENTS_STORAGE_ID)
                .setMaxIdleSeconds(this.config.clientMaxIdleTimeInS);

        hazelcast
                .getConfig()
                .getMapConfig(PEER_CONNECTIONS_STORAGE_ID)
                .setMaxIdleSeconds(this.config.peerConnectionsMaxIdleTime);

        hazelcast
                .getConfig()
                .getMapConfig(MEDIA_TRACKS_STORAGE_ID)
                .setMaxIdleSeconds(this.config.mediaTracksMaxIdleTimeInS);

        hazelcast
                .getConfig()
                .getMapConfig(SFU_STORAGE_ID)
                .setMaxIdleSeconds(this.config.sfuMaxIdleTimeInS);

        hazelcast
                .getConfig()
                .getMapConfig(SFU_TRANSPORTS_STORAGE_ID)
                .setMaxIdleSeconds(this.config.sfuTransportMaxIdleTimeInS);

        hazelcast
                .getConfig()
                .getMapConfig(SFU_RTP_PADS_STORAGE_ID)
                .setMaxIdleSeconds(this.config.sfuRtpPadMaxIdleTimeInS);

        var bindingLifetime = Math.max(this.config.mediaTracksMaxIdleTimeInS, this.config.sfuRtpPadMaxIdleTimeInS);
        hazelcast
                .getConfig()
                .getMapConfig(SFU_STREAMS_STORAGE_ID)
                .setMaxIdleSeconds(bindingLifetime);

        hazelcast
                .getConfig()
                .getMapConfig(SFU_SINKS_STORAGE_ID)
                .setMaxIdleSeconds(bindingLifetime);


        this.calls = hazelcast.getMap(CALLS_STORAGE_ID);
        this.callToClientIds = hazelcast.getMultiMap(CALL_TO_CLIENT_IDS_STORAGE_ID);
        this.serviceRoomToCallIds = hazelcast.getMap(SERVICE_ROOM_TO_CALL_ID_STORAGE_ID);

        this.clients = hazelcast.getMap(CLIENTS_STORAGE_ID);

        this.peerConnections = hazelcast.getMap(PEER_CONNECTIONS_STORAGE_ID);
        this.inboundTrackIdsToOutboundTrackIds = hazelcast.getMap(INBOUND_TO_OUTBOUND_TRACK_IDS_STORAGE_ID);

        this.SFUs = hazelcast.getMap(SFU_STORAGE_ID);

        this.sfuTransports = hazelcast.getMap(SFU_TRANSPORTS_STORAGE_ID);
        this.sfuToSfuTransportIds = hazelcast.getMultiMap(SFU_TO_TRANSPORT_SFU_IDS_STORAGE_ID);

        this.sfuStreams = hazelcast.getMap(SFU_STREAMS_STORAGE_ID);
        this.sfuSinks = hazelcast.getMap(SFU_SINKS_STORAGE_ID);
        this.sfuStreamIdToInternalOutboundRtpPadIds = hazelcast.getMultiMap(SFU_STREAM_IDS_TO_INTERNAL_OUTBOUND_RTP_PAD_IDS);

        this.sfuInternalInboundRtpPadIdToOutboundRtpPadId = hazelcast.getMap(SFU_INTERNAL_INBOUND_RTP_PAD_ID_TO_OUTBOUND_RTP_PAD_ID);
//        this.rtpStreamIdToSfuPadIds = hazelcast.getMultiMap(RTP_STREAM_ID_TO_SFU_PAD_IDS_STORAGE_ID);
    }

    @PreDestroy
    void teardown() {

    }


    public ReplicatedStorage<String, Models.Call> getCalls(){
        return this.calls;
    }
    public FederatedStorage<String, Set<String>> getCallToClientIds() { return this.callToClientIds; }
    public FederatedStorage<String, Set<String>> getServiceRoomToCallIds() { return this.serviceRoomToCallIds; }

    public SeparatedStorage<String, Models.Client> getClients() { return this.clients; }
    public FederatedStorage<String, Set<String>> getClientToPeerConnectionIds() { return this.clientToPeerConnectionIds; }

    public SeparatedStorage<String, Models.PeerConnection> getPeerConnections() { return this.peerConnections; }
    public FederatedStorage<String, Set<String>> getPeerConnectionToInboundTrackIds() { return this.peerConnectionToInboundMediaTrackIds; }
    public FederatedStorage<String, Set<String>> getPeerConnectionToOutboundTrackIds() { return this.peerConnectionToOutboundMediaTrackIds; }

    public SeparatedStorage<String, Models.InboundTrack> getInboundTracks() {
        return this.inboundTracks;
    }
    public SeparatedStorage<String, Models.OutboundTrack> getOutboundTracks() {
        return this.outboundTracks;
    }
    public IMap<UUID, UUID> getInboundTrackIdsToOutboundTrackIds() { return this.inboundTrackIdsToOutboundTrackIds; }

    public SeparatedStorage<String, Models.Sfu> getSFUs() {
        return this.SFUs;
    }
    public FederatedStorage<String, Set<String>> getSfuToSfuTransportIds() { return this.sfuToSfuTransportIds; }

    public SeparatedStorage<String, Models.SfuTransport> getSFUTransports() {
        return this.sfuTransports;
    }
    public FederatedStorage<String, Set<String>> getSfuTransportToSfuInboundRtpPadIds() { return this.sfuTransportToSfuRtpPadIds; }
    public FederatedStorage<String, Set<String>> getSfuTransportToSfuOutboundRtpPadIds() { return this.sfuTransportToSfuRtpPadIds; }

    public SeparatedStorage<String, Models.SfuInboundRtpPad> getSfuInboundRtpPads() {
        return this.sfuInboundRtpPads;
    }
    public SeparatedStorage<String, Models.SfuOutboundRtpPad> getSfuOutboundRtpPads() {
        return this.sfuOutboundRtpPads;
    }

    public MultiMap<UUID, UUID> getSfuStreamIdToRtpPadIds() { return this.sfuStreamIdToRtpPadIds; }
    public MultiMap<UUID, UUID> getSfuSinkIdToRtpPadIds() { return this.sfuSinkIdToRtpPadIds; }
    public MultiMap<UUID, UUID> getSfuStreamIdToInternalOutboundRtpPadIds() { return this.sfuStreamIdToInternalOutboundRtpPadIds; }
    public IMap<UUID, UUID> getSfuInternalInboundRtpPadIdToOutboundRtpPadId() { return this.sfuInternalInboundRtpPadIdToOutboundRtpPadId; }

    public IMap<UUID, SfuStreamDTO> getSfuStreams() { return this.sfuStreams; }
    public IMap<UUID, SfuSinkDTO> getSfuSinks() { return this.sfuSinks; }

    public IMap<UUID, GeneralEntryDTO> getGeneralEntries() { return this.generalEntries; }

    public IMap<String, WeakLockDTO> getWeakLocks() {return this.weakLocks;}
    public IMap<String, String> getSyncTaskStates() { return this.syncTaskStates; }
    public IMap<String, byte[]> getRequests() { return this.requests; }


}
