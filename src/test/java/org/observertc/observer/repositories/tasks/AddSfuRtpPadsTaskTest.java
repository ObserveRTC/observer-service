package org.observertc.observer.repositories.tasks;

import io.micronaut.context.BeanProvider;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.observertc.observer.utils.DTOGenerators;
import org.observertc.observer.utils.TestUtils;

import java.util.Map;
import java.util.UUID;

@MicronautTest
class AddSfuRtpPadsTaskTest {

    @Inject
    BeanProvider<AddSfuRtpPadsTask> addSfuRtpPadsTaskProvider;

    @Inject
    HamokStorages hazelcastMaps;

    @Inject
    DTOGenerators generator;

    @Test
    public void inserted_1() {
        var expected = generator.getSfuRtpPadDTO();
        var task = addSfuRtpPadsTaskProvider.get()
                .withSfuRtpPadDTOs(Map.of(expected.rtpPadId, expected));

        task.execute();

        var actual = this.hazelcastMaps.getSFURtpPads().get(expected.rtpPadId);
        var equals = expected.equals(actual);
        Assertions.assertTrue(equals);
    }

    @Test
    public void notCrashed_1() {
        var id = UUID.randomUUID();
        var task = addSfuRtpPadsTaskProvider.get()
                .withSfuRtpPadDTOs(TestUtils.nullValuedMap(id));

        task.execute();

        var actual = this.hazelcastMaps.getSFURtpPads().get(id);
        Assertions.assertNull(actual);
    }
}