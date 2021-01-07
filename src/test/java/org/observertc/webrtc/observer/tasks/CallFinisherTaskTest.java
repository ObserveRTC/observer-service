package org.observertc.webrtc.observer.tasks;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.jeasy.random.EasyRandom;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.observertc.webrtc.observer.ObserverHazelcast;
import org.observertc.webrtc.observer.models.CallEntity;
import org.observertc.webrtc.observer.models.SynchronizationSourceEntity;
import org.observertc.webrtc.observer.repositories.hazelcast.RepositoryProvider;
import org.observertc.webrtc.observer.repositories.hazelcast.SynchronizationSourcesRepository;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.Set;

@MicronautTest
class CallFinisherTaskTest {
    @Inject
    ObserverHazelcast observerHazelcast;

    @Inject
    RepositoryProvider repositoryProvider;

    @Inject
    Provider<CallFinisherTask> subjectProvider;

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
    public void shouldUnRegisterCall() {
        // Given
        CallEntity callEntity = generator.nextObject(CallEntity.class);
        Set<Long> SSRCs = Set.of(1L, 2L);
        this.repositoryProvider.getCallEntitiesRepository().add(callEntity.callUUID, callEntity);
        SSRCs.stream().forEach(ssrc -> {
           this.repositoryProvider.getSSRCRepository().save(
                   SynchronizationSourcesRepository.getKey(callEntity.serviceUUID, ssrc),
                   SynchronizationSourceEntity.of(callEntity.serviceUUID, ssrc, callEntity.callUUID)
           );
           this.repositoryProvider.getCallSynchronizationSourcesRepository().add(
                   callEntity.callUUID,
                   SynchronizationSourcesRepository.getKey(callEntity.serviceUUID, ssrc)
           );
        });
        this.repositoryProvider.getCallNamesRepository().add(callEntity.callName, callEntity.callUUID);

        // When
        try (CallFinisherTask callFinisherTask = subjectProvider.get()) {
            callFinisherTask
                    .forCallEntity(callEntity.callUUID)
                    .perform();
        }

        // Then
        Assertions.assertFalse(this.repositoryProvider.getCallSynchronizationSourcesRepository().exists(callEntity.callUUID));
        SSRCs.stream().forEach(ssrc -> {
            String key = SynchronizationSourcesRepository.getKey(callEntity.serviceUUID, ssrc);
            Assertions.assertFalse(this.repositoryProvider.getSSRCRepository().exists(key));
        });
        Assertions.assertFalse(this.repositoryProvider.getCallEntitiesRepository().exists(callEntity.callUUID));
        Assertions.assertFalse(this.repositoryProvider.getCallNamesRepository().exists(callEntity.callName));
    }
}