package org.observertc.observer.repositories.tasks;

import io.micronaut.context.BeanProvider;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.*;
import org.observertc.observer.repositories.HazelcastMaps;
import org.observertc.observer.utils.DTOMapGenerator;

import java.util.Set;
import java.util.UUID;

@MicronautTest(propertySources = "classpath:repository-tasks-test.yaml")
class RemoveSfuTransportsTaskTest {

    @Inject
    HazelcastMaps hazelcastMaps;

    DTOMapGenerator generator = new DTOMapGenerator().generateSingleSfuCase();

    @Inject
    BeanProvider<RemoveSfuTransportsTask> removeSfuTransportsTaskProvider;

    @BeforeEach
    void setup() {
        this.generator.saveTo(hazelcastMaps);
    }

    @AfterEach
    void teardown() {
        this.generator.deleteFrom(hazelcastMaps);
    }

    @Test
    public void removeSfuTransport_1() {
        var sfuTransports = this.generator.getSfuTransports();
        removeSfuTransportsTaskProvider.get()
                .whereSfuTransportIds(sfuTransports.keySet())
                .execute()
        ;

        var allDeleted = sfuTransports.keySet().stream().anyMatch(this.hazelcastMaps.getSFUTransports()::containsKey) == false;
        Assertions.assertTrue(allDeleted);
    }

    @Test
    public void removeSfuTransportBindings_1() {
        var sfuTransports = this.generator.getSfuTransports();
        removeSfuTransportsTaskProvider.get()
                .whereSfuTransportIds(sfuTransports.keySet())
                .execute()
                ;
        var allDeleted = sfuTransports.values().stream().allMatch(entry -> this.hazelcastMaps.getSfuToSfuTransportIds().containsEntry(entry.sfuId, entry.transportId) == false);
        Assertions.assertTrue(allDeleted);
    }

    @Test
    public void removeSfuRtpPads_1() {
        var sfuTransports = this.generator.getSfuTransports();
        var sfuRtpPads = this.generator.getSfuRtpPads();
        removeSfuTransportsTaskProvider.get()
                .whereSfuTransportIds(sfuTransports.keySet())
                .execute()
        ;

        var allDeleted = sfuRtpPads.keySet().stream().anyMatch(this.hazelcastMaps.getSFURtpPads()::containsKey) == false;
        Assertions.assertTrue(allDeleted);
    }

    @Test
    @DisplayName("When the transport dtos are marked to be removed already Then the task does not delete the already removed one")
    public void notTryingToDeleteTransportsMarkedAsRemoved() {
        var sfuTransports = this.generator.getSfuTransports();
        var task = removeSfuTransportsTaskProvider.get();
        sfuTransports.values().forEach(task::addRemovedSfuTransportDTO);
        task.execute();

        var allRemained = sfuTransports.keySet().stream().allMatch(this.hazelcastMaps.getSFUTransports()::containsKey);
        Assertions.assertTrue(allRemained);
    }

    @Test
    public void notCrashed_1() {
        var notExistingId = UUID.randomUUID();
        var task = removeSfuTransportsTaskProvider.get()
                .whereSfuTransportIds(Set.of(notExistingId))
                .execute()
                ;
    }
}