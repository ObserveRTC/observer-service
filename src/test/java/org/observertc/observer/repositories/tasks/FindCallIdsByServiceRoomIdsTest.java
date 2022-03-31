package org.observertc.observer.repositories.tasks;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.observertc.observer.repositories.HazelcastMaps;
import org.observertc.observer.samples.ServiceRoomId;
import org.observertc.observer.utils.DTOMapGenerator;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.Set;

@MicronautTest
class FindCallIdsByServiceRoomIdsTest {

    @Inject
    HazelcastMaps hazelcastMaps;

    @Inject
    Provider<FindCallIdsByServiceRoomIds> findCallIdsByServiceRoomIdsProvider;

    DTOMapGenerator dtoMapGenerator = new DTOMapGenerator().generateP2pCase();

    @BeforeEach
    void setup() {
        dtoMapGenerator.saveTo(hazelcastMaps);
    }

    @AfterEach
    void teardown() {
        dtoMapGenerator.deleteFrom(hazelcastMaps);
    }

    @Test
    void shouldFindCallByServiceIds_1() {
        var call = dtoMapGenerator.getCallDTO();
        var serviceRoomId = ServiceRoomId.make(call.serviceId, call.roomId);
        var task = findCallIdsByServiceRoomIdsProvider.get()
                .whereServiceRoomId(serviceRoomId)
                .execute();

        var foundCallIds = task.getResult();
        Assertions.assertTrue(task.succeeded());
        Assertions.assertEquals(foundCallIds.get(serviceRoomId), call.callId);
    }

    @Test
    void shouldFindCallByServiceIds_2() {
        var call = dtoMapGenerator.getCallDTO();
        var serviceRoomId = ServiceRoomId.make(call.serviceId, call.roomId);
        var task = findCallIdsByServiceRoomIdsProvider.get()
                .whereServiceRoomIds(Set.of(serviceRoomId))
                .execute();

        var foundCallIds = task.getResult();
        Assertions.assertTrue(task.succeeded());
        Assertions.assertEquals(foundCallIds.get(serviceRoomId), call.callId);
    }

    @Test
    void shouldFindCallByServiceIds_3() {
        var call = dtoMapGenerator.getCallDTO();
        var serviceRoomId = ServiceRoomId.make(call.serviceId, call.roomId);
        var task = findCallIdsByServiceRoomIdsProvider.get()
                .whereServiceRoomId(serviceRoomId.serviceId, serviceRoomId.roomId)
                .execute();

        var foundCallIds = task.getResult();
        Assertions.assertTrue(task.succeeded());
        Assertions.assertEquals(foundCallIds.get(serviceRoomId), call.callId);
    }
}