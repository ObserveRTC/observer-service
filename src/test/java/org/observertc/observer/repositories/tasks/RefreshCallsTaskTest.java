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
class RefreshCallsTaskTest {

    @Inject
    HamokStorages hamokStorages;

    @Inject
    BeanProvider<RefreshCallsTask> refreshCallsTaskProvider;

    DTOMapGenerator dtoMapGenerator = new DTOMapGenerator().generateP2pCase();

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
        var expectedClientIds = dtoMapGenerator.getClientDTOs().keySet();
        var expectedPeerConnectionIds = dtoMapGenerator.getPeerConnectionDTOs().keySet();
        var expectedTrackIds = dtoMapGenerator.getMediaTrackDTOs().keySet();
        var taskResult = refreshCallsTaskProvider.get()
                .withClientIds(expectedClientIds)
                .withPeerConnectionIds(expectedPeerConnectionIds)
                .withMediaTrackIds(expectedTrackIds)
                .execute()
                .getResult();

        boolean equalClientIds = equalSets(expectedClientIds, taskResult.foundClientIds);
        boolean equalPeerConnectionIds = equalSets(expectedPeerConnectionIds, taskResult.foundPeerConnectionIds);
        boolean equalTrackIds = equalSets(expectedTrackIds, taskResult.foundTrackIds);
        Assertions.assertTrue(equalClientIds);
        Assertions.assertTrue(equalPeerConnectionIds);
        Assertions.assertTrue(equalTrackIds);
    }


}