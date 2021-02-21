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

@MicronautTest
class AddPCsTaskTest {

    @Inject
    HazelcastMapTestUtils hazelcastMapTestUtils;

    @Inject
    EntitiesTestUtils entitiesTestUtils;

    @Inject
    Provider<AddPCsTask> addPCsTaskProvider;

    @Inject
    HazelcastMaps hazelcastMaps;

    @Test
    void shouldAddPCEntity_1() {
        PeerConnectionEntity pcEntity = entitiesTestUtils.generatePeerConnectionEntity();

        addPCsTaskProvider.get()
                .withPeerConnection(pcEntity)
                .execute();

        Assertions.assertTrue(hazelcastMapTestUtils.isPeerConnectionEntityStored(pcEntity));
    }

    @Test
    void shouldAddPCEntity_2() {
        PeerConnectionEntity pcEntity = entitiesTestUtils.generatePeerConnectionEntity();

        addPCsTaskProvider.get()
                .execute(Map.of(pcEntity.pcUUID, pcEntity));

        Assertions.assertTrue(hazelcastMapTestUtils.isPeerConnectionEntityStored(pcEntity));
    }

}