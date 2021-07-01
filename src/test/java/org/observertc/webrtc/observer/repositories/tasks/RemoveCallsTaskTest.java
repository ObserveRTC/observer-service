package org.observertc.webrtc.observer.repositories.tasks;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.observertc.webrtc.observer.dto.CallDTO;
import org.observertc.webrtc.observer.dto.ClientDTO;
import org.observertc.webrtc.observer.repositories.HazelcastMaps;
import org.observertc.webrtc.observer.samples.ServiceRoomId;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@MicronautTest
class RemoveCallsTaskTest {

    @Inject
    HazelcastMaps hazelcastMaps;

    @Inject
    CallMapGenerator callMapGenerator;

    @Inject
    Provider<RemoveCallsTask> removeCallsTaskProvider;

    private CallDTO createdCallDTO;
    private Map<UUID, ClientDTO> createdClientDTOs;

    @BeforeEach
    void setup() {
        this.callMapGenerator.generate();
        this.createdCallDTO = this.callMapGenerator.getCallDTO();
        this.createdClientDTOs = this.callMapGenerator.getClientDTOs();
    }

    @Test
    public void removeCall_1() {
        var task = removeCallsTaskProvider.get()
                .whereCallIds(Set.of(this.createdCallDTO.callId))
                .execute()
                ;

        var hasCallId = this.hazelcastMaps.getCalls().containsKey(this.createdCallDTO.callId);
        Assertions.assertFalse(hasCallId);
    }

    @Test
    public void removeCall_2() {
        var task = removeCallsTaskProvider.get()
                .addRemovedCallDTO(this.createdCallDTO)
                .execute()
                ;

        var hasCallId = this.hazelcastMaps.getCalls().containsKey(this.createdCallDTO.callId);
        Assertions.assertTrue(hasCallId);
    }

    @Test
    public void removeRoomBindings_1() {
        var serviceRoomId = ServiceRoomId.make(this.createdCallDTO.serviceId, this.createdCallDTO.roomId);
        var task = removeCallsTaskProvider.get()
                .whereCallIds(Set.of(this.createdCallDTO.callId))
                .execute();

        var hasServiceRoomId = this.hazelcastMaps.getServiceRoomToCallIds().containsKey(serviceRoomId.getKey());
        Assertions.assertFalse(hasServiceRoomId);
    }

    @Test
    public void removeRoomBindings_2() {
        var serviceRoomId = ServiceRoomId.make(this.createdCallDTO.serviceId, this.createdCallDTO.roomId);
        var task = removeCallsTaskProvider.get()
                .addRemovedCallDTO(this.createdCallDTO)
                .execute();

        var hasServiceRoomId = this.hazelcastMaps.getServiceRoomToCallIds().containsKey(serviceRoomId.getKey());
        Assertions.assertFalse(hasServiceRoomId);
    }

    @Test
    public void removeCallClientBindings_1() {
        var task = removeCallsTaskProvider.get()
                .whereCallIds(Set.of(this.createdCallDTO.callId))
                .execute()
                ;

        var hasCallId = this.hazelcastMaps.getCallToClientIds().containsKey(this.createdCallDTO.callId);
        Assertions.assertFalse(hasCallId);
    }

    @Test
    public void removeCallClientBindings_2() {
        var task = removeCallsTaskProvider.get()
                .addRemovedCallDTO(this.createdCallDTO)
                .execute()
                ;

        var hasCallId = this.hazelcastMaps.getCallToClientIds().containsKey(this.createdCallDTO.callId);
        Assertions.assertFalse(hasCallId);
    }

    @Test
    public void removeClientDTOs_1() {
        var task = removeCallsTaskProvider.get()
                .whereCallIds(Set.of(this.createdCallDTO.callId))
                .execute()
                ;

        this.createdClientDTOs.forEach((clientId, clientDTO) -> {
            var hasClient = this.hazelcastMaps.getClients().containsKey(clientId);
            Assertions.assertFalse(hasClient);
        });
    }

    @Test
    public void removeClientDTOs_2() {
        var task = removeCallsTaskProvider.get()
                .addRemovedCallDTO(this.createdCallDTO)
                .execute()
                ;

        this.createdClientDTOs.forEach((clientId, clientDTO) -> {
            var hasClient = this.hazelcastMaps.getClients().containsKey(clientId);
            Assertions.assertFalse(hasClient);
        });
    }
}