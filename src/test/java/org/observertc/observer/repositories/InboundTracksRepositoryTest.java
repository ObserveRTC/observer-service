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
class InboundTracksRepositoryTest {

    @Inject
    CallsRepository callsRepository;

    @Inject
    ClientsRepository clientsRepository;

    @Inject
    PeerConnectionsRepository peerConnectionsRepository;
//
    @Inject
    InboundTracksRepository inboundTracksRepository;

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

        var call = this.callsRepository.get(callModel.getCallId());
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
        var inboundTrackModels = modelsMapGenerator.getInboundTrackModels();
        for (var inboundTrackModel : inboundTrackModels.values()) {
            var peerConnection = peerConnections.get(inboundTrackModel.getPeerConnectionId());
            peerConnection.addInboundTrack(
                    inboundTrackModel.getTrackId(),
                    inboundTrackModel.getAdded(),
                    inboundTrackModel.hasSfuStreamId() ? inboundTrackModel.getSfuStreamId() : null,
                    inboundTrackModel.hasSfuSinkId() ? inboundTrackModel.getSfuSinkId() : null,
                    MediaKind.valueOf(inboundTrackModel.getKind()),
                    inboundTrackModel.getSsrc(0),
                    inboundTrackModel.hasMarker() ? inboundTrackModel.getMarker() : null
            );
        }
        this.callsRepository.save();

        Assertions.assertEquals(clientModels.size(), this.clientsRepository.getAll(clientModels.keySet()).size());
        Assertions.assertEquals(peerConnectionModels.size(), this.peerConnectionsRepository.getAll(peerConnectionModels.keySet()).size());
    }


    @Test
    @Order(2)
    @DisplayName("Inbound Tracks can be fetched through peerConnections repository and they inserted with the proper values")
    void test_2() {
        var inboundTrackModels = modelsMapGenerator.getInboundTrackModels();

        for (var inboundTrackModel : inboundTrackModels.values()) {
            var peerConnection = this.peerConnectionsRepository.get(inboundTrackModel.getPeerConnectionId());
            var inboundTrack = this.inboundTracksRepository.get(inboundTrackModel.getTrackId());

            Assertions.assertEquals(peerConnection.getClientId(), inboundTrack.getClientId());
            Assertions.assertTrue(peerConnection.hasInboundTrack(inboundTrack.getTrackId()));

            Assertions.assertEquals(inboundTrackModel.getServiceId(), inboundTrack.getServiceId());
            Assertions.assertEquals(inboundTrackModel.getRoomId(), inboundTrack.getRoomId());
            Assertions.assertEquals(inboundTrackModel.getCallId(), inboundTrack.getCallId());
            Assertions.assertEquals(inboundTrackModel.getUserId(), inboundTrack.getUserId());
            Assertions.assertEquals(inboundTrackModel.getClientId(), inboundTrack.getClientId());
            Assertions.assertEquals(inboundTrackModel.getPeerConnectionId(), inboundTrack.getPeerConnectionId());
            Assertions.assertEquals(inboundTrackModel.getAdded(), inboundTrack.getAdded());

            // after insert touched should be equal to joined
            Assertions.assertEquals(inboundTrackModel.getAdded(), inboundTrack.getTouched());

            Assertions.assertEquals(inboundTrackModel.getMediaUnitId(), inboundTrack.getMediaUnitId());

            // marker
            if (inboundTrackModel.hasMarker()) {
                Assertions.assertEquals(inboundTrackModel.getMarker(), inboundTrack.getMarker());
            } else {
                Assertions.assertTrue(inboundTrack.getMarker().isBlank());
            }

            // sfu stream id
            if (inboundTrackModel.hasSfuStreamId()) {
                Assertions.assertEquals(inboundTrackModel.getSfuStreamId(), inboundTrack.getSfuStreamId());
            } else {
                Assertions.assertNull(inboundTrack.getSfuStreamId());
            }

            // sfu sink id
            if (inboundTrackModel.hasSfuSinkId()) {
                Assertions.assertEquals(inboundTrackModel.getSfuSinkId(), inboundTrack.getSfuSinkId());
            } else {
                Assertions.assertNull(inboundTrack.getSfuSinkId());
            }

        }
    }
}