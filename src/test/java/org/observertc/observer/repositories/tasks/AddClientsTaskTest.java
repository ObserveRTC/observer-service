package org.observertc.observer.repositories.tasks;

import io.micronaut.context.BeanProvider;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.observertc.observer.repositories.HazelcastMaps;
import org.observertc.observer.utils.DTOGenerators;
import org.observertc.observer.utils.TestUtils;

import java.util.Map;
import java.util.UUID;

@MicronautTest
class AddClientsTaskTest {

    @Inject
    BeanProvider<AddClientsTask> addClientsTaskProvider;

    @Inject
    HazelcastMaps hazelcastMaps;

    @Inject
    DTOGenerators generator;

    @Test
    public void inserted_1() {
        var clientDTO = generator.getClientDTO();
        var task = addClientsTaskProvider.get()
                .withClientDTO(clientDTO);

        task.execute();

        var insertedClientDTO = this.hazelcastMaps.getClients().get(clientDTO.clientId);
        Assertions.assertEquals(clientDTO, insertedClientDTO);
    }

    @Test
    public void boundToCall_1() {
        var clientDTO = generator.getClientDTO();
        var task = addClientsTaskProvider.get()
                .withClientDTO(clientDTO);

        task.execute();

        var callClientIds = this.hazelcastMaps.getCallToClientIds().get(clientDTO.callId);
        Assertions.assertTrue(callClientIds.contains(clientDTO.clientId));
    }

    @Test
    public void inserted_2() {
        var clientDTO = generator.getClientDTO();
        var task = addClientsTaskProvider.get()
                .withClientDTOs(clientDTO);

        task.execute();

        var insertedClientDTO = this.hazelcastMaps.getClients().get(clientDTO.clientId);
        Assertions.assertEquals(clientDTO, insertedClientDTO);
    }

    @Test
    public void boundToCall_2() {
        var clientDTO = generator.getClientDTO();
        var task = addClientsTaskProvider.get()
                .withClientDTOs(clientDTO);

        task.execute();

        var callClientIds = this.hazelcastMaps.getCallToClientIds().get(clientDTO.callId);
        Assertions.assertTrue(callClientIds.contains(clientDTO.clientId));
    }

    @Test
    public void inserted_3() {
        var clientDTO = generator.getClientDTO();
        var task = addClientsTaskProvider.get()
                .withClientDTOs(Map.of(clientDTO.clientId, clientDTO));

        task.execute();

        var insertedClientDTO = this.hazelcastMaps.getClients().get(clientDTO.clientId);
        Assertions.assertEquals(clientDTO, insertedClientDTO);
    }

    @Test
    public void boundToCall_3() {
        var clientDTO = generator.getClientDTO();
        var task = addClientsTaskProvider.get()
                .withClientDTOs(Map.of(clientDTO.clientId, clientDTO));

        task.execute();

        var callClientIds = this.hazelcastMaps.getCallToClientIds().get(clientDTO.callId);
        Assertions.assertTrue(callClientIds.contains(clientDTO.clientId));
    }

    @Test
    public void notCrashed_1() {
        var clientId = UUID.randomUUID();
        var task = addClientsTaskProvider.get()
                .withClientDTOs(TestUtils.nullValuedMap(clientId));

        task.execute();

        var actual = this.hazelcastMaps.getMediaTracks().get(clientId);
        Assertions.assertNull(actual);
    }
}