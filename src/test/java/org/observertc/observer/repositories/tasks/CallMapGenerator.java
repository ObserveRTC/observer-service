package org.observertc.observer.repositories.tasks;

import io.micronaut.context.annotation.Prototype;
import org.observertc.observer.dto.*;
import org.observertc.observer.utils.RandomGenerators;
import org.observertc.observer.repositories.HazelcastMaps;
import org.observertc.observer.utils.DTOGenerators;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.*;

@Prototype
public class CallMapGenerator {

    private CallDTO callDTO;
    private Map<UUID, ClientDTO> clientDTOs = new HashMap<>();
    private Map<UUID, PeerConnectionDTO> peerConnectionDTOs = new HashMap<>();
    private Map<UUID, MediaTrackDTO> mediaTrackDTOs = new HashMap<>();

    @Inject
    HazelcastMaps hazelcastMaps;

    @Inject
    DTOGenerators dtoGenerators;

    @Inject
    RandomGenerators randomGenerators;

    @PostConstruct
    void setup() {

    }

    void generate() {
        Random random = new Random();
        this.callDTO = this.dtoGenerators.getCallDTO();
        this.hazelcastMaps.getCalls().put(this.callDTO.callId, this.callDTO);
        int clientsNum = random.nextInt(8) + 2;
        List<MediaTrackDTO> outboundMediaTrackDTOs = new LinkedList<>();
        List<MediaTrackDTO> inboundMediaTrackDTOs = new LinkedList<>();
//        clientsNum = 2;
        for (int i = 0; i < clientsNum; ++i) {
            var clientDTO = this.dtoGenerators.getClientDTOBuilder().withCallId(callDTO.callId).build();
            this.hazelcastMaps.getClients().put(clientDTO.clientId, clientDTO);
            this.hazelcastMaps.getCallToClientIds().put(clientDTO.callId, clientDTO.clientId);
            this.clientDTOs.put(clientDTO.clientId, clientDTO);
            int peerConnectionsNum = random.nextInt(2) + 1;
//            peerConnectionsNum = 1;
            for (int j = 0; j < peerConnectionsNum; ++j) {
                var peerConnectionDTO = this.dtoGenerators.getPeerConnectionDTOBuilder().withClientId(clientDTO.clientId).build();
                this.hazelcastMaps.getPeerConnections().put(peerConnectionDTO.peerConnectionId, peerConnectionDTO);
                this.hazelcastMaps.getClientToPeerConnectionIds().put(peerConnectionDTO.clientId, peerConnectionDTO.peerConnectionId);
                this.peerConnectionDTOs.put(peerConnectionDTO.peerConnectionId, peerConnectionDTO);
                int mediaTracksNum = random.nextInt(10) + 1;
//                mediaTracksNum = 2;
                for (int k = 0; k < mediaTracksNum; ++k) {
                    var mediaTrackDTO = this.dtoGenerators.getMediaTrackDTOBuilder()
                            .withCallId(this.callDTO.callId)
                            .withClientId(clientDTO.clientId)
                            .withPeerConnectionId(peerConnectionDTO.peerConnectionId).build();
                    mediaTrackDTO.direction =  k % 2 == 0 ? StreamDirection.OUTBOUND : StreamDirection.INBOUND;
                    this.hazelcastMaps.getMediaTracks().put(mediaTrackDTO.trackId, mediaTrackDTO);
                    switch (mediaTrackDTO.direction) {
                        case OUTBOUND:
                            this.hazelcastMaps.getPeerConnectionToOutboundTrackIds().put(mediaTrackDTO.peerConnectionId,mediaTrackDTO.trackId);
                            outboundMediaTrackDTOs.add(mediaTrackDTO);
                            break;
                        case INBOUND:
                            this.hazelcastMaps.getPeerConnectionToInboundTrackIds().put(mediaTrackDTO.peerConnectionId, mediaTrackDTO.trackId);
                            inboundMediaTrackDTOs.add(mediaTrackDTO);
                            break;
                    }
                    this.mediaTrackDTOs.put(mediaTrackDTO.trackId, mediaTrackDTO);
                }
            }
        }
        Iterator<MediaTrackDTO> it = inboundMediaTrackDTOs.iterator();
        while(it.hasNext()) {
            var inboundMediaTrackDTO = it.next();
            int tried = 0;
            for (; tried < 100; ++tried) {
                var outboundMediaTrackDTO = this.randomGenerators.getRandomFromList(outboundMediaTrackDTOs);
                if (outboundMediaTrackDTO.peerConnectionId == inboundMediaTrackDTO.peerConnectionId ||
                    outboundMediaTrackDTO.clientId == inboundMediaTrackDTO.clientId) {
                    continue;
                }
                inboundMediaTrackDTO.ssrc = outboundMediaTrackDTO.ssrc;
                this.hazelcastMaps.getMediaTracks().put(inboundMediaTrackDTO.trackId, inboundMediaTrackDTO);
                break;
            }
        }
    }

    public CallDTO getCallDTO() {
        return this.callDTO;
    }

    public Map<UUID, ClientDTO> getClientDTOs() {
        return Collections.unmodifiableMap(this.clientDTOs);
    }


    public Map<UUID, PeerConnectionDTO> getPeerConnectionDTOs() {
        return Collections.unmodifiableMap(this.peerConnectionDTOs);
    }


    public Map<UUID, MediaTrackDTO> getMediaTrackDTOs() {
        return Collections.unmodifiableMap(this.mediaTrackDTOs);
    }

}
