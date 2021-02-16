package org.observertc.webrtc.observer.repositories.tasks;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.observertc.webrtc.observer.entities.CallEntity;
import org.observertc.webrtc.observer.repositories.HazelcastMaps;

import javax.inject.Inject;
import javax.inject.Provider;

@MicronautTest
class RemoveCallsTaskTest {


    @Inject
    TestUtils testUtils;

    @Inject
    Provider<RemoveCallsTask> removeCallsTaskProvider;

    @Inject
    HazelcastMaps hazelcastMaps;

    @Test
    void shouldPurgeCallEntity_1() {
        CallEntity callEntity = testUtils.generateCallEntity();
        testUtils.insertCallEntity(callEntity);

        removeCallsTaskProvider.get()
                .whereCallEntities(callEntity)
                .execute();

        Assertions.assertTrue(testUtils.isCallEntityDeleted(callEntity));
    }

    @Test
    void shouldPurgeCallEntity_2() {
        CallEntity callEntity = testUtils.generateCallEntity();
        testUtils.insertCallEntity(callEntity);

        removeCallsTaskProvider.get()
                .whereCallUUID(callEntity.call.callUUID)
                .execute();

        Assertions.assertTrue(testUtils.isCallEntityDeleted(callEntity));
    }





}