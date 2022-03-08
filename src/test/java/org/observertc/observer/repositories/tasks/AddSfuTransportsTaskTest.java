package org.observertc.observer.repositories.tasks;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.observertc.observer.repositories.HazelcastMaps;
import org.observertc.observer.utils.DTOGenerators;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.Map;

@MicronautTest
class AddSfuTransportsTaskTest {

    @Inject
    Provider<AddSfuTransportsTask> addSfuTransportsTaskProvider;

    @Inject
    HazelcastMaps hazelcastMaps;

    @Inject
    DTOGenerators generator;

    @Test
    public void inserted_1() {
        var sfuTransportDTO = generator.getSfuTransportDTO();
        var task = addSfuTransportsTaskProvider.get()
                .withSfuTransportDTO(sfuTransportDTO);

        task.execute();

        var insertedSfuTransportDTO = this.hazelcastMaps.getSFUTransports().get(sfuTransportDTO.transportId);
        Assertions.assertEquals(sfuTransportDTO, insertedSfuTransportDTO);
    }

    @Test
    public void inserted_2() {
        var sfuTransportDTO = generator.getSfuTransportDTO();
        var task = addSfuTransportsTaskProvider.get()
                .withSfuTransportDTO(sfuTransportDTO);

        task.execute();

        var insertedSfuTransportDTO = this.hazelcastMaps.getSFUTransports().get(sfuTransportDTO.transportId);
        Assertions.assertEquals(sfuTransportDTO, insertedSfuTransportDTO);
    }

    @Test
    public void inserted_3() {
        var sfuTransportDTO = generator.getSfuTransportDTO();
        var task = addSfuTransportsTaskProvider.get()
                .withSfuTransportDTOs(Map.of(sfuTransportDTO.transportId, sfuTransportDTO));

        task.execute();

        var insertedSfuTransportDTO = this.hazelcastMaps.getSFUTransports().get(sfuTransportDTO.transportId);
        Assertions.assertEquals(sfuTransportDTO, insertedSfuTransportDTO);
    }
}