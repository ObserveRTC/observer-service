package org.observertc.observer.repositories;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.*;
import org.observertc.observer.samples.ServiceRoomId;
import org.observertc.observer.utils.ModelsMapGenerator;
import org.observertc.schemas.dtos.Models;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ObservedCallsRepositoryTest {

    @Inject
    CallsRepository callsRepository;

    @Inject
    ClientsRepository clientsRepository;

    private ModelsMapGenerator modelsMapGenerator = new ModelsMapGenerator().generateP2pCase();
    private Models.Call callModel;

    @Test
    @Order(1)
    @DisplayName("When a new call is inserted it is not inserted before")
    void test_1() {
        this.callModel = modelsMapGenerator.getCallModel();
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
    }

    @Test
    @Order(2)
    @DisplayName("When the same call is inserted it returns the inserted one and not insert the provided one")
    void test_2() {
        var serviceRoomId = ServiceRoomId.make(callModel.getServiceId(), callModel.getRoomId());
        var marker = Instant.now().toEpochMilli() % 2 == 0 ? UUID.randomUUID().toString() : null;
        var alreadyInsertedCalls = this.callsRepository.insertAll(List.of(
                new CallsRepository.CreateCallInfo(
                        serviceRoomId,
                        marker,
                        callModel.getCallId(),
                        callModel.getStarted()
                )
        ));
        Assertions.assertEquals(1, alreadyInsertedCalls.size());
    }

    @Test
    @Order(3)
    @DisplayName("When a call is fetched Then it has the proper values")
    void test_3() {
        var callModel = modelsMapGenerator.getCallModel();
        var serviceRoomId = ServiceRoomId.make(callModel.getServiceId(), callModel.getRoomId());
        var call = this.callsRepository.get(callModel.getCallId());

        Assertions.assertEquals(call.getServiceRoomId(), serviceRoomId);
        Assertions.assertEquals(call.getCallId(), callModel.getCallId());
        Assertions.assertEquals(call.getServiceId(), callModel.getServiceId());
        Assertions.assertEquals(call.getRoomId(), callModel.getRoomId());

        if (call.getMarker() == null) {
            Assertions.assertTrue(callModel.hasMarker());
        } else {
            Assertions.assertEquals(call.getMarker(), callModel.getMarker());
        }
    }

    @Test
    @Order(4)
    @DisplayName("When clients are added no error is observed")
    void test_4() {
        var callModel = modelsMapGenerator.getCallModel();
        var clientModels = modelsMapGenerator.getClientModels();
        var serviceRoomId = ServiceRoomId.make(callModel.getServiceId(), callModel.getRoomId());
        var call = this.callsRepository.get(callModel.getCallId());

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
    @Order(5)
    @DisplayName("When clients are tried to add again Then errors are thrown")
    void test_5() {
        var callModel = modelsMapGenerator.getCallModel();
        var clientModels = modelsMapGenerator.getClientModels();
        var serviceRoomId = ServiceRoomId.make(callModel.getServiceId(), callModel.getRoomId());
        var call = this.callsRepository.get(callModel.getCallId());

        for (var clientModel : clientModels.values()) {
            Assertions.assertThrows(AlreadyCreatedException.class, () -> {
                call.addClient(
                        clientModel.getClientId(),
                        clientModel.getUserId(),
                        clientModel.getMediaUnitId(),
                        clientModel.getTimeZoneId(),
                        clientModel.getJoined(),
                        clientModel.hasMarker() ? clientModel.getMarker() : null
                );
            });
        }
    }

    @Test
    @Order(6)
    @DisplayName("Clients can be accessed through the call by getting its all clients")
    void test_6() {
        var callModel = modelsMapGenerator.getCallModel();
        var clientModels = modelsMapGenerator.getClientModels();
        var serviceRoomId = ServiceRoomId.make(callModel.getServiceId(), callModel.getRoomId());
        var clients = this.callsRepository.get(callModel.getCallId()).getClients();

        for (var clientModel : clientModels.values()) {
            var client = clients.get(clientModel.getClientId());
            Assertions.assertNotNull(client);
        }
    }

    @Test
    @Order(7)
    @DisplayName("Clients can be accessed through the call by getting one by one")
    void test_7() {
        var callModel = modelsMapGenerator.getCallModel();
        var clientModels = modelsMapGenerator.getClientModels();
        var serviceRoomId = ServiceRoomId.make(callModel.getServiceId(), callModel.getRoomId());
        var call = this.callsRepository.get(callModel.getCallId());

        for (var clientModel : clientModels.values()) {
            Assertions.assertTrue(call.hasClient(clientModel.getClientId()));
            Assertions.assertNotNull(call.getClient(clientModel.getClientId()));
        }
    }

    @Test
    @Order(8)
    @DisplayName("Clients can be accessed through the client repository")
    void test_8() {
        var clientModels = modelsMapGenerator.getClientModels();
        var clients = this.clientsRepository.getAll(clientModels.keySet());

        for (var clientModel : clientModels.values()) {
            Assertions.assertNotNull(clients.get(clientModel.getClientId()));
        }
    }

    @Test
    @Order(10)
    @DisplayName("Clients can be deleted through the call")
    void test_10() {
        var callModel = modelsMapGenerator.getCallModel();
        var clientModels = modelsMapGenerator.getClientModels();
        var serviceRoomId = ServiceRoomId.make(callModel.getServiceId(), callModel.getRoomId());
        var call = this.callsRepository.get(callModel.getCallId());

        for (var clientModel : clientModels.values()) {
            var removed = call.removeClient(clientModel.getClientId());
            Assertions.assertTrue(removed);

            var removedAgain = call.removeClient(clientModel.getClientId());
            Assertions.assertFalse(removedAgain);
        }
        this.callsRepository.save();
    }

    @Test
    @Order(12)
    @DisplayName("Removed clients are not available though clients repo")
    void test_12() {
        var clientModels = modelsMapGenerator.getClientModels();
        var clientIds = clientModels.keySet();
        var clients = this.clientsRepository.getAll(clientIds);
        Assertions.assertEquals(0, clients.size());
    }

    @Test
    @Order(13)
    @DisplayName("Remove cannot be done twice")
    void test_13() {
        var removedCalls_1 = this.callsRepository.removeAll(Set.of(callModel.getCallId()));
        var removedCalls_2 = this.callsRepository.removeAll(Set.of(callModel.getCallId()));
        Assertions.assertTrue(removedCalls_1.contains(callModel.getCallId()));
        Assertions.assertEquals(1, removedCalls_1.size());
        Assertions.assertEquals(0, removedCalls_2.size());
    }

    @Test
    @Order(14)
    @DisplayName("Removed call cannot be accessed though call repo")
    void test_14() {
        var callModel = modelsMapGenerator.getCallModel();
        var serviceRoomId = ServiceRoomId.make(callModel.getServiceId(), callModel.getRoomId());
        var call = this.callsRepository.get(callModel.getCallId());
        var calls = this.callsRepository.getAll(Set.of(callModel.getCallId()));
        Assertions.assertNull(call);
        Assertions.assertEquals(0, calls.size());
    }
}