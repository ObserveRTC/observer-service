package org.observertc.observer.utils;

import org.observertc.observer.samples.ServiceRoomId;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class DTOMapGenerator {
    private static final String ALICE_USER_ID = "Alice";
    private static final String BOB_USER_ID = "Bob";
    private static final String ASIA_SFU_MEDIA_UNIT_ID = "asia-sfu";

    private CallDTO callDTO;
    private Map<UUID, ClientDTO> clientDTOs = new HashMap<>();
    private Map<UUID, PeerConnectionDTO> peerConnectionDTOs = new HashMap<>();
    private Map<UUID, MediaTrackDTO> mediaTrackDTOs = new HashMap<>();
    private Map<UUID, SfuDTO> sfuDTOs = new HashMap<>();
    private Map<UUID, SfuTransportDTO> sfuTransports = new HashMap<>();
    private Map<UUID, SfuRtpPadDTO> sfuRtpPads = new HashMap<>();

    ModelsGenerator modelsGenerator = new ModelsGenerator();

    RandomGenerators randomGenerators = new RandomGenerators();

    public DTOMapGenerator saveTo(HamokStorages hamokStorages) {
        ServiceRoomId serviceRoomId = ServiceRoomId.make(callDTO.serviceId, callDTO.roomId);
        hamokStorages.getServiceRoomToCallIds().put(serviceRoomId.getKey(), callDTO.callId);
        hamokStorages.getCalls().put(this.callDTO.callId, this.callDTO);

        // clients
        this.clientDTOs.values().stream().forEach(clientDTO -> {
            hamokStorages.getCallToClientIds().put(clientDTO.callId, clientDTO.clientId);
        });
        hamokStorages.getClients().putAll(this.clientDTOs);

        // peer connections
        this.peerConnectionDTOs.values().stream().forEach(peerConnectionDTO -> {
            hamokStorages.getClientToPeerConnectionIds().put(peerConnectionDTO.clientId, peerConnectionDTO.peerConnectionId);
        });
        hamokStorages.getPeerConnections().putAll(this.peerConnectionDTOs);

        // media tracks
        this.mediaTrackDTOs.values().stream().forEach(mediaTrackDTO -> {
            switch (mediaTrackDTO.direction) {
                case INBOUND:
                    hamokStorages.getPeerConnectionToInboundTrackIds().put(mediaTrackDTO.peerConnectionId, mediaTrackDTO.trackId);
                    break;
                case OUTBOUND:
                    hamokStorages.getPeerConnectionToOutboundTrackIds().put(mediaTrackDTO.peerConnectionId, mediaTrackDTO.trackId);
                    break;
            }
        });
        hamokStorages.getMediaTracks().putAll(this.mediaTrackDTOs);

        if (this.sfuDTOs.size() < 1) {
            return this;
        }
        hamokStorages.getSFUs().putAll(this.sfuDTOs);
        this.sfuTransports.values().stream().forEach(sfuTransportDTO -> {
            hamokStorages.getSfuToSfuTransportIds().put(sfuTransportDTO.sfuId, sfuTransportDTO.transportId);
        });
        hamokStorages.getSFUTransports().putAll(this.sfuTransports);

        this.sfuRtpPads.values().stream().forEach(sfuRtpPadDTO -> {
            hamokStorages.getSfuTransportToSfuRtpPadIds().put(sfuRtpPadDTO.transportId, sfuRtpPadDTO.rtpPadId);
        });
        hamokStorages.getSFURtpPads().putAll(this.sfuRtpPads);
        return this;
    }

    public DTOMapGenerator deleteFrom(HamokStorages hamokStorages) {
        hamokStorages.getCalls().remove(this.callDTO.callId);

        // clients
        this.clientDTOs.values().stream().forEach(clientDTO -> {
            hamokStorages.getCallToClientIds().remove(clientDTO.callId, clientDTO.clientId);
        });
        this.clientDTOs.keySet().stream().forEach(hamokStorages.getClients()::remove);

        // peer connections
        this.peerConnectionDTOs.values().stream().forEach(peerConnectionDTO -> {
            hamokStorages.getClientToPeerConnectionIds().remove(peerConnectionDTO.clientId, peerConnectionDTO.peerConnectionId);
        });
        this.peerConnectionDTOs.keySet().stream().forEach(hamokStorages.getPeerConnections()::remove);

        // media tracks
        this.mediaTrackDTOs.values().stream().forEach(mediaTrackDTO -> {
            switch (mediaTrackDTO.direction) {
                case INBOUND:
                    hamokStorages.getPeerConnectionToInboundTrackIds().remove(mediaTrackDTO.peerConnectionId, mediaTrackDTO.trackId);
                    break;
                case OUTBOUND:
                    hamokStorages.getPeerConnectionToOutboundTrackIds().remove(mediaTrackDTO.peerConnectionId, mediaTrackDTO.trackId);
                    break;
            }
        });
        this.mediaTrackDTOs.keySet().stream().forEach(hamokStorages.getMediaTracks()::remove);

        if (this.sfuDTOs.size() < 1) {
            return this;
        }
        this.sfuDTOs.keySet().stream().forEach(hamokStorages.getSFUs()::remove);
        this.sfuTransports.keySet().stream().forEach(hamokStorages.getSFUTransports()::remove);
        this.sfuRtpPads.keySet().stream().forEach(hamokStorages.getSFURtpPads()::remove);
        return this;
    }

    public DTOMapGenerator generateP2pCase() {
        if (Objects.nonNull(this.callDTO)) throw new RuntimeException("cannot generate two calls");
        this.callDTO = this.modelsGenerator.getCallDTO();
        var alice = this.generateClientSide(ALICE_USER_ID);
        var bob = this.generateClientSide(BOB_USER_ID);
        alice.inboundTrack.ssrc = bob.outboundTrack.ssrc;
        bob.inboundTrack.ssrc = alice.outboundTrack.ssrc;
        this.putClientSides(alice, bob);
        return this;
    }

    public DTOMapGenerator generateSingleSfuCase() {
        if (Objects.nonNull(this.callDTO)) throw new RuntimeException("cannot generate two calls");
        this.callDTO = this.modelsGenerator.getCallDTO();
        var alice = this.generateClientSide(ALICE_USER_ID);
        var bob = this.generateClientSide(BOB_USER_ID);
        var asiaSFU = this.generateSFUSide(ASIA_SFU_MEDIA_UNIT_ID);
        alice.outboundTrack.sfuStreamId = asiaSFU.alice_to_sfu_inboundRtpPad.streamId;
        alice.outboundTrack.sfuSinkId = null;
        alice.inboundTrack.sfuStreamId = asiaSFU.sfu_to_alice_outboundRtpPad.streamId;
        alice.inboundTrack.sfuSinkId = asiaSFU.sfu_to_alice_outboundRtpPad.sinkId;

        bob.outboundTrack.sfuStreamId = asiaSFU.bob_to_sfu_inboundRtpPad.streamId;
        bob.outboundTrack.sfuSinkId = null;
        bob.inboundTrack.sfuStreamId = asiaSFU.sfu_to_bob_outboundRtpPad.streamId;
        bob.inboundTrack.sfuSinkId = asiaSFU.sfu_to_bob_outboundRtpPad.sinkId;
        this.putClientSides(alice, bob);
        this.putSfuSides(asiaSFU);
        return this;
    }




    public DTOMapGenerator generateMultipleSfu() {
        if (Objects.nonNull(this.callDTO)) throw new RuntimeException("cannot generate two calls");
        throw new RuntimeException("Not implemented");
    }

    public CallDTO getCallDTO() {
        return this.callDTO;
    }

    public Map<UUID, ClientDTO> getClientDTOs() {
        return this.clientDTOs;
    }
    public Map<UUID, PeerConnectionDTO> getPeerConnectionDTOs() {
        return this.peerConnectionDTOs;
    }
    public Map<UUID, MediaTrackDTO> getMediaTrackDTOs() {
        return this.mediaTrackDTOs;
    }
    public Map<UUID, SfuDTO> getSfuDTOs() {
        return this.sfuDTOs;
    }
    public Map<UUID, SfuTransportDTO> getSfuTransports() {
        return this.sfuTransports;
    }
    public Map<UUID, SfuRtpPadDTO> getSfuRtpPads() {
        return this.sfuRtpPads;
    }

    private void putSfuSides(SfuSide... sfuSides) {
        for (var sfuSide : sfuSides) {
            this.sfuDTOs.put(sfuSide.sfu.sfuId, sfuSide.sfu);
            this.sfuTransports.put(sfuSide.alice_transport.transportId, sfuSide.alice_transport);
            this.sfuTransports.put(sfuSide.bob_transport.transportId, sfuSide.bob_transport);
            this.sfuRtpPads.put(sfuSide.alice_to_sfu_inboundRtpPad.rtpPadId, sfuSide.alice_to_sfu_inboundRtpPad);
            this.sfuRtpPads.put(sfuSide.sfu_to_alice_outboundRtpPad.rtpPadId, sfuSide.sfu_to_alice_outboundRtpPad);
            this.sfuRtpPads.put(sfuSide.bob_to_sfu_inboundRtpPad.rtpPadId, sfuSide.bob_to_sfu_inboundRtpPad);
            this.sfuRtpPads.put(sfuSide.sfu_to_bob_outboundRtpPad.rtpPadId, sfuSide.sfu_to_bob_outboundRtpPad);
        }
    }

    private class SfuSide {
        final SfuDTO sfu;
        final SfuTransportDTO alice_transport;
        final SfuTransportDTO bob_transport;
        final SfuRtpPadDTO alice_to_sfu_inboundRtpPad;
        final SfuRtpPadDTO sfu_to_alice_outboundRtpPad;
        final SfuRtpPadDTO bob_to_sfu_inboundRtpPad;
        final SfuRtpPadDTO sfu_to_bob_outboundRtpPad;

        private SfuSide(SfuDTO sfu,
                        SfuTransportDTO alice_transport,
                        SfuTransportDTO bob_transport,
                        SfuRtpPadDTO alice_to_sfu_inboundRtpPad,
                        SfuRtpPadDTO sfu_to_alice_outboundRtpPad,
                        SfuRtpPadDTO bob_to_sfu_inboundRtpPad,
                        SfuRtpPadDTO sfu_to_bob_outboundRtpPad
        ) {
            this.sfu = sfu;
            this.alice_transport = alice_transport;
            this.bob_transport = bob_transport;
            this.alice_to_sfu_inboundRtpPad = alice_to_sfu_inboundRtpPad;
            this.sfu_to_alice_outboundRtpPad = sfu_to_alice_outboundRtpPad;
            this.bob_to_sfu_inboundRtpPad = bob_to_sfu_inboundRtpPad;
            this.sfu_to_bob_outboundRtpPad = sfu_to_bob_outboundRtpPad;
        }
    }

    private ClientSide generateClientSide(String userId) {
        if (Objects.isNull(callDTO)) throw new RuntimeException("Cannot generate client side without callDTO");
        var client = this.modelsGenerator.getClientDTOBuilderFromCallDTO(callDTO)
                .withUserId(userId)
                .build();
        var peerConnection = this.modelsGenerator.getPeerConnectionDTOFromClientDTO(client);
        var outboundTrack = this.modelsGenerator.getMediaTrackDTOBuilderFromPeerConnectionDTO(peerConnection)
                .withDirection(StreamDirection.OUTBOUND)
                .build();
        var inboundTrack = this.modelsGenerator.getMediaTrackDTOBuilderFromPeerConnectionDTO(peerConnection)
                .withDirection(StreamDirection.INBOUND)
                .build();
        var result = new ClientSide(client, peerConnection, outboundTrack, inboundTrack);
        return result;
    }

    private SfuSide generateSFUSide(String mediaUnitId) {
        if (Objects.isNull(callDTO)) throw new RuntimeException("Cannot generate client side without callDTO");
        var sfu = this.modelsGenerator.getSfuDTOBuilder()
                .withServiceId(callDTO.serviceId)
                .withMediaUnitId(mediaUnitId)
                .build();
        var alice_transport = this.modelsGenerator.getSfuTransportDTOBuilderFromSfuDTO(sfu)
                .build();
        var bob_transport = this.modelsGenerator.getSfuTransportDTOBuilderFromSfuDTO(sfu)
                .build();
        UUID alice_stream = UUID.randomUUID();
        UUID bob_stream = UUID.randomUUID();
        UUID alice_sink = UUID.randomUUID();
        UUID bob_sink = UUID.randomUUID();
        var alice_to_sfu_inboundRtpPad = this.modelsGenerator.getSfuRtpPadDTOBuilderFromSfuTransportDTO(alice_transport)
                .withStreamDirection(StreamDirection.INBOUND)
                .withStreamId(alice_stream)
                .withSinkId(null)
                .build();
        var bob_to_sfu_inboundRtpPad = this.modelsGenerator.getSfuRtpPadDTOBuilderFromSfuTransportDTO(bob_transport)
                .withStreamDirection(StreamDirection.INBOUND)
                .withStreamId(bob_stream)
                .withSinkId(null)
                .build();
        var sfu_to_alice_outboundRtpPad = this.modelsGenerator.getSfuRtpPadDTOBuilderFromSfuTransportDTO(alice_transport)
                .withStreamDirection(StreamDirection.OUTBOUND)
                .withStreamId(bob_stream)
                .withSinkId(bob_sink)
                .build();
        var sfu_to_bob_outboundRtpPad = this.modelsGenerator.getSfuRtpPadDTOBuilderFromSfuTransportDTO(bob_transport)
                .withStreamDirection(StreamDirection.OUTBOUND)
                .withStreamId(alice_stream)
                .withSinkId(alice_sink)
                .build();
        var result = new SfuSide(
                sfu,
                alice_transport,
                bob_transport,
                alice_to_sfu_inboundRtpPad,
                sfu_to_alice_outboundRtpPad,
                bob_to_sfu_inboundRtpPad,
                sfu_to_bob_outboundRtpPad
        );
        return result;
    }

    private void putClientSides(ClientSide... clientSides) {
        for (var clientSide : clientSides) {
            this.clientDTOs.put(clientSide.clientDTO.clientId, clientSide.clientDTO);
            this.peerConnectionDTOs.put(clientSide.peerConnectionDTO.peerConnectionId, clientSide.peerConnectionDTO);
            this.mediaTrackDTOs.put(clientSide.outboundTrack.trackId, clientSide.outboundTrack);
            this.mediaTrackDTOs.put(clientSide.inboundTrack.trackId, clientSide.inboundTrack);
        }
    }

    private class ClientSide {
        final ClientDTO clientDTO;
        final PeerConnectionDTO peerConnectionDTO;
        final MediaTrackDTO outboundTrack;
        final MediaTrackDTO inboundTrack;

        private ClientSide(ClientDTO clientDTO, PeerConnectionDTO peerConnectionDTO, MediaTrackDTO outboundTrack, MediaTrackDTO inboundTrack) {
            this.clientDTO = clientDTO;
            this.peerConnectionDTO = peerConnectionDTO;
            this.outboundTrack = outboundTrack;
            this.inboundTrack = inboundTrack;
        }
    }
}
