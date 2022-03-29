//package org.observertc.observer.repositories.tasks;
//
//import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
//import org.junit.jupiter.api.Assertions;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.observertc.observer.dto.SfuRtpPadDTO;
//import org.observertc.observer.repositories.HazelcastMaps;
//
//import javax.inject.Inject;
//import javax.inject.Provider;
//import java.util.Set;
//
//@MicronautTest
//class RemoveSfuRtpStreamTaskTest {
//
//    @Inject
//    HazelcastMaps hazelcastMaps;
//
//    @Inject
//    SfuRtpStreamPodDTOGenerator generator;
//
//    @Inject
//    Provider<RemoveSfuRtpPadsTask> removeSfuRtpStreamsTaskProvider;
//
//    private SfuRtpPadDTO createdDTO;
//
//    @BeforeEach
//    void setup() {
//        this.createdDTO = this.generator.get();
//        this.hazelcastMaps.getSFURtpPads().put(this.createdDTO.rtpPadId, this.createdDTO);
//    }
//
//    @Test
//    public void removeSfuTransport_1() {
//        var task = removeSfuRtpStreamsTaskProvider.get()
//                .whereSfuRtpStreamPadIds(Set.of(this.createdDTO.rtpPadId))
//                .execute()
//                ;
//
//        var hasId = this.hazelcastMaps.getSFURtpPads().containsKey(this.createdDTO.rtpPadId);
//        Assertions.assertFalse(hasId);
//    }
//
//    @Test
//    public void removeSfuTransport_2() {
//        var task = removeSfuRtpStreamsTaskProvider.get()
//                .addRemovedSfuRtpStreamPadDTO(this.createdDTO)
//                .execute()
//                ;
//
//        var hasId = this.hazelcastMaps.getSFURtpPads().containsKey(this.createdDTO.rtpPadId);
//        Assertions.assertTrue(hasId);
//    }
//}