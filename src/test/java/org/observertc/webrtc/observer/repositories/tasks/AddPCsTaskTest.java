package org.observertc.webrtc.observer.repositories.tasks;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.observertc.webrtc.observer.entities.PeerConnectionEntity;
import org.observertc.webrtc.observer.repositories.HazelcastMaps;

import javax.inject.Inject;
import javax.inject.Provider;

@MicronautTest
class AddPCsTaskTest {

    @Inject
    TestUtils testUtils;

    @Inject
    Provider<AddPCsTask> addPCsTaskProvider;

    @Inject
    HazelcastMaps hazelcastMaps;

    @Test
    void shouldAddPCEntity_1() {
        PeerConnectionEntity pcEntity = testUtils.generatePeerConnectionEntity();

        addPCsTaskProvider.get()
                .withPeerConnection(pcEntity)
                .execute();

        Assertions.assertTrue(testUtils.isPeerConnectionEntityStored(pcEntity));
    }
}