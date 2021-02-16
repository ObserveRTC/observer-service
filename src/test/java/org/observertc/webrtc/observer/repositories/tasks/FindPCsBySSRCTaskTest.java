package org.observertc.webrtc.observer.repositories.tasks;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.observertc.webrtc.observer.entities.PeerConnectionEntity;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.Map;
import java.util.UUID;

@MicronautTest
class FindPCsBySSRCTaskTest {

    @Inject
    Provider<FindPCsBySSRCTask> findPCsBySSRCTaskProvider;

    @Inject
    TestUtils testUtils;

    @Test
    void shouldFoundCallByCallName_1() {
        PeerConnectionEntity pcEntity = testUtils.generatePeerConnectionEntity();
        testUtils.insertPeerConnectionEntity(pcEntity);

        Map<UUID, PeerConnectionEntity> map = findPCsBySSRCTaskProvider.get()
                .whereServiceAndSSRC(pcEntity.serviceUUID, pcEntity.SSRCs)
                .execute()
                .getResult();

        Assertions.assertEquals(pcEntity, map.get(pcEntity.pcUUID));
    }

    @Test
    void shouldFoundCallByCallName_2() {
        PeerConnectionEntity pcEntity = testUtils.generatePeerConnectionEntity();
        testUtils.insertPeerConnectionEntity(pcEntity);

        Map<UUID, PeerConnectionEntity> map = findPCsBySSRCTaskProvider.get()
                .execute(Map.of(pcEntity.serviceUUID, pcEntity.SSRCs))
                .getResult();

        Assertions.assertEquals(pcEntity, map.get(pcEntity.pcUUID));
    }
}