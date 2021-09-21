package org.observertc.webrtc.observer.entities;

import org.observertc.webrtc.observer.dto.CallDTOGenerator;
import org.observertc.webrtc.observer.dto.ClientDTOGenerator;
import org.observertc.webrtc.observer.dto.MediaTrackDTOGenerator;
import org.observertc.webrtc.observer.dto.PeerConnectionDTOGenerator;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Random;
import java.util.UUID;

@Singleton
public class EntitiesTestUtils {

    @Inject
    CallDTOGenerator callDTOGenerator;

    @Inject
    PeerConnectionDTOGenerator peerConnectionDTOGenerator;

    @Inject
    MediaTrackDTOGenerator mediaTrackDTOGenerator;

    @Inject
    ClientDTOGenerator clientDTOGenerator;

    private final Random random = new Random();

    public CallEntity getCallEntity() {
        var callDTO = this.callDTOGenerator.get();
        var result = CallEntity.builder().withCallDTO(callDTO);
        for (int i = 0, c = 1 + random.nextInt(1); i < c; ++i) {
            var clientEntity = this.getClientEntity(callDTO.callId);
            result.withClientEntity(clientEntity);
        }
        return result.build();
    }

    public ClientEntity getClientEntity() {
        UUID callId = UUID.randomUUID();
        return this.getClientEntity(
                callId
        );
    }

    private ClientEntity getClientEntity(UUID callId) {
        var clientDTO = this.clientDTOGenerator.withCallId(callId).get();
        var result = ClientEntity.builder()
                .withClientDTO(clientDTO);
        for (int i = 0, c = 1 + random.nextInt(1); i < c; ++i) {
            var peerConnectionEntity = this.getPeerConnectionEntity(callId, clientDTO.clientId);
            result.withPeerConnectionEntity(peerConnectionEntity);
        }
        return result.build();
    }

    public PeerConnectionEntity getPeerConnectionEntity() {
        UUID callId = UUID.randomUUID();
        UUID clientId = UUID.randomUUID();
        return this.getPeerConnectionEntity(
                callId,
                clientId
        );
    }

    private PeerConnectionEntity getPeerConnectionEntity(UUID callId, UUID clientId) {
        var peerConnectionDTO = this.peerConnectionDTOGenerator
                .withClientId(clientId).get();
        PeerConnectionEntity.Builder result = PeerConnectionEntity.builder().withPeerConnectionDTO(peerConnectionDTO);

        for (int i = 0, c = 1 + random.nextInt(4); i < c; ++i) {
            var trackDTO = this.mediaTrackDTOGenerator
                    .withCallId(callId)
                    .withClientId(clientId)
                    .withPeerConnectionId(peerConnectionDTO.peerConnectionId)
                    .get();
            switch(trackDTO.direction) {
                case INBOUND:
                    result.withInboundMediaTrackDTO(trackDTO);
                    break;
                case OUTBOUND:
                    result.withOutboundMediaTrackDTO(trackDTO);
                    break;
            }
        }
        return result.build();
    }

}
