package org.observertc.observer.repositories.tasks;

import io.micronaut.context.BeanProvider;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.observertc.observer.repositories.HamokStorages;
import org.observertc.observer.samples.ServiceRoomId;
import org.observertc.observer.utils.DTOMapGenerator;

import java.util.Set;
import java.util.UUID;

@MicronautTest
class RemoveCallsTaskTest {

    @Inject
    HamokStorages hazelcastMaps;

    DTOMapGenerator DTOMapGenerator = new DTOMapGenerator().generateP2pCase();

    @Inject
    BeanProvider<RemoveCallsTask> removeCallsTaskProvider;


    @BeforeEach
    void setup() {
        this.DTOMapGenerator.saveTo(hazelcastMaps);
    }

    @AfterEach
    void teardown() {
        this.DTOMapGenerator.deleteFrom(hazelcastMaps);
    }

    @Test
    public void removeCall_1() {
        var call = DTOMapGenerator.getCallDTO();
        var task = removeCallsTaskProvider.get()
                .whereCallIds(Set.of(call.callId))
                .execute()
                ;

        var hasCallId = this.hazelcastMaps.getCalls().containsKey(call.callId);
        Assertions.assertFalse(hasCallId);
    }


    @Test
    public void removeCall_2() {
        var call = DTOMapGenerator.getCallDTO();
        var task = removeCallsTaskProvider.get()
                .addRemovedCallDTO(call)
                .execute()
                ;

        var hasCallId = this.hazelcastMaps.getCalls().containsKey(call.callId);
        Assertions.assertTrue(hasCallId);
    }

    @Test
    public void removeRoomBindings_1() {
        var call = DTOMapGenerator.getCallDTO();
        var serviceRoomId = ServiceRoomId.make(call.serviceId, call.roomId);
        var task = removeCallsTaskProvider.get()
                .whereCallIds(Set.of(call.callId))
                .execute();

        var hasServiceRoomId = this.hazelcastMaps.getServiceRoomToCallIds().containsKey(serviceRoomId.getKey());
        Assertions.assertFalse(hasServiceRoomId);
    }

    @Test
    public void removeRoomBindings_2() {
        var call = DTOMapGenerator.getCallDTO();
        var serviceRoomId = ServiceRoomId.make(call.serviceId, call.roomId);
        var task = removeCallsTaskProvider.get()
                .addRemovedCallDTO(call)
                .execute();

        var hasServiceRoomId = this.hazelcastMaps.getServiceRoomToCallIds().containsKey(serviceRoomId.getKey());
        Assertions.assertFalse(hasServiceRoomId);
    }

    @Test
    public void removeCallClientBindings_1() {
        var call = DTOMapGenerator.getCallDTO();
        var task = removeCallsTaskProvider.get()
                .whereCallIds(Set.of(call.callId))
                .execute()
                ;

        var hasCallId = this.hazelcastMaps.getCallToClientIds().containsKey(call.callId);
        Assertions.assertFalse(hasCallId);
    }

    @Test
    public void removeCallClientBindings_2() {
        var call = DTOMapGenerator.getCallDTO();
        var task = removeCallsTaskProvider.get()
                .addRemovedCallDTO(call)
                .execute()
                ;

        var hasCallId = this.hazelcastMaps.getCallToClientIds().containsKey(call.callId);
        Assertions.assertFalse(hasCallId);
    }

    @Test
    public void removeClientDTOs_1() {
        var call = DTOMapGenerator.getCallDTO();
        var task = removeCallsTaskProvider.get()
                .whereCallIds(Set.of(call.callId))
                .execute()
                ;

        DTOMapGenerator.getClientDTOs().forEach((clientId, clientDTO) -> {
            var hasClient = this.hazelcastMaps.getClients().containsKey(clientId);
            Assertions.assertFalse(hasClient);
        });
    }

    @Test
    public void removeClientDTOs_2() {
        var call = DTOMapGenerator.getCallDTO();
        var task = removeCallsTaskProvider.get()
                .addRemovedCallDTO(call)
                .execute()
                ;

        DTOMapGenerator.getClientDTOs().forEach((clientId, clientDTO) -> {
            var hasClient = this.hazelcastMaps.getClients().containsKey(clientId);
            Assertions.assertFalse(hasClient);
        });
    }

    @Test
    public void notCrashed_1() {
        var notExistingId = UUID.randomUUID();
        var task = removeCallsTaskProvider.get()
                .whereCallIds(Set.of(notExistingId))
                .execute()
                ;
    }
}