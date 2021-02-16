package org.observertc.webrtc.observer.repositories.tasks;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.jeasy.random.EasyRandom;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.observertc.webrtc.observer.dto.CallDTO;
import org.observertc.webrtc.observer.entities.CallEntity;
import org.observertc.webrtc.observer.repositories.HazelcastMaps;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@MicronautTest
class FindCallsByNameTaskTest {

    private static final EasyRandom generator = new EasyRandom();

    @Inject
    Provider<FindCallsByNameTask> findCallsByNameTaskProvider;

    @Inject
    HazelcastMaps hazelcastMaps;

    @Test
    void shouldFoundCallByCallName_1() {
        CallDTO callDTO = generator.nextObject(CallDTO.class);
        hazelcastMaps.getCallDTOs().put(callDTO.callUUID, callDTO);
        hazelcastMaps.getCallNames(callDTO.serviceUUID).put(callDTO.callName, callDTO.callUUID);

        Map<UUID, CallEntity> map = findCallsByNameTaskProvider.get()
                .whereCallName(callDTO.serviceUUID, callDTO.callName)
                .execute()
                .getResult();

        Assertions.assertNotNull(map.get(callDTO.callUUID));
        Assertions.assertTrue(callDTO.equals(map.get(callDTO.callUUID)));
    }

    @Test
    void shouldFoundCallByCallName_2() {
        CallDTO callDTO = generator.nextObject(CallDTO.class);
        hazelcastMaps.getCallDTOs().put(callDTO.callUUID, callDTO);
        hazelcastMaps.getCallNames(callDTO.serviceUUID).put(callDTO.callName, callDTO.callUUID);

        Map<UUID, CallEntity> map = findCallsByNameTaskProvider.get()
                .execute(Map.of(callDTO.serviceUUID, Set.of(callDTO.callName)))
                .getResult();

        Assertions.assertNotNull(map.get(callDTO.callUUID));
        Assertions.assertTrue(callDTO.equals(map.get(callDTO.callUUID)));
    }


    @Test
    void shouldRemoveUnboundCall_1() {
        CallDTO callDTO = generator.nextObject(CallDTO.class);
        hazelcastMaps.getCallNames(callDTO.serviceUUID).put(callDTO.callName, callDTO.callUUID);

        Map<UUID, CallEntity> map = findCallsByNameTaskProvider.get()
                .whereCallName(callDTO.serviceUUID, callDTO.callName)
                .removeUnboundCallNameIsNotBound()
                .execute()
                .getResult();

        Assertions.assertNull(map.get(callDTO.callUUID));
        Assertions.assertEquals(0,  hazelcastMaps.getCallNames(callDTO.serviceUUID).get(callDTO.callName).size());
    }

    @Test
    void shouldRemoveUnboundCall_2() {
        CallDTO callDTO = generator.nextObject(CallDTO.class);
        hazelcastMaps.getCallNames(callDTO.serviceUUID).put(callDTO.callName, callDTO.callUUID);

        Map<UUID, CallEntity> map = findCallsByNameTaskProvider.get()
                .removeUnboundCallNameIsNotBound()
                .execute(Map.of(callDTO.serviceUUID, Set.of(callDTO.callName)))
                .getResult();

        Assertions.assertNull(map.get(callDTO.callUUID));
        Assertions.assertEquals(0,  hazelcastMaps.getCallNames(callDTO.serviceUUID).get(callDTO.callName).size());
    }


}