package org.observertc.observer.repositories.tasks;

import io.micronaut.context.BeanProvider;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.*;
import org.observertc.observer.utils.DTOMapGenerator;

import java.util.Set;
import java.util.UUID;

@MicronautTest(propertySources = "repository-tasks-test.yaml")
class RemoveSFUsTaskTest {

    @Inject
    HamokStorages hamokStorages;

    DTOMapGenerator generator = new DTOMapGenerator().generateSingleSfuCase();

    @Inject
    BeanProvider<RemoveSFUsTask> removeSFUsTaskProvider;

    @BeforeEach
    void setup() {
        this.generator.saveTo(hamokStorages);
    }

    @AfterEach
    void teardown() {
        this.generator.deleteFrom(hamokStorages);
    }

    @Test
    public void removeSfus_1() {
        var sfus = this.generator.getSfuDTOs();
        removeSFUsTaskProvider.get()
                .whereSfuIds(sfus.keySet())
                .execute()
        ;

        var allDeleted = sfus.keySet().stream().anyMatch(this.hamokStorages.getSFUs()::containsKey) == false;
        Assertions.assertTrue(allDeleted);
    }

    @Test
    public void removeSfuTransports_1() {
        var sfus = this.generator.getSfuDTOs();
        var sfuTransports = this.generator.getSfuTransports();
        removeSFUsTaskProvider.get()
                .whereSfuIds(sfus.keySet())
                .execute()
        ;

        var allDeleted = sfuTransports.keySet().stream().anyMatch(this.hamokStorages.getSFUTransports()::containsKey) == false;
        Assertions.assertTrue(allDeleted);
    }

    @Test
    public void removeSfuRtpPads_1() {
        var sfus = this.generator.getSfuDTOs();
        var sfuRtpPads = this.generator.getSfuRtpPads();
        removeSFUsTaskProvider.get()
                .whereSfuIds(sfus.keySet())
                .execute()
        ;

        var allDeleted = sfuRtpPads.keySet().stream().anyMatch(this.hamokStorages.getSFURtpPads()::containsKey) == false;
        Assertions.assertTrue(allDeleted);
    }

    @Test
    @DisplayName("When the transport dtos are marked to be removed already Then the task does not delete the already removed one")
    public void notTryingToDeleteSfusMarkedAsRemoved() {
        var sfus = this.generator.getSfuDTOs();
        var task = removeSFUsTaskProvider.get();
        sfus.values().forEach(task::addRemovedSfuDTO);
        task.execute();

        var allRemained = sfus.keySet().stream().allMatch(this.hamokStorages.getSFUs()::containsKey);
        Assertions.assertTrue(allRemained);
    }

    @Test
    public void notCrashed_1() {
        var notExistingId = UUID.randomUUID();
        var task = removeSFUsTaskProvider.get()
                .whereSfuIds(Set.of(notExistingId))
                .execute()
                ;
    }
}