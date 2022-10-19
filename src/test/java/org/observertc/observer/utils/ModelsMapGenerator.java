package org.observertc.observer.utils;

import org.observertc.observer.configs.MediaKind;
import org.observertc.observer.repositories.CallsRepository;
import org.observertc.observer.repositories.HamokStorages;
import org.observertc.observer.samples.ServiceRoomId;
import org.observertc.schemas.dtos.Models;

import java.util.*;

public class ModelsMapGenerator {
    private static final String ALICE_USER_ID = "Alice";
    private static final String BOB_USER_ID = "Bob";
    private static final String ASIA_SFU_MEDIA_UNIT_ID = "asia-sfu";

    private Models.Call callDTO;
    private Map<String, Models.Client> clientDTOs = new HashMap<>();
    private Map<String, Models.PeerConnection> peerConnectionDTOs = new HashMap<>();
    private Map<String, Models.InboundTrack> inboundTracks = new HashMap<>();
    private Map<String, Models.OutboundTrack> outboundTracks = new HashMap<>();
    private Map<String, Models.Sfu> sfuDTOs = new HashMap<>();
    private Map<String, Models.SfuTransport> sfuTransports = new HashMap<>();
    private Map<String, Models.SfuInboundRtpPad> sfuInboundRtpPads = new HashMap<>();
    private Map<String, Models.SfuOutboundRtpPad> sfuOutboundRtpPads = new HashMap<>();

    ModelsGenerator modelsGenerator = new ModelsGenerator();

    RandomGenerators randomGenerators = new RandomGenerators();

    public ModelsMapGenerator saveTo(HamokStorages hamokStorages) {
        ServiceRoomId serviceRoomId = ServiceRoomId.make(callDTO.getServiceId(), callDTO.getRoomId());
        hamokStorages.getCallsRepository().insertAll(List.of(
                new CallsRepository.CreateCallInfo(
                        serviceRoomId,
                        "test",
                        this.callDTO.getCallId(),
                        this.callDTO.getStarted()
                )
        ));
        var call = hamokStorages.getCallsRepository().get(serviceRoomId);

        // clients

        this.clientDTOs.values().stream().forEach(clientModel -> {
            call.addClient(
                    clientModel.getClientId(),
                    clientModel.getUserId(),
                    clientModel.getMediaUnitId(),
                    clientModel.getTimeZoneId(),
                    clientModel.getJoined(),
                    clientModel.getMarker()
            );
        });

        var clients = call.getClients();

        // peer connections
        this.peerConnectionDTOs.values().stream().forEach(peerConnectionModel -> {
            var client = clients.get(peerConnectionModel.getClientId());
            client.addPeerConnection(
                    peerConnectionModel.getPeerConnectionId(),
                    peerConnectionModel.getOpened(),
                    peerConnectionModel.getMarker()
            );
        });
        var peerConnections = hamokStorages.getPeerConnectionsRepository().getAll(this.peerConnectionDTOs.keySet());

        // media tracks
        this.inboundTracks.values().stream().forEach(inboundTrackModel -> {
            var peerConnection = peerConnections.get(inboundTrackModel.getPeerConnectionId());
            peerConnection.addInboundTrack(
                    inboundTrackModel.getTrackId(),
                    inboundTrackModel.getAdded(),
                    inboundTrackModel.getSfuStreamId(),
                    inboundTrackModel.getSfuSinkId(),
                    MediaKind.valueOf(inboundTrackModel.getKind()),
                    inboundTrackModel.getSsrc(0),
                    inboundTrackModel.getMarker()
            );
        });

        this.outboundTracks.values().stream().forEach(outboundTrackModel -> {
            var peerConnection = peerConnections.get(outboundTrackModel.getPeerConnectionId());
            peerConnection.addOutboundTrack(
                    outboundTrackModel.getTrackId(),
                    outboundTrackModel.getAdded(),
                    outboundTrackModel.getSfuStreamId(),
                    MediaKind.valueOf(outboundTrackModel.getKind()),
                    outboundTrackModel.getSsrc(0),
                    outboundTrackModel.getMarker()
            );
        });

        if (this.sfuDTOs.size() < 1) {
            hamokStorages.getCallsRepository().save();
            hamokStorages.getSfusRepository().save();
            return this;
        }
        this.sfuDTOs.values().forEach(sfuModel -> {
            hamokStorages.getSfusRepository().add(
                    sfuModel.getServiceId(),
                    sfuModel.getMediaUnitId(),
                    sfuModel.getSfuId(),
                    sfuModel.getJoined(),
                    sfuModel.getTimeZoneId(),
                    sfuModel.getMarker()
            );
        });

        var sfus = hamokStorages.getSfusRepository().getAll(this.sfuDTOs.keySet());

        this.sfuTransports.values().forEach(sfuTransport -> {
            var sfu = sfus.get(sfuTransport.getSfuId());
            sfu.addSfuTransport(
                    sfuTransport.getTransportId(),
                    sfuTransport.getInternal(),
                    sfuTransport.getOpened(),
                    sfuTransport.getMarker()
            );
        });

        var sfuTransports = hamokStorages.getSfuTransportsRepository().getAll(this.sfuTransports.keySet());
        this.sfuInboundRtpPads.values().forEach(sfuInboundRtpPad -> {
            var sfuTransport = sfuTransports.get(sfuInboundRtpPad.getSfuTransportId());
            sfuTransport.addInboundRtpPad(
                    sfuInboundRtpPad.getRtpPadId(),
                    sfuInboundRtpPad.getSsrc(),
                    sfuInboundRtpPad.getSfuStreamId(),
                    sfuInboundRtpPad.getAdded(),
                    sfuInboundRtpPad.getMarker()
            );
        });
        this.sfuOutboundRtpPads.values().forEach(sfuOutboundRtpPad -> {
            var sfuTransport = sfuTransports.get(sfuOutboundRtpPad.getSfuTransportId());
            sfuTransport.addOutboundRtpPad(
                    sfuOutboundRtpPad.getRtpPadId(),
                    sfuOutboundRtpPad.getSsrc(),
                    sfuOutboundRtpPad.getSfuStreamId(),
                    sfuOutboundRtpPad.getSfuSinkId(),
                    sfuOutboundRtpPad.getAdded(),
                    sfuOutboundRtpPad.getMarker()
            );
        });
        hamokStorages.getCallsRepository().save();
        hamokStorages.getSfusRepository().save();
        return this;
    }

    public ModelsMapGenerator deleteFrom(HamokStorages hamokStorages) {
        if (this.callDTO != null) {
            ServiceRoomId serviceRoomId = ServiceRoomId.make(this.callDTO.getServiceId(), this.callDTO.getRoomId());
            hamokStorages.getCallsRepository().removeAll(Set.of(serviceRoomId));
        }

        if (this.sfuDTOs.size() < 1) {
            this.sfuDTOs.keySet().stream().forEach(hamokStorages.getSfusRepository()::remove);
        }
        hamokStorages.getCallsRepository().save();
        hamokStorages.getSfusRepository().save();
        return this;
    }

    public ModelsMapGenerator generateP2pCase() {
        if (Objects.nonNull(this.callDTO)) throw new RuntimeException("cannot generate two calls");
        this.callDTO = this.modelsGenerator.getCallModel();
        var alice = this.generateClientSide(ALICE_USER_ID);
        var bob = this.generateClientSide(BOB_USER_ID);
        alice.inboundTrack = Models.InboundTrack.newBuilder(alice.inboundTrack)
                .clearSfuSinkId()
                .clearSfuStreamId()
                .clearSsrc()
                .addSsrc(bob.outboundTrack.getSsrc(0))
                .build();
        alice.outboundTrack = Models.OutboundTrack.newBuilder(alice.outboundTrack)
                .clearSfuStreamId()
                .build();
        bob.inboundTrack = Models.InboundTrack.newBuilder(bob.inboundTrack)
                .clearSsrc()
                .clearSfuSinkId()
                .clearSfuStreamId()
                .addSsrc(alice.outboundTrack.getSsrc(0))
                .build();
        bob.outboundTrack = Models.OutboundTrack.newBuilder(bob.outboundTrack)
                .clearSfuStreamId()
                .build();
        this.putClientSides(alice, bob);
        return this;
    }

    public ModelsMapGenerator generateSingleSfuCase() {
        if (Objects.nonNull(this.callDTO)) throw new RuntimeException("cannot generate two calls");
        this.callDTO = this.modelsGenerator.getCallModel();
        var alice = this.generateClientSide(ALICE_USER_ID);
        var bob = this.generateClientSide(BOB_USER_ID);
        var asiaSFU = this.generateSFUSide(ASIA_SFU_MEDIA_UNIT_ID);
        alice.outboundTrack = Models.OutboundTrack.newBuilder(alice.outboundTrack)
                .setSfuStreamId(asiaSFU.alice_to_sfu_inboundRtpPad.getSfuStreamId())
                .build();

        alice.inboundTrack = Models.InboundTrack.newBuilder(alice.inboundTrack)
                .setSfuStreamId(asiaSFU.sfu_to_alice_outboundRtpPad.getSfuStreamId())
                .setSfuSinkId(asiaSFU.sfu_to_alice_outboundRtpPad.getSfuSinkId())
                .build();

        bob.outboundTrack = Models.OutboundTrack.newBuilder(bob.outboundTrack)
                .setSfuStreamId(asiaSFU.bob_to_sfu_inboundRtpPad.getSfuStreamId())
                .build();
        bob.inboundTrack = Models.InboundTrack.newBuilder(bob.inboundTrack)
                .setSfuStreamId(asiaSFU.sfu_to_bob_outboundRtpPad.getSfuStreamId())
                .setSfuSinkId(asiaSFU.sfu_to_bob_outboundRtpPad.getSfuSinkId())
                .build();
        this.putClientSides(alice, bob);
        this.putSfuSides(asiaSFU);
        return this;
    }


    public ModelsMapGenerator generateMultipleSfu() {
        if (Objects.nonNull(this.callDTO)) throw new RuntimeException("cannot generate two calls");
        throw new RuntimeException("Not implemented");
    }

    public Models.Call getCallModel() {
        return this.callDTO;
    }

    public Map<String, Models.Client> getClientModels() {
        return this.clientDTOs;
    }
    public Map<String, Models.PeerConnection> getPeerConnectionModels() {
        return this.peerConnectionDTOs;
    }
    public Map<String, Models.InboundTrack> getInboundTrackModels() {
        return this.inboundTracks;
    }
    public Map<String, Models.OutboundTrack> getOutboundTrackModels() {
        return this.outboundTracks;
    }
    public Map<String, Models.Sfu> getSfuModels() {
        return this.sfuDTOs;
    }
    public Map<String, Models.SfuTransport> getSfuTransports() {
        return this.sfuTransports;
    }
    public Map<String, Models.SfuInboundRtpPad> getSfuInboundRtpPads() {
        return this.sfuInboundRtpPads;
    }
    public Map<String, Models.SfuOutboundRtpPad> getSfuOutboundRtpPads() {
        return this.sfuOutboundRtpPads;
    }

    private void putSfuSides(SfuSide... sfuSides) {
        for (var sfuSide : sfuSides) {
            this.sfuDTOs.put(sfuSide.sfu.getSfuId(), sfuSide.sfu);
            this.sfuTransports.put(sfuSide.alice_transport.getTransportId(), sfuSide.alice_transport);
            this.sfuTransports.put(sfuSide.bob_transport.getTransportId(), sfuSide.bob_transport);
            this.sfuInboundRtpPads.put(sfuSide.alice_to_sfu_inboundRtpPad.getRtpPadId(), sfuSide.alice_to_sfu_inboundRtpPad);
            this.sfuOutboundRtpPads.put(sfuSide.sfu_to_alice_outboundRtpPad.getRtpPadId(), sfuSide.sfu_to_alice_outboundRtpPad);
            this.sfuInboundRtpPads.put(sfuSide.bob_to_sfu_inboundRtpPad.getRtpPadId(), sfuSide.bob_to_sfu_inboundRtpPad);
            this.sfuOutboundRtpPads.put(sfuSide.sfu_to_bob_outboundRtpPad.getRtpPadId(), sfuSide.sfu_to_bob_outboundRtpPad);
        }
    }

    private class SfuSide {
        final Models.Sfu sfu;
        final Models.SfuTransport alice_transport;
        final Models.SfuTransport bob_transport;
        final Models.SfuInboundRtpPad alice_to_sfu_inboundRtpPad;
        final Models.SfuOutboundRtpPad sfu_to_alice_outboundRtpPad;
        final Models.SfuInboundRtpPad bob_to_sfu_inboundRtpPad;
        final Models.SfuOutboundRtpPad sfu_to_bob_outboundRtpPad;

        private SfuSide(Models.Sfu sfu,
                        Models.SfuTransport alice_transport,
                        Models.SfuTransport bob_transport,
                        Models.SfuInboundRtpPad alice_to_sfu_inboundRtpPad,
                        Models.SfuOutboundRtpPad sfu_to_alice_outboundRtpPad,
                        Models.SfuInboundRtpPad bob_to_sfu_inboundRtpPad,
                        Models.SfuOutboundRtpPad sfu_to_bob_outboundRtpPad
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
        var client = this.modelsGenerator.getClientModelBuilderFromCallModel(callDTO)
                .setUserId(userId)
                .build();

        var peerConnection = this.modelsGenerator.getPeerConnectionModelFromClientModel(client);
        var outboundTrack = this.modelsGenerator.getOutboundTrackBuilderFromPeerConnectionModel(peerConnection)
                .setKind(MediaKind.VIDEO.name())
                .build();
        var inboundTrack = this.modelsGenerator.getInboundTrackBuilderFromPeerConnectionModel(peerConnection)
                .setKind(MediaKind.VIDEO.name())
                .build();
        var result = new ClientSide(client, peerConnection, outboundTrack, inboundTrack);
        return result;
    }

    private SfuSide generateSFUSide(String mediaUnitId) {
        if (Objects.isNull(callDTO)) throw new RuntimeException("Cannot generate client side without callDTO");
        var sfu = this.modelsGenerator.getSfuModelBuilder()
                .setServiceId(callDTO.getServiceId())
                .setMediaUnitId(mediaUnitId)
                .build();
        var alice_transport = this.modelsGenerator.getSfuTransportModelBuilderFromSfuModel(sfu)
                .build();
        var bob_transport = this.modelsGenerator.getSfuTransportModelBuilderFromSfuModel(sfu)
                .build();
        var alice_stream = UUID.randomUUID().toString();
        var bob_stream = UUID.randomUUID().toString();
        var alice_sink = UUID.randomUUID().toString();
        var bob_sink = UUID.randomUUID().toString();
        var alice_to_sfu_inboundRtpPad = this.modelsGenerator.getSfuInboundRtpPadBuilderFromSfuTransportModel(alice_transport)
                .setSfuStreamId(alice_stream)
                .build();
        var bob_to_sfu_inboundRtpPad = this.modelsGenerator.getSfuInboundRtpPadBuilderFromSfuTransportModel(bob_transport)
                .setSfuStreamId(bob_stream)
                .build();
        var sfu_to_alice_outboundRtpPad = this.modelsGenerator.getSfuOutboundRtpPadBuilderFromSfuTransportModel(alice_transport)
                .setSfuStreamId(bob_stream)
                .setSfuSinkId(bob_sink)
                .build();
        var sfu_to_bob_outboundRtpPad = this.modelsGenerator.getSfuOutboundRtpPadBuilderFromSfuTransportModel(bob_transport)
                .setSfuStreamId(alice_stream)
                .setSfuSinkId(alice_sink)
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
            this.clientDTOs.put(clientSide.clientDTO.getClientId(), clientSide.clientDTO);
            this.peerConnectionDTOs.put(clientSide.peerConnectionDTO.getPeerConnectionId(), clientSide.peerConnectionDTO);
            this.outboundTracks.put(clientSide.outboundTrack.getTrackId(), clientSide.outboundTrack);
            this.inboundTracks.put(clientSide.inboundTrack.getTrackId(), clientSide.inboundTrack);
        }
    }

    private class ClientSide {
        Models.Client clientDTO;
        Models.PeerConnection peerConnectionDTO;
        Models.OutboundTrack outboundTrack;
        Models.InboundTrack inboundTrack;

        private ClientSide(Models.Client clientDTO, Models.PeerConnection peerConnectionDTO, Models.OutboundTrack outboundTrack, Models.InboundTrack inboundTrack) {
            this.clientDTO = clientDTO;
            this.peerConnectionDTO = peerConnectionDTO;
            this.outboundTrack = outboundTrack;
            this.inboundTrack = inboundTrack;
        }
    }
}
