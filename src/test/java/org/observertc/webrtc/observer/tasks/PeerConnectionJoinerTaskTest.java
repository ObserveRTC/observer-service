package org.observertc.webrtc.observer.tasks;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.jeasy.random.EasyRandom;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.observertc.webrtc.observer.ObserverHazelcast;
import org.observertc.webrtc.observer.entities.PeerConnectionEntity;
import org.observertc.webrtc.observer.repositories.stores.PeerConnectionsRepository;
import org.observertc.webrtc.observer.repositories.stores.RepositoryProvider;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.Collection;
import java.util.UUID;

@MicronautTest
class PeerConnectionJoinerTaskTest {
    @Inject
    ObserverHazelcast observerHazelcast;

    @Inject
    RepositoryProvider repositoryProvider;

    @Inject
    Provider<PeerConnectionJoinerTask> subjectProvider;

    static EasyRandom generator;

    @BeforeAll
    static void setup() {
        generator = new EasyRandom();
    }

    @AfterAll
    static void teardown() {

    }

    @Test
    public void shouldValidate() {
        Assertions.assertThrows(Exception.class, () -> {
            subjectProvider.get()
                    .perform();
        });
    }

    public void shouldUnRegisterCall() {
        // Given
        PeerConnectionEntity pcEntity = generator.nextObject(PeerConnectionEntity.class);
        PeerConnectionJoinerTask peerConnectionJoinerTask = subjectProvider.get();

        // When
        peerConnectionJoinerTask
                .forEntity(pcEntity)
                .execute();

        // Then
        PeerConnectionsRepository pcRepository = this.repositoryProvider.getPeerConnectionsRepository();
        Assertions.assertTrue(pcRepository.exists(pcEntity.peerConnectionUUID));
        Collection<UUID> pcUUIDs = this.repositoryProvider.getCallPeerConnectionsRepository().find(pcEntity.callUUID);
        Assertions.assertNotNull(pcUUIDs);
        Assertions.assertTrue(pcUUIDs.contains(pcEntity.peerConnectionUUID));
    }
}