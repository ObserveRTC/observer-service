package org.observertc.webrtc.observer.repositories.tasks;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.observertc.webrtc.observer.dto.SfuRtpStreamDTO;
import org.observertc.webrtc.observer.dto.SfuRtpStreamDTOGenerator;
import org.observertc.webrtc.observer.repositories.HazelcastMaps;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.Set;

@MicronautTest
class RemoveSfuRtpStreamTaskTest {

    @Inject
    HazelcastMaps hazelcastMaps;

    @Inject
    SfuRtpStreamDTOGenerator generator;

    @Inject
    Provider<RemoveSfuRtpStreamsTask> removeSfuRtpStreamsTaskProvider;

    private SfuRtpStreamDTO createdDTO;

    @BeforeEach
    void setup() {
        this.createdDTO = this.generator.get();
        this.hazelcastMaps.getSFURtpStreams().put(this.createdDTO.transportId, this.createdDTO);
    }

    @Test
    public void removeSfuTransport_1() {
        var task = removeSfuRtpStreamsTaskProvider.get()
                .whereSfuRtpStreamPodIds(Set.of(this.createdDTO.streamId))
                .execute()
                ;

        var hasId = this.hazelcastMaps.getSFURtpStreams().containsKey(this.createdDTO.transportId);
        Assertions.assertFalse(hasId);
    }

    @Test
    public void removeSfuTransport_2() {
        var task = removeSfuRtpStreamsTaskProvider.get()
                .addRemovedSfuRtpStreamPodDTO(this.createdDTO)
                .execute()
                ;

        var hasId = this.hazelcastMaps.getSFURtpStreams().containsKey(this.createdDTO.transportId);
        Assertions.assertTrue(hasId);
    }
}