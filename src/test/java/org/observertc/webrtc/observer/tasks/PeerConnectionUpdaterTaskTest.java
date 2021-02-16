package org.observertc.webrtc.observer.tasks;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.jeasy.random.EasyRandom;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.observertc.webrtc.observer.ObserverHazelcast;
import org.observertc.webrtc.observer.entities.OldPeerConnectionEntity;
import org.observertc.webrtc.observer.repositories.stores.RepositoryProvider;
import org.observertc.webrtc.observer.repositories.stores.SynchronizationSourcesRepository;

import javax.inject.Inject;
import javax.inject.Provider;

@MicronautTest
class PeerConnectionUpdaterTaskTest {

    @Inject
    ObserverHazelcast observerHazelcast;

    @Inject
    RepositoryProvider repositoryProvider;

    @Inject
    Provider<PeerConnectionsUpdaterTask> subjectProvider;

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
                    .execute();
        });
    }

    @Test
    public void shouldAddMissingSSRCs() {
        // Given
        final long SSRC = 1L;
        OldPeerConnectionEntity pcEntity = generator.nextObject(OldPeerConnectionEntity.class);
        this.repositoryProvider.getPeerConnectionsRepository().save(pcEntity.peerConnectionUUID, pcEntity);

        // When
        PeerConnectionsUpdaterTask task = subjectProvider.get()
                .addStream(pcEntity.serviceUUID, pcEntity.peerConnectionUUID, SSRC);
        task.execute();

        // Then
        SynchronizationSourcesRepository SSRCRepository = this.repositoryProvider.getSSRCRepository();
        Assertions.assertTrue(SSRCRepository.exists(
                SynchronizationSourcesRepository.getKey(pcEntity.serviceUUID, SSRC)
        ));
    }
}