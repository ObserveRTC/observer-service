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
import java.util.Map;
import java.util.UUID;

@MicronautTest
class FetchPCsTaskTest {

    @Inject
    Provider<FetchPCsTask> fetchPCsTaskProvider;

    @Inject
    EntitiesTestUtils entitiesTestUtils;

    @Inject
    HazelcastMaps hazelcastMaps;

    @Inject
    HazelcastMapTestUtils hazelcastMapTestUtils;

    @Test
    public void shouldFetchEntity_1() {
        PeerConnectionEntity pcEntity = entitiesTestUtils.generatePeerConnectionEntity();
        hazelcastMapTestUtils.insertPeerConnectionEntity(pcEntity);

        Map<UUID, PeerConnectionEntity> retrievedEntities = fetchPCsTaskProvider.get()
                .wherePCUuid(pcEntity.pcUUID)
                .execute()
                .getResult();

        Assertions.assertEquals(pcEntity, retrievedEntities.get(pcEntity.pcUUID));
    }

    @Test
    public void shouldFetchEntity_2() {
        PeerConnectionEntity pcEntity = entitiesTestUtils.generatePeerConnectionEntity();
        hazelcastMapTestUtils.insertPeerConnectionEntity(pcEntity);

        Map<UUID, PeerConnectionEntity> retrievedEntities = fetchPCsTaskProvider.get()
                .wherePCUuid(pcEntity.pcUUID)
                .execute()
                .getResult();

        Assertions.assertEquals(pcEntity, retrievedEntities.get(pcEntity.pcUUID));
    }
}