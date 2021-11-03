package org.observertc.webrtc.observer.repositories.tasks;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.observertc.webrtc.observer.dto.SfuRtpPadDTO;
import org.observertc.webrtc.observer.dto.SfuRtpStreamPodDTOGenerator;
import org.observertc.webrtc.observer.repositories.HazelcastMaps;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.Set;

@MicronautTest
class RemoveSfuRtpStreamTaskTest {

    @Inject
    HazelcastMaps hazelcastMaps;

    @Inject
    SfuRtpStreamPodDTOGenerator generator;

    @Inject
    Provider<RemoveSfuRtpPadsTask> removeSfuRtpStreamsTaskProvider;

    private SfuRtpPadDTO createdDTO;

    @BeforeEach
    void setup() {
        this.createdDTO = this.generator.get();
        this.hazelcastMaps.getSFURtpPads().put(this.createdDTO.sfuPadId, this.createdDTO);
    }

    @Test
    public void removeSfuTransport_1() {
        var task = removeSfuRtpStreamsTaskProvider.get()
                .whereSfuRtpStreamPodIds(Set.of(this.createdDTO.sfuPadId))
                .execute()
                ;

        var hasId = this.hazelcastMaps.getSFURtpPads().containsKey(this.createdDTO.sfuPadId);
        Assertions.assertFalse(hasId);
    }

    @Test
    public void removeSfuTransport_2() {
        var task = removeSfuRtpStreamsTaskProvider.get()
                .addRemovedSfuRtpStreamPodDTO(this.createdDTO)
                .execute()
                ;

        var hasId = this.hazelcastMaps.getSFURtpPads().containsKey(this.createdDTO.sfuPadId);
        Assertions.assertTrue(hasId);
    }
}