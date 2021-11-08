package org.observertc.webrtc.observer.repositories.tasks;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.observertc.webrtc.observer.dto.SfuDTOGenerator;
import org.observertc.webrtc.observer.repositories.HazelcastMaps;

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
    SfuDTOGenerator generator;

    @Test
    public void inserted_1() {
        var sfuDTO = generator.get();
        var task = addSFUsTaskProvider.get()
                .withSfuDTO(sfuDTO);

        task.execute();

        var insertedSfuDTO = this.hazelcastMaps.getSFUs().get(sfuDTO.sfuId);
        Assertions.assertEquals(sfuDTO, insertedSfuDTO);
    }

    @Test
    public void inserted_2() {
        var sfuDTO = generator.get();
        var task = addSFUsTaskProvider.get()
                .withSfuDTO(sfuDTO);

        task.execute();

        var insertedSfuDTO = this.hazelcastMaps.getSFUs().get(sfuDTO.sfuId);
        Assertions.assertEquals(sfuDTO, insertedSfuDTO);
    }

    @Test
    public void inserted_3() {
        var sfuDTO = generator.get();
        var task = addSFUsTaskProvider.get()
                .withSfuDTOs(Map.of(sfuDTO.sfuId, sfuDTO));

        task.execute();

        var insertedSfuDTO = this.hazelcastMaps.getSFUs().get(sfuDTO.sfuId);
        Assertions.assertEquals(sfuDTO, insertedSfuDTO);
    }
}