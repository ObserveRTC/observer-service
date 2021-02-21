package org.observertc.webrtc.observer.repositories.tasks;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.observertc.webrtc.observer.entities.CallEntity;
import org.observertc.webrtc.observer.entities.EntitiesTestUtils;
import org.observertc.webrtc.observer.repositories.HazelcastMapTestUtils;
import org.observertc.webrtc.observer.repositories.HazelcastMaps;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.Map;

@MicronautTest
class AddCallsTaskTest {

    @Inject
    EntitiesTestUtils entitiesTestUtils;

    @Inject
    HazelcastMapTestUtils hazelcastMapTestUtils;

    @Inject
    Provider<AddCallsTask> addCallsTaskProvider;

    @Inject
    HazelcastMaps hazelcastMaps;

    @Test
    void shouldAddCallEntity_1() {
        CallEntity callEntity = entitiesTestUtils.generateCallEntity();

        var task = addCallsTaskProvider.get()
                .withCallEntity(callEntity)
                .execute();

        Assertions.assertTrue(hazelcastMapTestUtils.isCallEntityStored(callEntity));
        Assertions.assertEquals(callEntity, task.getResult().values().stream().findFirst().get());
    }

    @Test
    void shouldAddCallEntity_2() {
        CallEntity callEntity = entitiesTestUtils.generateCallEntity();

        var task = addCallsTaskProvider.get()
                .execute(Map.of(callEntity.call.callUUID, callEntity));

        Assertions.assertTrue(hazelcastMapTestUtils.isCallEntityStored(callEntity));
        Assertions.assertEquals(callEntity, task.getResult().values().stream().findFirst().get());
    }

    @Test
    void shouldFindExistingCallEntity_1() {
        CallEntity addedEntity = entitiesTestUtils.generateCallEntity();
        hazelcastMapTestUtils.insertCallEntity(addedEntity);
        CallEntity candidate = entitiesTestUtils.generateCallEntity();
        candidate.call.callName = addedEntity.call.callName;
        candidate.call.serviceUUID = addedEntity.call.serviceUUID;

        var task = addCallsTaskProvider.get()
                .withCallEntity(candidate)
                .execute();

        Assertions.assertEquals(addedEntity.call, task.getResult().values().stream().findFirst().get().call);
    }



}