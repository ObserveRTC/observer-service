package org.observertc.observer.repositories;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.*;
import org.observertc.observer.common.MediaKind;
import org.observertc.observer.samples.ServiceRoomId;
import org.observertc.observer.utils.ModelsMapGenerator;

import java.util.List;

@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class PeerConnectionRepositoryTest {

    @Inject
    CallsRepository callsRepository;

    @Inject
    ClientsRepository clientsRepository;

    @Inject
    PeerConnectionsRepository peerConnectionsRepository;
//
    @Inject
    InboundTracksRepository inboundTracksRepository;

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
                        callModel.getCallId()
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
        this.callsRepository.save();

        Assertions.assertEquals(clientModels.size(), this.clientsRepository.getAll(clientModels.keySet()).size());
        Assertions.assertEquals(peerConnectionModels.size(), this.peerConnectionsRepository.getAll(peerConnectionModels.keySet()).size());
    }


    @Test
    @Order(2)
    @DisplayName("Peer connections can be fetched through clients repository and they inserted with the proper values")
    void test_2() {
        var peerConnectionModels = modelsMapGenerator.getPeerConnectionModels();
        var peerConnections = this.peerConnectionsRepository.getAll(peerConnectionModels.keySet());

        for (var peerConnectionModel : peerConnectionModels.values()) {
            var client = this.clientsRepository.get(peerConnectionModel.getClientId());
            var peerConnection = this.peerConnectionsRepository.get(peerConnectionModel.getPeerConnectionId());

            Assertions.assertEquals(client.getClientId(), peerConnection.getClientId());
            Assertions.assertTrue(client.hasPeerConnection(peerConnection.getPeerConnectionId()));

            Assertions.assertEquals(peerConnectionModel.getServiceId(), peerConnection.getServiceId());
            Assertions.assertEquals(peerConnectionModel.getRoomId(), peerConnection.getRoomId());
            Assertions.assertEquals(peerConnectionModel.getCallId(), peerConnection.getCallId());
            Assertions.assertEquals(peerConnectionModel.getUserId(), peerConnection.getUserId());
            Assertions.assertEquals(peerConnectionModel.getClientId(), peerConnection.getClientId());
            Assertions.assertEquals(peerConnectionModel.getPeerConnectionId(), peerConnection.getPeerConnectionId());
            Assertions.assertEquals(peerConnectionModel.getOpened(), peerConnection.getOpened());

            // after insert touched should be equal to joined
            Assertions.assertEquals(peerConnectionModel.getOpened(), peerConnection.getTouched());

            Assertions.assertEquals(peerConnectionModel.getMediaUnitId(), peerConnection.getMediaUnitId());
            if (peerConnectionModel.hasMarker()) {
                Assertions.assertEquals(peerConnectionModel.getMarker(), peerConnection.getMarker());
            } else {
                Assertions.assertTrue(peerConnection.getMarker().isBlank());
            }
        }
    }

    @Test
    @Order(3)
    @DisplayName("PeerConnections can be fetched through clients")
    void test_3() {
        var peerConnectionModels = modelsMapGenerator.getPeerConnectionModels();

        for (var peerConnectionModel : peerConnectionModels.values()) {
            var client = this.clientsRepository.get(peerConnectionModel.getClientId());
            var peerConnection = this.peerConnectionsRepository.get(peerConnectionModel.getPeerConnectionId());

            Assertions.assertTrue(client.hasPeerConnection(peerConnection.getPeerConnectionId()));
            Assertions.assertEquals(client.getPeerConnection(peerConnection.getPeerConnectionId()), peerConnection);
        }
    }

    @Test
    @Order(4)
    @DisplayName("InboundTrack can be added and added InboundTracks have the proper value")
    void test_4() {
        var inboundTrackModels = modelsMapGenerator.getInboundTrackModels();
        var peerConnectionModels = modelsMapGenerator.getPeerConnectionModels();
        var peerConnections = this.peerConnectionsRepository.getAll(peerConnectionModels.keySet());

        for (var inboundTrackModel : inboundTrackModels.values()) {
            var peerConnection = peerConnections.get(inboundTrackModel.getPeerConnectionId());
            var addInboundTrack = peerConnection.addInboundTrack(
                    inboundTrackModel.getTrackId(),
                    inboundTrackModel.getAdded(),
                    inboundTrackModel.hasSfuStreamId() ? inboundTrackModel.getSfuStreamId() : null,
                    inboundTrackModel.hasSfuSinkId() ? inboundTrackModel.getSfuSinkId() : null,
                    MediaKind.valueOf(inboundTrackModel.getKind()),
                    inboundTrackModel.getSsrc(0),
                    inboundTrackModel.hasMarker() ? inboundTrackModel.getMarker() : null
            );

            Assertions.assertEquals(peerConnection.getServiceId(), addInboundTrack.getServiceId());
            Assertions.assertEquals(peerConnection.getRoomId(), addInboundTrack.getRoomId());
            Assertions.assertEquals(peerConnection.getCallId(), addInboundTrack.getCallId());
            Assertions.assertEquals(peerConnection.getUserId(), addInboundTrack.getUserId());
            Assertions.assertEquals(peerConnection.getClientId(), addInboundTrack.getClientId());
            Assertions.assertEquals(peerConnection.getPeerConnectionId(), addInboundTrack.getPeerConnectionId());

            Assertions.assertEquals(inboundTrackModel.getTrackId(), addInboundTrack.getTrackId());
            Assertions.assertEquals(inboundTrackModel.getAdded(), addInboundTrack.getAdded());
            Assertions.assertEquals(inboundTrackModel.getAdded(), addInboundTrack.getTouched());

            Assertions.assertEquals(peerConnection.getMediaUnitId(), addInboundTrack.getMediaUnitId());
            Assertions.assertEquals(inboundTrackModel.getMarker(), addInboundTrack.getMarker());

            Assertions.assertTrue(peerConnection.hasInboundTrack(inboundTrackModel.getTrackId()));
        }
        this.callsRepository.save();
    }

    @Test
    @Order(5)
    @DisplayName("InboundTracks can not be added twice")
    void test_5() {
        var inboundTrackModels = modelsMapGenerator.getInboundTrackModels();
        var peerConnectionModels = modelsMapGenerator.getPeerConnectionModels();
        var peerConnections = this.peerConnectionsRepository.getAll(peerConnectionModels.keySet());

        for (var inboundTrackModel : inboundTrackModels.values()) {
            var peerConnection = peerConnections.get(inboundTrackModel.getPeerConnectionId());
            Assertions.assertThrows(AlreadyCreatedException.class, () -> {
                peerConnection.addInboundTrack(
                        inboundTrackModel.getTrackId(),
                        inboundTrackModel.getAdded(),
                        inboundTrackModel.hasSfuStreamId() ? inboundTrackModel.getSfuStreamId() : null,
                        inboundTrackModel.hasSfuSinkId() ? inboundTrackModel.getSfuSinkId() : null,
                        MediaKind.valueOf(inboundTrackModel.getKind()),
                        inboundTrackModel.getSsrc(0),
                        inboundTrackModel.hasMarker() ? inboundTrackModel.getMarker() : null
                );
            });
        }
    }

    @Test
    @Order(6)
    @DisplayName("InboundTracks can be accessed through PeerConnections by getting its all inboundTracks")
    void test_6() {
        var inboundTrackModels = modelsMapGenerator.getInboundTrackModels();
        var peerConnectionModels = modelsMapGenerator.getPeerConnectionModels();
        var peerConnections = this.peerConnectionsRepository.getAll(peerConnectionModels.keySet());

        var totalAccessedInboundTrackIds = 0;
        for (var peerConnection : peerConnections.values()) {
            var inboundTracks = peerConnection.getInboundTracks();
            totalAccessedInboundTrackIds += inboundTracks.size();

            for (var inboundTrack : inboundTracks.values()) {
                Assertions.assertNotNull(inboundTrackModels.get(inboundTrack.getTrackId()));
            }
        }
        Assertions.assertEquals(totalAccessedInboundTrackIds, inboundTrackModels.size());
    }

    @Test
    @Order(7)
    @DisplayName("InboundTracks can be accessed through the PeerConnection by getting one by one")
    void test_7() {
        var inboundTrackModels = modelsMapGenerator.getInboundTrackModels();
        var peerConnectionModels = modelsMapGenerator.getPeerConnectionModels();
        var peerConnections = this.peerConnectionsRepository.getAll(peerConnectionModels.keySet());

        for (var inboundTrackModel : inboundTrackModels.values()) {
            var peerConnection = peerConnections.get(inboundTrackModel.getPeerConnectionId());
            Assertions.assertNotNull(peerConnection.getInboundTrack(inboundTrackModel.getTrackId()));
        }
    }

    @Test
    @Order(8)
    @DisplayName("InboundTracks can be accessed through the InboundTracks repository")
    void test_8() {
        var inboundTrackModels = modelsMapGenerator.getInboundTrackModels();
        var inboundTracks = this.inboundTracksRepository.getAll(inboundTrackModels.keySet());

        for (var inboundTrackModel : inboundTrackModels.values()) {
            Assertions.assertNotNull(inboundTracks.get(inboundTrackModel.getTrackId()));
        }
    }

    @Test
    @Order(9)
    @DisplayName("InboundTracks can be deleted through the PeerConnection")
    void test_9() {
        var inboundTrackModels = modelsMapGenerator.getInboundTrackModels();
        var peerConnectionModels = modelsMapGenerator.getPeerConnectionModels();
        var peerConnections = this.peerConnectionsRepository.getAll(peerConnectionModels.keySet());

        for (var inboundTrackModel : inboundTrackModels.values()) {
            var peerConnection = peerConnections.get(inboundTrackModel.getPeerConnectionId());
            var removed = peerConnection.removeInboundTrack(inboundTrackModel.getTrackId());
            Assertions.assertTrue(removed);

            var removedAgain = peerConnection.removeInboundTrack(inboundTrackModel.getTrackId());
            Assertions.assertFalse(removedAgain);
        }
        this.callsRepository.save();
    }

    @Test
    @Order(10)
    @DisplayName("Removed inbound tracks are not available though inbound tracks repo")
    void test_10() {
        var inboundTrackModels = modelsMapGenerator.getInboundTrackModels();
        var inboundTracks = this.inboundTracksRepository.getAll(inboundTrackModels.keySet());
        Assertions.assertEquals(0, inboundTracks.size());

        for (var inboundTrackModel : inboundTrackModels.values()) {
            var inboundTrack = this.inboundTracksRepository.get(inboundTrackModel.getTrackId());
            Assertions.assertNull(inboundTrack);
        }
    }

    @Test
    @Order(11)
    @DisplayName("OutboundTrack can be added and added OutboundTracks have the proper value")
    void test_11() {
        var outboundTrackModels = modelsMapGenerator.getOutboundTrackModels();
        var peerConnectionModels = modelsMapGenerator.getPeerConnectionModels();
        var peerConnections = this.peerConnectionsRepository.getAll(peerConnectionModels.keySet());

        for (var outboundTrackModel : outboundTrackModels.values()) {
            var peerConnection = peerConnections.get(outboundTrackModel.getPeerConnectionId());
            var addOutboundTrack = peerConnection.addOutboundTrack(
                    outboundTrackModel.getTrackId(),
                    outboundTrackModel.getAdded(),
                    outboundTrackModel.hasSfuStreamId() ? outboundTrackModel.getSfuStreamId() : null,
                    MediaKind.valueOf(outboundTrackModel.getKind()),
                    outboundTrackModel.getSsrc(0),
                    outboundTrackModel.hasMarker() ? outboundTrackModel.getMarker() : null
            );

            Assertions.assertEquals(peerConnection.getServiceId(), addOutboundTrack.getServiceId());
            Assertions.assertEquals(peerConnection.getRoomId(), addOutboundTrack.getRoomId());
            Assertions.assertEquals(peerConnection.getCallId(), addOutboundTrack.getCallId());
            Assertions.assertEquals(peerConnection.getUserId(), addOutboundTrack.getUserId());
            Assertions.assertEquals(peerConnection.getClientId(), addOutboundTrack.getClientId());
            Assertions.assertEquals(peerConnection.getPeerConnectionId(), addOutboundTrack.getPeerConnectionId());

            Assertions.assertEquals(outboundTrackModel.getTrackId(), addOutboundTrack.getTrackId());
            Assertions.assertEquals(outboundTrackModel.getAdded(), addOutboundTrack.getAdded());
            Assertions.assertEquals(outboundTrackModel.getAdded(), addOutboundTrack.getTouched());

            Assertions.assertEquals(peerConnection.getMediaUnitId(), addOutboundTrack.getMediaUnitId());
            Assertions.assertEquals(outboundTrackModel.getMarker(), addOutboundTrack.getMarker());

            Assertions.assertTrue(peerConnection.hasOutboundTrack(outboundTrackModel.getTrackId()));
        }
        this.callsRepository.save();
    }

    @Test
    @Order(12)
    @DisplayName("OutboundTracks can not be added twice")
    void test_12() {
        var outboundTrackModels = modelsMapGenerator.getOutboundTrackModels();
        var peerConnectionModels = modelsMapGenerator.getPeerConnectionModels();
        var peerConnections = this.peerConnectionsRepository.getAll(peerConnectionModels.keySet());

        for (var outboundTrackModel : outboundTrackModels.values()) {
            var peerConnection = peerConnections.get(outboundTrackModel.getPeerConnectionId());
            Assertions.assertThrows(AlreadyCreatedException.class, () -> {
                peerConnection.addOutboundTrack(
                        outboundTrackModel.getTrackId(),
                        outboundTrackModel.getAdded(),
                        outboundTrackModel.hasSfuStreamId() ? outboundTrackModel.getSfuStreamId() : null,
                        MediaKind.valueOf(outboundTrackModel.getKind()),
                        outboundTrackModel.getSsrc(0),
                        outboundTrackModel.hasMarker() ? outboundTrackModel.getMarker() : null
                );
            });
        }
    }

    @Test
    @Order(13)
    @DisplayName("OutboundTracks can be accessed through PeerConnections by getting its all outboundTracks")
    void test_13() {
        var outboundTrackModels = modelsMapGenerator.getOutboundTrackModels();
        var peerConnectionModels = modelsMapGenerator.getPeerConnectionModels();
        var peerConnections = this.peerConnectionsRepository.getAll(peerConnectionModels.keySet());

        var totalAccessedOutboundTrackIds = 0;
        for (var peerConnection : peerConnections.values()) {
            var outboundTracks = peerConnection.getOutboundTracks();
            totalAccessedOutboundTrackIds += outboundTracks.size();

            for (var outboundTrack : outboundTracks.values()) {
                Assertions.assertNotNull(outboundTrackModels.get(outboundTrack.getTrackId()));
            }
        }
        Assertions.assertEquals(totalAccessedOutboundTrackIds, outboundTrackModels.size());
    }

    @Test
    @Order(14)
    @DisplayName("OutboundTracks can be accessed through the PeerConnection by getting one by one")
    void test_14() {
        var outboundTrackModels = modelsMapGenerator.getOutboundTrackModels();
        var peerConnectionModels = modelsMapGenerator.getPeerConnectionModels();
        var peerConnections = this.peerConnectionsRepository.getAll(peerConnectionModels.keySet());

        for (var outboundTrackModel : outboundTrackModels.values()) {
            var peerConnection = peerConnections.get(outboundTrackModel.getPeerConnectionId());
            Assertions.assertNotNull(peerConnection.getOutboundTrack(outboundTrackModel.getTrackId()));
        }
    }

    @Test
    @Order(15)
    @DisplayName("OutboundTracks can be accessed through the OutboundTracks repository")
    void test_15() {
        var outboundTrackModels = modelsMapGenerator.getOutboundTrackModels();
        var outboundTracks = this.outboundTracksRepository.getAll(outboundTrackModels.keySet());

        for (var outboundTrackModel : outboundTrackModels.values()) {
            Assertions.assertNotNull(outboundTracks.get(outboundTrackModel.getTrackId()));
        }
    }

    @Test
    @Order(16)
    @DisplayName("OutboundTracks can be deleted through the PeerConnection")
    void test_16() {
        var outboundTrackModels = modelsMapGenerator.getOutboundTrackModels();
        var peerConnectionModels = modelsMapGenerator.getPeerConnectionModels();
        var peerConnections = this.peerConnectionsRepository.getAll(peerConnectionModels.keySet());

        for (var outboundTrackModel : outboundTrackModels.values()) {
            var peerConnection = peerConnections.get(outboundTrackModel.getPeerConnectionId());
            var removed = peerConnection.removeOutboundTrack(outboundTrackModel.getTrackId());
            Assertions.assertTrue(removed);

            var removedAgain = peerConnection.removeOutboundTrack(outboundTrackModel.getTrackId());
            Assertions.assertFalse(removedAgain);
        }
        this.callsRepository.save();
    }

    @Test
    @Order(17)
    @DisplayName("Removed outbound tracks are not available though outbound tracks repo")
    void test_17() {
        var outboundTrackModels = modelsMapGenerator.getOutboundTrackModels();
        var outboundTracks = this.outboundTracksRepository.getAll(outboundTrackModels.keySet());
        Assertions.assertEquals(0, outboundTracks.size());

        for (var outboundTrackModel : outboundTrackModels.values()) {
            var outboundTrack = this.outboundTracksRepository.get(outboundTrackModel.getTrackId());
            Assertions.assertNull(outboundTrack);
        }
    }
}