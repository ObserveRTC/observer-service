package org.observertc.webrtc.observer.repositories.tasks;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.observertc.webrtc.observer.dto.ClientDTOGenerator;
import org.observertc.webrtc.observer.repositories.HazelcastMaps;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.Map;

@MicronautTest
class AddClientsTaskTest {

    @Inject
    Provider<AddClientsTask> addClientsTaskProvider;

    @Inject
    HazelcastMaps hazelcastMaps;

    @Inject
    ClientDTOGenerator generator;

    @Test
    public void inserted_1() {
        var clientDTO = generator.get();
        var task = addClientsTaskProvider.get()
                .withClientDTO(clientDTO);

        task.execute();

        var insertedClientDTO = this.hazelcastMaps.getClients().get(clientDTO.clientId);
        Assertions.assertEquals(clientDTO, insertedClientDTO);
    }

    @Test
    public void bindToCall_1() {
        var clientDTO = generator.get();
        var task = addClientsTaskProvider.get()
                .withClientDTO(clientDTO);

        task.execute();

        var callClientIds = this.hazelcastMaps.getCallToClientIds().get(clientDTO.callId);
        Assertions.assertTrue(callClientIds.contains(clientDTO.clientId));
    }

    @Test
    public void inserted_2() {
        var clientDTO = generator.get();
        var task = addClientsTaskProvider.get()
                .withClientDTOs(clientDTO);

        task.execute();

        var insertedClientDTO = this.hazelcastMaps.getClients().get(clientDTO.clientId);
        Assertions.assertEquals(clientDTO, insertedClientDTO);
    }

    @Test
    public void bindToCall_2() {
        var clientDTO = generator.get();
        var task = addClientsTaskProvider.get()
                .withClientDTOs(clientDTO);

        task.execute();

        var callClientIds = this.hazelcastMaps.getCallToClientIds().get(clientDTO.callId);
        Assertions.assertTrue(callClientIds.contains(clientDTO.clientId));
    }

    @Test
    public void inserted_3() {
        var clientDTO = generator.get();
        var task = addClientsTaskProvider.get()
                .withClientDTOs(Map.of(clientDTO.clientId, clientDTO));

        task.execute();

        var insertedClientDTO = this.hazelcastMaps.getClients().get(clientDTO.clientId);
        Assertions.assertEquals(clientDTO, insertedClientDTO);
    }

    @Test
    public void bindToCall_3() {
        var clientDTO = generator.get();
        var task = addClientsTaskProvider.get()
                .withClientDTOs(Map.of(clientDTO.clientId, clientDTO));

        task.execute();

        var callClientIds = this.hazelcastMaps.getCallToClientIds().get(clientDTO.callId);
        Assertions.assertTrue(callClientIds.contains(clientDTO.clientId));
    }
}