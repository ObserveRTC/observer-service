package org.observertc.webrtc.observer.repositories.tasks;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.observertc.webrtc.observer.entities.PeerConnectionEntity;
import org.observertc.webrtc.observer.repositories.HazelcastMaps;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.Map;
import java.util.UUID;

class FetchPCsTaskTest {
    @Inject
    Provider<FetchPCsTask> fetchPCsTaskProvider;

    @Inject
    HazelcastMaps hazelcastMaps;

    @Inject
    TestUtils testUtils;

    @Test
    public void shouldFetchEntity_1() {
        PeerConnectionEntity pcEntity = testUtils.generatePeerConnectionEntity();
        testUtils.insertPeerConnectionEntity(pcEntity);

        Map<UUID, PeerConnectionEntity> retrievedEntities = fetchPCsTaskProvider.get()
                .wherePCUuid(pcEntity.pcUUID)
                .execute()
                .getResult();

        Assertions.assertEquals(pcEntity, retrievedEntities.get(pcEntity.pcUUID));
    }

    @Test
    public void shouldFetchEntity_2() {
        PeerConnectionEntity pcEntity = testUtils.generatePeerConnectionEntity();
        testUtils.insertPeerConnectionEntity(pcEntity);

        Map<UUID, PeerConnectionEntity> retrievedEntities = fetchPCsTaskProvider.get()
                .wherePCUuid(pcEntity.pcUUID)
                .execute()
                .getResult();

        Assertions.assertEquals(pcEntity, retrievedEntities.get(pcEntity.pcUUID));
    }
}