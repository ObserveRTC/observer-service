package org.observertc.observer.repositories.tasks;

import io.micronaut.context.BeanProvider;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.*;
import org.observertc.observer.repositories.HazelcastMaps;
import org.observertc.observer.utils.DTOMapGenerator;

@MicronautTest
class RemoveSfuRtpPadsTaskTest {

    @Inject
    HazelcastMaps hazelcastMaps;

    DTOMapGenerator generator = new DTOMapGenerator().generateSingleSfuCase();

    @Inject
    BeanProvider<RemoveSfuRtpPadsTask> removeSfuRtpPadsTaskProvider;

    @BeforeEach
    void setup() {
        this.generator.saveTo(hazelcastMaps);
    }

    @AfterEach
    void teardown() {
        this.generator.deleteFrom(hazelcastMaps);
    }

    @Test
    public void removeSfus_1() {
        var rtpPads = this.generator.getSfuRtpPads();
        removeSfuRtpPadsTaskProvider.get()
                .whereSfuRtpStreamPadIds(rtpPads.keySet())
                .execute()
        ;

        var allDeleted = rtpPads.keySet().stream().anyMatch(this.hazelcastMaps.getSFUTransports()::containsKey) == false;
        Assertions.assertTrue(allDeleted);
    }

    @Test
    @DisplayName("When the transport dtos are marked to be removed already Then the task does not delete the already removed one")
    public void notTryingToDeleteSfusMarkedAsRemoved() {
        var rtpPads = this.generator.getSfuRtpPads();
        var task = removeSfuRtpPadsTaskProvider.get();
        rtpPads.values().forEach(task::addRemovedSfuRtpStreamPadDTO);
        task.execute();

        var allRemained = rtpPads.keySet().stream().allMatch(this.hazelcastMaps.getSFURtpPads()::containsKey);
        Assertions.assertTrue(allRemained);
    }
}