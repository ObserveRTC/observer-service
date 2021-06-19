package org.observertc.webrtc.observer.repositories.tasks;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.observertc.webrtc.observer.entities.CallEntity;
import org.observertc.webrtc.observer.entities.EntitiesTestUtils;
import org.observertc.webrtc.observer.repositories.HazelcastMapTestUtils;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@MicronautTest
class FetchCallsTaskTest {

    @Inject
    Provider<FetchCallsTask> callEntitiesFetcherTaskProvider;
//
//
    @Inject
    HazelcastMapTestUtils hazelcastMapTestUtils;

    @Inject
    EntitiesTestUtils entitiesTestUtils;

    @Test
    public void shouldFetchEntity_1() {
        CallEntity callEntity = entitiesTestUtils.generateCallEntity();
        hazelcastMapTestUtils.insertCallEntity(callEntity);
//
        Map<UUID, CallEntity> callEntities = callEntitiesFetcherTaskProvider.get()
                .whereCallUUID(callEntity.call.callUUID)
                .execute()
                .getResult();
        CallEntity retrievedCallEntity = callEntities.get(callEntity.call.callUUID);

        Assertions.assertEquals(callEntity, retrievedCallEntity);
    }

    @Test
    public void shouldFetchEntity_2() {
        CallEntity callEntity = entitiesTestUtils.generateCallEntity();
        hazelcastMapTestUtils.insertCallEntity(callEntity);

        Map<UUID, CallEntity> callEntities = callEntitiesFetcherTaskProvider.get()
                .whereCallIds(Set.of(callEntity.call.callUUID))
                .execute()
                .getResult();
        CallEntity retrievedCallEntity = callEntities.get(callEntity.call.callUUID);

        Assertions.assertEquals(callEntity, retrievedCallEntity);
    }

    @Test
    public void shouldFetchEntity_3() {
        CallEntity callEntity = entitiesTestUtils.generateCallEntity();
        hazelcastMapTestUtils.insertCallEntity(callEntity);

        Map<UUID, CallEntity> callEntities = callEntitiesFetcherTaskProvider.get()
                .execute(Set.of(callEntity.call.callUUID))
                .getResult();
        CallEntity retrievedCallEntity = callEntities.get(callEntity.call.callUUID);

        Assertions.assertEquals(callEntity, retrievedCallEntity);
    }

}