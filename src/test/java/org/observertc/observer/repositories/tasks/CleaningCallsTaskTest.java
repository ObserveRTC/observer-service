package org.observertc.observer.repositories.tasks;

import io.micronaut.context.BeanProvider;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.observertc.observer.common.Sleeper;
import org.observertc.observer.repositories.HazelcastMaps;
import org.observertc.observer.utils.DTOMapGenerator;

@MicronautTest(environments = "test")
class CleaningCallsTaskTest {
    org.observertc.observer.utils.DTOMapGenerator DTOMapGenerator = new DTOMapGenerator().generateP2pCase();

    @Inject
    HazelcastMaps hazelcastMaps;

    @Inject
    BeanProvider<CleaningCallsTask> cleaningCallsTaskBeanProvider;

    @BeforeEach
    void setup() {
        DTOMapGenerator.saveTo(hazelcastMaps);
    }

    @AfterEach
    void teardown() {
        DTOMapGenerator.deleteFrom(hazelcastMaps);
    }

    @Test
    void shouldClean_1() {
        var thresholdInMs = 1000;
        new Sleeper(() -> thresholdInMs * 2).run();
        cleaningCallsTaskBeanProvider.get().withExpirationThresholdInMs(thresholdInMs).execute();

        // the following maps are not updated by client entries, therefore not removed manually by accessTime
//        Assertions.assertEquals(0, this.hazelcastMaps.getCalls().size());
//        Assertions.assertEquals(0, this.hazelcastMaps.getServiceRoomToCallIds().size());

        Assertions.assertEquals(0, this.hazelcastMaps.getClients().size());
        Assertions.assertEquals(0, this.hazelcastMaps.getPeerConnections().size());
        Assertions.assertEquals(0, this.hazelcastMaps.getMediaTracks().size());
        Assertions.assertEquals(0, this.hazelcastMaps.getInboundTrackIdsToOutboundTrackIds().size());
        Assertions.assertEquals(0, this.hazelcastMaps.getSFUs().size());
        Assertions.assertEquals(0, this.hazelcastMaps.getSFUTransports().size());
        Assertions.assertEquals(0, this.hazelcastMaps.getSfuInternalInboundRtpPadIdToOutboundRtpPadId().size());
        Assertions.assertEquals(0, this.hazelcastMaps.getSfuStreams().size());
        Assertions.assertEquals(0, this.hazelcastMaps.getSfuSinks().size());
        Assertions.assertEquals(0, this.hazelcastMaps.getGeneralEntries().size());
        Assertions.assertEquals(0, this.hazelcastMaps.getWeakLocks().size());
        Assertions.assertEquals(0, this.hazelcastMaps.getSyncTaskStates().size());
        Assertions.assertEquals(0, this.hazelcastMaps.getRequests().size());
        Assertions.assertEquals(0, this.hazelcastMaps.getEtcMap().size());
    }
}