package org.observertc.webrtc.observer.repositories.tasks;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.observertc.webrtc.observer.entities.EntitiesTestUtils;
import org.observertc.webrtc.observer.entities.PeerConnectionEntity;
import org.observertc.webrtc.observer.repositories.HazelcastMapTestUtils;
import org.observertc.webrtc.observer.repositories.HazelcastMaps;

import javax.inject.Inject;
import javax.inject.Provider;

@MicronautTest
class RemovePeerConnectionsTaskTest {

    @Inject
    HazelcastMapTestUtils hazelcastMapTestUtils;

    @Inject
    Provider<RemovePeerConnectionsTask> removePCsTaskProvider;

    @Inject
    HazelcastMaps hazelcastMaps;

    @Inject
    EntitiesTestUtils entitiesTestUtils;

    @Test
    void shouldPurgePCEntity_1() {
        PeerConnectionEntity pcEntity = entitiesTestUtils.generatePeerConnectionEntity();
        hazelcastMapTestUtils.insertPeerConnectionEntity(pcEntity);

        removePCsTaskProvider.get()
                .wherePCEntities(pcEntity)
                .execute();

        Assertions.assertTrue(hazelcastMapTestUtils.isPeerConnectionEntityDeleted(pcEntity));
    }

    @Test
    void shouldPurgePCEntity_2() {
        PeerConnectionEntity pcEntity = entitiesTestUtils.generatePeerConnectionEntity();
        hazelcastMapTestUtils.insertPeerConnectionEntity(pcEntity);

        removePCsTaskProvider.get()
                .wherePCUUIDs(pcEntity.pcUUID)
                .execute();

        Assertions.assertTrue(hazelcastMapTestUtils.isPeerConnectionEntityDeleted(pcEntity));
    }




}