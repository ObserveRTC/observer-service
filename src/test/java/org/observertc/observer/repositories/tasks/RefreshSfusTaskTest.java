package org.observertc.observer.repositories.tasks;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.observertc.observer.repositories.HazelcastMaps;
import org.observertc.observer.utils.DTOMapGenerator;

import javax.inject.Inject;
import javax.inject.Provider;

import static org.observertc.observer.utils.TestUtils.equalSets;

@MicronautTest
class RefreshSfusTaskTest {

    @Inject
    HazelcastMaps hazelcastMaps;

    @Inject
    Provider<RefreshSfusTask> refreshSfusTaskProvider;

    DTOMapGenerator dtoMapGenerator = new DTOMapGenerator().generateSingleSfuCase();

    @BeforeEach
    void setup() {
        dtoMapGenerator.saveTo(hazelcastMaps);
    }

    @AfterEach
    void teardown() {
        dtoMapGenerator.deleteFrom(hazelcastMaps);
    }


    @Test
    public void shouldRefreshCalls() {
        var expectedSfuIds = dtoMapGenerator.getSfuDTOs().keySet();
        var expectedSfuTransportIds = dtoMapGenerator.getSfuTransports().keySet();
        var expectedSfuRtpPadIds = dtoMapGenerator.getSfuRtpPads().keySet();
        var taskResult = refreshSfusTaskProvider.get()
                .withSfuIds(expectedSfuIds)
                .withSfuTransportIds(expectedSfuTransportIds)
                .withSfuRtpPadIds(expectedSfuRtpPadIds)
                .execute()
                .getResult();

        boolean equalSfuIds = equalSets(expectedSfuIds, taskResult.foundSfuIds);
        boolean equalSfuTransportIds = equalSets(expectedSfuTransportIds, taskResult.foundSfuTransportIds);
        boolean equalSfuRtpPadIds = equalSets(expectedSfuRtpPadIds, taskResult.foundRtpPadIds);
        Assertions.assertTrue(equalSfuIds);
        Assertions.assertTrue(equalSfuTransportIds);
        Assertions.assertTrue(equalSfuRtpPadIds);
    }
}