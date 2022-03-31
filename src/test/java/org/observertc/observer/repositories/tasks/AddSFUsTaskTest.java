package org.observertc.observer.repositories.tasks;

import io.micronaut.context.BeanProvider;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.observertc.observer.repositories.HazelcastMaps;
import org.observertc.observer.utils.DTOGenerators;

import java.util.Map;

@MicronautTest
class AddSFUsTaskTest {

    @Inject
    BeanProvider<AddSFUsTask> addSFUsTaskProvider;

    @Inject
    HazelcastMaps hazelcastMaps;

    @Inject
    DTOGenerators generator;

    @Test
    public void inserted_1() {
        var expected = generator.getSfuDTO();
        var task = addSFUsTaskProvider.get()
                .withSfuDTO(expected);

        task.execute();

        var actual = this.hazelcastMaps.getSFUs().get(expected.sfuId);
        boolean equals = expected.equals(actual);
        Assertions.assertTrue(equals);
    }

    @Test
    public void inserted_2() {
        var expected = generator.getSfuDTO();
        var task = addSFUsTaskProvider.get()
                .withSfuDTO(expected);

        task.execute();

        var actual = this.hazelcastMaps.getSFUs().get(expected.sfuId);
        boolean equals = expected.equals(actual);
        Assertions.assertTrue(equals);
    }

    @Test
    public void inserted_3() {
        var expected = generator.getSfuDTO();
        var task = addSFUsTaskProvider.get()
                .withSfuDTOs(Map.of(expected.sfuId, expected));

        task.execute();

        var actual = this.hazelcastMaps.getSFUs().get(expected.sfuId);
        boolean equals = expected.equals(actual);
        Assertions.assertTrue(equals);
    }
}