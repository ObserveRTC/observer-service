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
import java.util.Set;
import java.util.stream.Collectors;

@MicronautTest
class UpdatePCSSRCsTaskTest {

    @Inject
    HazelcastMapTestUtils hazelcastMapTestUtils;

    @Inject
    Provider<UpdatePCSSRCsTask> updatePCSSRCsTaskProvider;

    @Inject
    HazelcastMaps hazelcastMaps;

    @Inject
    EntitiesTestUtils entitiesTestUtils;

    @Test
    public void shouldUpdateSSRCs_1() {
        PeerConnectionEntity pcEntity = entitiesTestUtils.generatePeerConnectionEntity();
        hazelcastMapTestUtils.insertPeerConnectionEntity(pcEntity);
        Set<Long> newSSRCs = entitiesTestUtils.generatePeerConnectionEntity().SSRCs;
        Set<Long> deletedSSRCs = pcEntity.SSRCs.stream().filter(SSRC -> !newSSRCs.contains(SSRC)).collect(Collectors.toSet());

        updatePCSSRCsTaskProvider.get()
                .withPeerConnectionSSRCs(pcEntity.serviceUUID, pcEntity.pcUUID, newSSRCs)
                .execute();


        Assertions.assertTrue(hazelcastMapTestUtils.isSSRCsStoredToPC(pcEntity.serviceUUID, pcEntity.pcUUID, newSSRCs));
        Assertions.assertTrue(hazelcastMapTestUtils.isSSRCsDeletedFromPC(pcEntity.serviceUUID, pcEntity.pcUUID, deletedSSRCs));
    }




}