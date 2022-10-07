package org.observertc.observer.repositories;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.*;
import org.observertc.observer.samples.ServiceRoomId;
import org.observertc.observer.utils.ModelsMapGenerator;

import java.util.List;

@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ClientsRepositoryTest {

    @Inject
    CallsRepository callsRepository;

    @Inject
    ClientsRepository clientsRepository;

    @Inject
    PeerConnectionsRepository peerConnectionsRepository;
//
//    @Inject
//    InboundTracksRepository inboundTracksRepository;
//
//    @Inject
//    OutboundTracksRepository outboundTracksRepository;

    private ModelsMapGenerator modelsMapGenerator = new ModelsMapGenerator().generateP2pCase();


    @Test
    @Order(1)
    @DisplayName("Setup for scenario: Insert a call and the clients")
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
        this.callsRepository.save();
    }


    @Test
    @Order(2)
    @DisplayName("Clients can be fetched through calls repository and they inserted with the proper values")
    void test_2() {
        var clientModels = modelsMapGenerator.getClientModels();
        var clients = this.clientsRepository.getAll(clientModels.keySet());

        for (var client : clients.values()) {
            var clientModel = clientModels.get(client.getClientId());
            var call = this.callsRepository.get(client.getServiceRoomId());

            Assertions.assertEquals(client.getCallId(), call.getCallId());
            Assertions.assertTrue(call.hasClient(client.getClientId()));

            Assertions.assertEquals(clientModel.getServiceId(), client.getServiceId());
            Assertions.assertEquals(clientModel.getRoomId(), client.getRoomId());
            Assertions.assertEquals(clientModel.getCallId(), client.getCallId());
            Assertions.assertEquals(clientModel.getUserId(), client.getUserId());
            Assertions.assertEquals(clientModel.getClientId(), client.getClientId());
            Assertions.assertEquals(clientModel.getJoined(), client.getJoined());

            // after insert touched should be equal to joined
            Assertions.assertEquals(clientModel.getJoined(), client.getTouched());

            Assertions.assertEquals(clientModel.getTimeZoneId(), client.getTimeZoneId());
            Assertions.assertEquals(clientModel.getMediaUnitId(), client.getMediaUnitId());
            if (clientModel.hasMarker()) {
                Assertions.assertEquals(clientModel.getMarker(), client.getMarker());
            } else {
                Assertions.assertTrue(client.getMarker().isBlank());
            }
        }
    }

    @Test
    @Order(3)
    @DisplayName("Calls can be fetched through peer connections")
    void test_3() {
        var clientModels = modelsMapGenerator.getClientModels();
        var clients = this.clientsRepository.getAll(clientModels.keySet());

        for (var client : clients.values()) {
            var call = this.callsRepository.get(client.getServiceRoomId());

            Assertions.assertTrue(call.hasClient(client.getClientId()));
            Assertions.assertEquals(call.getServiceRoomId(), client.getServiceRoomId());
            Assertions.assertEquals(call.getClient(client.getClientId()), client);
        }
    }

    @Test
    @Order(4)
    @DisplayName("PeerConnections can be added and added peer connection have the proper value")
    void test_4() {
        var peerConnectionModels = modelsMapGenerator.getPeerConnectionModels();
        var clientModels = modelsMapGenerator.getClientModels();
        var clients = this.clientsRepository.getAll(clientModels.keySet());

        for (var peerConnectionModel : peerConnectionModels.values()) {
            var client = clients.get(peerConnectionModel.getClientId());
            var addedPeerConnection = client.addPeerConnection(
                    peerConnectionModel.getPeerConnectionId(),
                    peerConnectionModel.getOpened(),
                    peerConnectionModel.hasMarker() ? peerConnectionModel.getMarker() : null
            );

            Assertions.assertEquals(client.getServiceId(), addedPeerConnection.getServiceId());
            Assertions.assertEquals(client.getRoomId(), addedPeerConnection.getRoomId());
            Assertions.assertEquals(client.getCallId(), addedPeerConnection.getCallId());
            Assertions.assertEquals(client.getClientId(), addedPeerConnection.getClientId());
            Assertions.assertEquals(client.getUserId(), addedPeerConnection.getUserId());
            Assertions.assertEquals(peerConnectionModel.getOpened(), addedPeerConnection.getOpened());
            Assertions.assertEquals(peerConnectionModel.getOpened(), addedPeerConnection.getTouched());

            Assertions.assertEquals(peerConnectionModel.getMediaUnitId(), addedPeerConnection.getMediaUnitId());
            Assertions.assertEquals(peerConnectionModel.getMarker(), addedPeerConnection.getMarker());

            Assertions.assertTrue(client.hasPeerConnection(addedPeerConnection.getPeerConnectionId()));
        }
        this.callsRepository.save();
    }

    @Test
    @Order(5)
    @DisplayName("PeerConnections can not be added twice")
    void test_5() {
        var peerConnectionModels = modelsMapGenerator.getPeerConnectionModels();
        var clientModels = modelsMapGenerator.getClientModels();
        var clients = this.clientsRepository.getAll(clientModels.keySet());

        for (var peerConnectionModel : peerConnectionModels.values()) {
            var client = clients.get(peerConnectionModel.getClientId());
            Assertions.assertThrows(AlreadyCreatedException.class, () -> {
                var addedPeerConnection = client.addPeerConnection(
                        peerConnectionModel.getPeerConnectionId(),
                        peerConnectionModel.getOpened(),
                        peerConnectionModel.hasMarker() ? peerConnectionModel.getMarker() : null
                );
            });
        }
    }

    @Test
    @Order(6)
    @DisplayName("PeerConnections can be accessed through clients by getting its all peerConnections")
    void test_6() {
        var clientModels = modelsMapGenerator.getClientModels();
        var peerConnectionModels = modelsMapGenerator.getPeerConnectionModels();
        var clients = this.clientsRepository.getAll(clientModels.keySet());

        var totalAccessedPeerConnectionIds = 0;
        for (var client : clients.values()) {
            var peerConnections = client.getPeerConnections();
            totalAccessedPeerConnectionIds += peerConnections.size();

            for (var peerConnection : peerConnections.values()) {
                Assertions.assertNotNull(peerConnectionModels.get(peerConnection.getPeerConnectionId()));
            }
        }
        Assertions.assertEquals(totalAccessedPeerConnectionIds, peerConnectionModels.size());
    }

    @Test
    @Order(7)
    @DisplayName("PeerConnections can be accessed through the client by getting one by one")
    void test_7() {
        var clientModels = modelsMapGenerator.getClientModels();
        var peerConnectionModels = modelsMapGenerator.getPeerConnectionModels();
        var clients = this.clientsRepository.getAll(clientModels.keySet());

        for (var peerConnectionModel : peerConnectionModels.values()) {
            var client = clients.get(peerConnectionModel.getClientId());
            Assertions.assertNotNull(client.getPeerConnection(peerConnectionModel.getPeerConnectionId()));
        }
    }

    @Test
    @Order(8)
    @DisplayName("PeerConnections can be accessed through the client repository")
    void test_8() {
        var peerConnectionModels = modelsMapGenerator.getPeerConnectionModels();
        var peerConnections = this.peerConnectionsRepository.getAll(peerConnectionModels.keySet());

        for (var peerConnectionModel : peerConnectionModels.values()) {
            Assertions.assertNotNull(peerConnections.get(peerConnectionModel.getPeerConnectionId()));
        }
    }

    @Test
    @Order(9)
    @DisplayName("PeerConnections can be deleted through the client")
    void test_9() {
        var clientModels = modelsMapGenerator.getClientModels();
        var peerConnectionModels = modelsMapGenerator.getPeerConnectionModels();
        var clients = this.clientsRepository.getAll(clientModels.keySet());

        for (var peerConnectionModel : peerConnectionModels.values()) {
            var client = clients.get(peerConnectionModel.getClientId());
            var removed = client.removePeerConnection(peerConnectionModel.getPeerConnectionId());
            Assertions.assertTrue(removed);

            var removedAgain = client.removePeerConnection(peerConnectionModel.getPeerConnectionId());
            Assertions.assertFalse(removedAgain);
        }
        this.callsRepository.save();
    }

    @Test
    @Order(10)
    @DisplayName("Removed peer connections are not available though peer connections repo")
    void test_10() {
        var peerConnectionModels = modelsMapGenerator.getPeerConnectionModels();
        var peerConnections = this.peerConnectionsRepository.getAll(peerConnectionModels.keySet());
        Assertions.assertEquals(0, peerConnections.size());

        for (var peerConnectionModel : peerConnectionModels.values()) {
            var peerConnection = this.peerConnectionsRepository.get(peerConnectionModel.getPeerConnectionId());
            Assertions.assertNull(peerConnection);
        }
    }
}