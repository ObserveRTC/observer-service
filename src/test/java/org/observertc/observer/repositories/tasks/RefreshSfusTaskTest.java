package org.observertc.observer.repositories.tasks;

import io.micronaut.context.BeanProvider;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.observertc.observer.repositories.HamokStorages;
import org.observertc.observer.utils.DTOMapGenerator;

import static org.observertc.observer.utils.TestUtils.equalSets;

@MicronautTest
class RefreshSfusTaskTest {

    @Inject
    HamokStorages hamokStorages;

    @Inject
    BeanProvider<RefreshSfusTask> refreshSfusTaskProvider;

    DTOMapGenerator dtoMapGenerator = new DTOMapGenerator().generateSingleSfuCase();

    @BeforeEach
    void setup() {
        dtoMapGenerator.saveTo(hamokStorages);
    }

    @AfterEach
    void teardown() {
        dtoMapGenerator.deleteFrom(hamokStorages);
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