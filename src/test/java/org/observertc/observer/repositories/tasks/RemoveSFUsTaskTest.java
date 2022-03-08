package org.observertc.observer.repositories.tasks;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.observertc.observer.dto.SfuDTO;
import org.observertc.observer.repositories.HazelcastMaps;
import org.observertc.observer.utils.DTOGenerators;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.Set;

@MicronautTest
class RemoveSFUsTaskTest {

    @Inject
    HazelcastMaps hazelcastMaps;

    @Inject
    DTOGenerators generator;

    @Inject
    Provider<RemoveSFUsTask> removeSFUsTaskProvider;

    private SfuDTO createdSfuDTO;

    @BeforeEach
    void setup() {
        this.createdSfuDTO = this.generator.getSfuDTO();
        this.hazelcastMaps.getSFUs().put(this.createdSfuDTO.sfuId, this.createdSfuDTO);
    }

    @Test
    public void removeSfu_1() {
        var task = removeSFUsTaskProvider.get()
                .whereSfuIds(Set.of(this.createdSfuDTO.sfuId))
                .execute()
                ;

        var hasSfuId = this.hazelcastMaps.getSFUs().containsKey(this.createdSfuDTO.sfuId);
        Assertions.assertFalse(hasSfuId);
    }

    @Test
    public void removeSfu_2() {
        var task = removeSFUsTaskProvider.get()
                .addRemovedSfuDTO(this.createdSfuDTO)
                .execute()
                ;

        var hasSfuId = this.hazelcastMaps.getSFUs().containsKey(this.createdSfuDTO.sfuId);
        Assertions.assertTrue(hasSfuId);
    }
}