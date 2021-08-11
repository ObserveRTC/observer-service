package org.observertc.webrtc.observer.repositories.tasks;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.observertc.webrtc.observer.dto.SfuTransportDTO;
import org.observertc.webrtc.observer.dto.SfuTransportDTOGenerator;
import org.observertc.webrtc.observer.repositories.HazelcastMaps;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.Set;

@MicronautTest
class RemoveSfuTransportsTaskTest {

    @Inject
    HazelcastMaps hazelcastMaps;

    @Inject
    SfuTransportDTOGenerator generator;

    @Inject
    Provider<RemoveSfuTransportsTask> removeSfuTransportsTaskProvider;

    private SfuTransportDTO createdDTO;

    @BeforeEach
    void setup() {
        this.createdDTO = this.generator.get();
        this.hazelcastMaps.getSFUTransports().put(this.createdDTO.transportId, this.createdDTO);
    }

    @Test
    public void removeSfuTransport_1() {
        var task = removeSfuTransportsTaskProvider.get()
                .whereSfuIds(Set.of(this.createdDTO.transportId))
                .execute()
                ;

        var hasId = this.hazelcastMaps.getSFUTransports().containsKey(this.createdDTO.transportId);
        Assertions.assertFalse(hasId);
    }

    @Test
    public void removeSfuTransport_2() {
        var task = removeSfuTransportsTaskProvider.get()
                .addRemovedSfuDTO(this.createdDTO)
                .execute()
                ;

        var hasId = this.hazelcastMaps.getSFUTransports().containsKey(this.createdDTO.transportId);
        Assertions.assertTrue(hasId);
    }
}