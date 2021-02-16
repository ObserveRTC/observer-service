package org.observertc.webrtc.observer.repositories.tasks;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.observertc.webrtc.observer.entities.CallEntity;
import org.observertc.webrtc.observer.repositories.HazelcastMaps;

import javax.inject.Inject;
import javax.inject.Provider;

@MicronautTest
class AddCallsTaskTest {

    @Inject
    TestUtils testUtils;

    @Inject
    Provider<AddCallsTask> addCallsTaskProvider;

    @Inject
    HazelcastMaps hazelcastMaps;

    @Test
    void shouldAddCallEntity_1() {
        CallEntity callEntity = testUtils.generateCallEntity();

        addCallsTaskProvider.get()
                .withCallEntity(callEntity)
                .execute();

        Assertions.assertTrue(testUtils.isCallEntityStored(callEntity));
    }



}