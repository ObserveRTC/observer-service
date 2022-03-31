package org.observertc.observer.repositories.tasks;

import io.micronaut.context.BeanProvider;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.*;
import org.observertc.observer.repositories.HazelcastMaps;
import org.observertc.observer.utils.DTOMapGenerator;

@MicronautTest
class RemoveSFUsTaskTest {

    @Inject
    HazelcastMaps hazelcastMaps;

    DTOMapGenerator generator = new DTOMapGenerator().generateSingleSfuCase();

    @Inject
    BeanProvider<RemoveSFUsTask> removeSFUsTaskProvider;

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
        var sfus = this.generator.getSfuDTOs();
        removeSFUsTaskProvider.get()
                .whereSfuIds(sfus.keySet())
                .execute()
        ;

        var allDeleted = sfus.keySet().stream().anyMatch(this.hazelcastMaps.getSFUTransports()::containsKey) == false;
        Assertions.assertTrue(allDeleted);
    }

    @Test
    @DisplayName("When the transport dtos are marked to be removed already Then the task does not delete the already removed one")
    public void notTryingToDeleteSfusMarkedAsRemoved() {
        var sfus = this.generator.getSfuDTOs();
        var task = removeSFUsTaskProvider.get();
        sfus.values().forEach(task::addRemovedSfuDTO);
        task.execute();

        var allRemained = sfus.keySet().stream().allMatch(this.hazelcastMaps.getSFUs()::containsKey);
        Assertions.assertTrue(allRemained);
    }
}