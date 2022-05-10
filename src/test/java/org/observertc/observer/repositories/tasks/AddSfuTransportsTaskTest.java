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
class AddSfuTransportsTaskTest {

    @Inject
    BeanProvider<AddSfuTransportsTask> addSfuTransportsTaskProvider;

    @Inject
    HazelcastMaps hazelcastMaps;

    @Inject
    DTOGenerators generator;

    @Test
    public void inserted_1() {
        var expected = generator.getSfuTransportDTO();
        var task = addSfuTransportsTaskProvider.get()
                .withSfuTransportDTO(expected);

        task.execute();

        var actual = this.hazelcastMaps.getSFUTransports().get(expected.transportId);
        var equals = expected.equals(actual);
        Assertions.assertTrue(equals);
    }

    @Test
    public void inserted_2() {
        var expected = generator.getSfuTransportDTO();
        var task = addSfuTransportsTaskProvider.get()
                .withSfuTransportDTO(expected);

        task.execute();

        var actual = this.hazelcastMaps.getSFUTransports().get(expected.transportId);
        var equals = expected.equals(actual);
        Assertions.assertTrue(equals);
    }

    @Test
    public void inserted_3() {
        var expected = generator.getSfuTransportDTO();
        var task = addSfuTransportsTaskProvider.get()
                .withSfuTransportDTOs(Map.of(expected.transportId, expected));

        task.execute();

        var actual = this.hazelcastMaps.getSFUTransports().get(expected.transportId);
        var equals = expected.equals(actual);
        Assertions.assertTrue(equals);
    }

    @Test
    public void notCrashed_1() {
        var id = UUID.randomUUID();
        var task = addSfuTransportsTaskProvider.get()
                .withSfuTransportDTOs(TestUtils.nullValuedMap(id));

        task.execute();

        var actual = this.hazelcastMaps.getSFUTransports().get(id);
        Assertions.assertNull(actual);
    }
}