package org.observertc.webrtc.observer.repositories.tasks;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.observertc.webrtc.observer.dto.SfuRtpStreamPodDTO;
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
    Provider<RemoveSfuRtpStreamsTask> removeSfuRtpStreamsTaskProvider;

    private SfuRtpStreamPodDTO createdDTO;

    @BeforeEach
    void setup() {
        this.createdDTO = this.generator.get();
        this.hazelcastMaps.getSFURtpPods().put(this.createdDTO.sfuPodId, this.createdDTO);
    }

    @Test
    public void removeSfuTransport_1() {
        var task = removeSfuRtpStreamsTaskProvider.get()
                .whereSfuRtpStreamPodIds(Set.of(this.createdDTO.sfuPodId))
                .execute()
                ;

        var hasId = this.hazelcastMaps.getSFURtpPods().containsKey(this.createdDTO.sfuPodId);
        Assertions.assertFalse(hasId);
    }

    @Test
    public void removeSfuTransport_2() {
        var task = removeSfuRtpStreamsTaskProvider.get()
                .addRemovedSfuRtpStreamPodDTO(this.createdDTO)
                .execute()
                ;

        var hasId = this.hazelcastMaps.getSFURtpPods().containsKey(this.createdDTO.sfuPodId);
        Assertions.assertTrue(hasId);
    }
}