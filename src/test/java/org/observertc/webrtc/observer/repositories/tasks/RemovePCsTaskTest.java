package org.observertc.webrtc.observer.repositories.tasks;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.observertc.webrtc.observer.entities.PeerConnectionEntity;
import org.observertc.webrtc.observer.repositories.HazelcastMaps;

import javax.inject.Inject;
import javax.inject.Provider;

@MicronautTest
class RemovePCsTaskTest {

    @Inject
    TestUtils testUtils;

    @Inject
    Provider<RemovePCsTask> removePCsTaskProvider;

    @Inject
    HazelcastMaps hazelcastMaps;

    @Test
    void shouldPurgePCEntity_1() {
        PeerConnectionEntity pcEntity = testUtils.generatePeerConnectionEntity();
        testUtils.insertPeerConnectionEntity(pcEntity);

        removePCsTaskProvider.get()
                .wherePCEntities(pcEntity)
                .execute();

        Assertions.assertTrue(testUtils.isPeerConnectionEntityDeleted(pcEntity));
    }

    @Test
    void shouldPurgePCEntity_2() {
        PeerConnectionEntity pcEntity = testUtils.generatePeerConnectionEntity();
        testUtils.insertPeerConnectionEntity(pcEntity);

        removePCsTaskProvider.get()
                .wherePCUUIDs(pcEntity.pcUUID)
                .execute();

        Assertions.assertTrue(testUtils.isPeerConnectionEntityDeleted(pcEntity));
    }




}