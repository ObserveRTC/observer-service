package org.observertc.webrtc.observer.tasks;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.jeasy.random.EasyRandom;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.observertc.webrtc.observer.ObserverHazelcast;
import org.observertc.webrtc.observer.entities.OldPeerConnectionEntity;
import org.observertc.webrtc.observer.repositories.stores.PeerConnectionsRepository;
import org.observertc.webrtc.observer.repositories.stores.RepositoryProvider;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.Collection;
import java.util.UUID;

@MicronautTest
class PeerConnectionDetacherTaskTest {
    @Inject
    ObserverHazelcast observerHazelcast;

    @Inject
    RepositoryProvider repositoryProvider;

    @Inject
    Provider<PeerConnectionDetacherTask> subjectProvider;

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

    @Test
    public void shouldDetachPeerConnection() {
        // Given
        OldPeerConnectionEntity pcEntity = generator.nextObject(OldPeerConnectionEntity.class);
        this.repositoryProvider.getCallPeerConnectionsRepository().add(pcEntity.callUUID, pcEntity.peerConnectionUUID);
        this.repositoryProvider.getPeerConnectionsRepository().save(pcEntity.peerConnectionUUID, pcEntity);
        PeerConnectionDetacherTask peerConnectionDetacherTask = subjectProvider.get();

        // When
        peerConnectionDetacherTask
                .forPeerConnectionUUID(pcEntity.peerConnectionUUID)
                .execute();

        // Then
        PeerConnectionsRepository pcRepository = this.repositoryProvider.getPeerConnectionsRepository();
        Assertions.assertFalse(pcRepository.exists(pcEntity.peerConnectionUUID));
        Collection<UUID> pcUUIDs = this.repositoryProvider.getCallPeerConnectionsRepository().find(pcEntity.callUUID);
        Assertions.assertNotNull(pcUUIDs);
        Assertions.assertFalse(pcUUIDs.contains(pcEntity.peerConnectionUUID));
    }
}