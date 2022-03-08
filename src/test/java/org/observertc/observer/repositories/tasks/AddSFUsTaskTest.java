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
class AddSFUsTaskTest {

    @Inject
    Provider<AddSFUsTask> addSFUsTaskProvider;

    @Inject
    HazelcastMaps hazelcastMaps;

    @Inject
    DTOGenerators generator;

    @Test
    public void inserted_1() {
        var sfuDTO = generator.getSfuDTO();
        var task = addSFUsTaskProvider.get()
                .withSfuDTO(sfuDTO);

        task.execute();

        var insertedSfuDTO = this.hazelcastMaps.getSFUs().get(sfuDTO.sfuId);
        Assertions.assertEquals(sfuDTO, insertedSfuDTO);
    }

    @Test
    public void inserted_2() {
        var sfuDTO = generator.getSfuDTO();
        var task = addSFUsTaskProvider.get()
                .withSfuDTO(sfuDTO);

        task.execute();

        var insertedSfuDTO = this.hazelcastMaps.getSFUs().get(sfuDTO.sfuId);
        Assertions.assertEquals(sfuDTO, insertedSfuDTO);
    }

    @Test
    public void inserted_3() {
        var sfuDTO = generator.getSfuDTO();
        var task = addSFUsTaskProvider.get()
                .withSfuDTOs(Map.of(sfuDTO.sfuId, sfuDTO));

        task.execute();

        var insertedSfuDTO = this.hazelcastMaps.getSFUs().get(sfuDTO.sfuId);
        Assertions.assertEquals(sfuDTO, insertedSfuDTO);
    }
}