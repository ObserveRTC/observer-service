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
class AddSfuRtpPadsTaskTest {

    @Inject
    Provider<AddSfuRtpPadsTask> addSfuRtpPadsTaskProvider;

    @Inject
    HazelcastMaps hazelcastMaps;

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
}