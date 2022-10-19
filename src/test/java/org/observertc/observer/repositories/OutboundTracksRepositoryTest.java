package org.observertc.observer.repositories;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.*;
import org.observertc.observer.configs.MediaKind;
import org.observertc.observer.samples.ServiceRoomId;
import org.observertc.observer.utils.ModelsMapGenerator;

import java.util.List;

@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class OutboundTracksRepositoryTest {

    @Inject
    CallsRepository callsRepository;

    @Inject
    ClientsRepository clientsRepository;

    @Inject
    PeerConnectionsRepository peerConnectionsRepository;
//
    @Inject
    OutboundTracksRepository outboundTracksRepository;

    private ModelsMapGenerator modelsMapGenerator = new ModelsMapGenerator().generateP2pCase();

    @Test
    @Order(1)
    @DisplayName("Setup for scenario: Insert a call, clients, and peer connections")
    void test_1() {
        var callModel = modelsMapGenerator.getCallModel();
        var serviceRoomId = ServiceRoomId.make(callModel.getServiceId(), callModel.getRoomId());
        var alreadyInsertedCalls = this.callsRepository.insertAll(List.of(
                new CallsRepository.CreateCallInfo(
                        serviceRoomId,
                        callModel.hasMarker() ? callModel.getMarker() : null,
                        callModel.getCallId(),
                        callModel.getStarted()
                )
        ));
        Assertions.assertEquals(0, alreadyInsertedCalls.size());

        var call = this.callsRepository.get(serviceRoomId);
        var clientModels = modelsMapGenerator.getClientModels();
        for (var clientModel : clientModels.values()) {
            call.addClient(
                    clientModel.getClientId(),
                    clientModel.getUserId(),
                    clientModel.getMediaUnitId(),
                    clientModel.getTimeZoneId(),
                    clientModel.getJoined(),
                    clientModel.hasMarker() ? clientModel.getMarker() : null
            );
        }

        var peerConnectionModels = modelsMapGenerator.getPeerConnectionModels();
        for (var peerConnectionModel : peerConnectionModels.values()) {
            var client = call.getClient(peerConnectionModel.getClientId());
            client.addPeerConnection(
                    peerConnectionModel.getPeerConnectionId(),
                    peerConnectionModel.getOpened(),
                    peerConnectionModel.hasMarker() ? peerConnectionModel.getMarker() : null
            );
        }

        var peerConnections = this.peerConnectionsRepository.getAll(peerConnectionModels.keySet());
        var outboundTrackModels = modelsMapGenerator.getOutboundTrackModels();
        for (var outboundTrackModel : outboundTrackModels.values()) {
            var peerConnection = peerConnections.get(outboundTrackModel.getPeerConnectionId());
            peerConnection.addOutboundTrack(
                    outboundTrackModel.getTrackId(),
                    outboundTrackModel.getAdded(),
                    outboundTrackModel.hasSfuStreamId() ? outboundTrackModel.getSfuStreamId() : null,
                    MediaKind.valueOf(outboundTrackModel.getKind()),
                    outboundTrackModel.getSsrc(0),
                    outboundTrackModel.hasMarker() ? outboundTrackModel.getMarker() : null
            );
        }
        this.callsRepository.save();

        Assertions.assertEquals(clientModels.size(), this.clientsRepository.getAll(clientModels.keySet()).size());
        Assertions.assertEquals(peerConnectionModels.size(), this.peerConnectionsRepository.getAll(peerConnectionModels.keySet()).size());
    }


    @Test
    @Order(2)
    @DisplayName("Outbound Tracks can be fetched through peerConnections repository and they inserted with the proper values")
    void test_2() {
        var outboundTrackModels = modelsMapGenerator.getOutboundTrackModels();

        for (var outboundTrackModel : outboundTrackModels.values()) {
            var peerConnection = this.peerConnectionsRepository.get(outboundTrackModel.getPeerConnectionId());
            var outboundTrack = this.outboundTracksRepository.get(outboundTrackModel.getTrackId());

            Assertions.assertEquals(peerConnection.getClientId(), outboundTrack.getClientId());
            Assertions.assertTrue(peerConnection.hasOutboundTrack(outboundTrack.getTrackId()));

            Assertions.assertEquals(outboundTrackModel.getServiceId(), outboundTrack.getServiceId());
            Assertions.assertEquals(outboundTrackModel.getRoomId(), outboundTrack.getRoomId());
            Assertions.assertEquals(outboundTrackModel.getCallId(), outboundTrack.getCallId());
            Assertions.assertEquals(outboundTrackModel.getUserId(), outboundTrack.getUserId());
            Assertions.assertEquals(outboundTrackModel.getClientId(), outboundTrack.getClientId());
            Assertions.assertEquals(outboundTrackModel.getPeerConnectionId(), outboundTrack.getPeerConnectionId());
            Assertions.assertEquals(outboundTrackModel.getAdded(), outboundTrack.getAdded());

            // after insert touched should be equal to joined
            Assertions.assertEquals(outboundTrackModel.getAdded(), outboundTrack.getTouched());

            Assertions.assertEquals(outboundTrackModel.getMediaUnitId(), outboundTrack.getMediaUnitId());

            // marker
            if (outboundTrackModel.hasMarker()) {
                Assertions.assertEquals(outboundTrackModel.getMarker(), outboundTrack.getMarker());
            } else {
                Assertions.assertTrue(outboundTrack.getMarker().isBlank());
            }

            // sfu stream id
            if (outboundTrackModel.hasSfuStreamId()) {
                Assertions.assertEquals(outboundTrackModel.getSfuStreamId(), outboundTrack.getSfuStreamId());
            } else {
                Assertions.assertNull(outboundTrack.getSfuStreamId());
            }

        }
    }
}