package org.observertc.observer.repositories.tasks;

import io.micronaut.context.BeanProvider;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.*;
import org.observertc.observer.repositories.HazelcastMaps;
import org.observertc.observer.samples.ServiceRoomId;
import org.observertc.observer.utils.RandomGenerators;

import java.time.Instant;
import java.util.UUID;

@MicronautTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class CreateCallIfNotExistsTaskTest {


    private final static RandomGenerators randomGenerator = new RandomGenerators();
    private final static String ROOM_ID = randomGenerator.getRandomTestRoomIds();
    private final static String SERVICE_ID = randomGenerator.getRandomServiceId();
    private final static UUID CALL_ID = UUID.randomUUID();

    @Inject
    BeanProvider<CreateCallIfNotExistsTask> createCallIfNotExistsTaskProvider;

    @Inject
    HazelcastMaps hazelcastMaps;

    @Test
    @Order(1)
    @DisplayName("When serviceId is not given Then Call should not be created")
    void shouldNotCreateCall_1() {
        var timestamp = Instant.now().toEpochMilli();
        var task = this.createCallIfNotExistsTaskProvider.get()
                .withStartedTimestamp(timestamp)
                ;

        Assertions.assertThrows(Throwable.class, task::execute);
    }

    @Test
    @Order(2)
    @DisplayName("When started timestamp is not given Then Call should not be created")
    void shouldNotCreateCall_2() {
        var serviceRoomId = ServiceRoomId.make(UUID.randomUUID().toString(), ROOM_ID);
        var task = this.createCallIfNotExistsTaskProvider.get()
                .withServiceRoomId(serviceRoomId)
                ;

        task.execute();

        Assertions.assertFalse(task.succeeded());
    }

    @Test
    @Order(3)
    @DisplayName("When task is executed with a valid serviceRomId Then it creates a call")
    void shouldCreateCall_1() {
        var timestamp = Instant.now().toEpochMilli();
        var serviceRoomId = ServiceRoomId.make(UUID.randomUUID().toString(), ROOM_ID);
        var task = this.createCallIfNotExistsTaskProvider.get()
                .withServiceRoomId(serviceRoomId)
                .withStartedTimestamp(timestamp)
                ;

        task.execute();

        var actual = task.getResult();
        Assertions.assertTrue(task.succeeded());
        Assertions.assertNotNull(actual);
    }

    @Test
    @Order(4)
    @DisplayName("When two valid serviceRoomIds having different serviceIds are given Then they two calls are created")
    void shouldCreateCall_2() {
        var timestamp = Instant.now().toEpochMilli();
        var serviceRoomId_1 = ServiceRoomId.make(UUID.randomUUID().toString(), ROOM_ID);
        var serviceRoomId_2 = ServiceRoomId.make(UUID.randomUUID().toString(), ROOM_ID);
        var task_1 = this.createCallIfNotExistsTaskProvider.get()
                .withServiceRoomId(serviceRoomId_1)
                .withStartedTimestamp(timestamp)
                ;
        var task_2 = this.createCallIfNotExistsTaskProvider.get()
                .withServiceRoomId(serviceRoomId_2)
                .withStartedTimestamp(timestamp)
                ;

        task_1.execute();
        task_2.execute();

        var call_1 = task_1.getResult();
        var call_2 = task_2.getResult();
        var equals = call_1.equals(call_2);
        Assertions.assertFalse(equals);
    }

    @Test
    @Order(5)
    @DisplayName("When two valid serviceRoomIds having different roomIds are given Then they two calls are created")
    void shouldCreateCall_3() {
        var timestamp = Instant.now().toEpochMilli();
        var serviceRoomId_1 = ServiceRoomId.make(SERVICE_ID, UUID.randomUUID().toString());
        var serviceRoomId_2 = ServiceRoomId.make(SERVICE_ID, UUID.randomUUID().toString());
        var task_1 = this.createCallIfNotExistsTaskProvider.get()
                .withServiceRoomId(serviceRoomId_1)
                .withStartedTimestamp(timestamp)
                ;
        var task_2 = this.createCallIfNotExistsTaskProvider.get()
                .withServiceRoomId(serviceRoomId_2)
                .withStartedTimestamp(timestamp)
                ;

        task_1.execute();
        task_2.execute();

        var call_1 = task_1.getResult();
        var call_2 = task_2.getResult();
        var equals = call_1.equals(call_2);
        Assertions.assertFalse(equals);
    }

    @Test
    @Order(6)
    @DisplayName("When two valid serviceRoomIds having the same room and service are given Then they one call is created")
    void shouldCreateCall_4() {
        var timestamp = Instant.now().toEpochMilli();
        var serviceRoomId_1 = ServiceRoomId.make(UUID.randomUUID().toString(), UUID.randomUUID().toString());
        var serviceRoomId_2 = ServiceRoomId.fromKey(serviceRoomId_1.getKey());
        var task_1 = this.createCallIfNotExistsTaskProvider.get()
                .withServiceRoomId(serviceRoomId_1)
                .withStartedTimestamp(timestamp)
                ;
        var task_2 = this.createCallIfNotExistsTaskProvider.get()
                .withServiceRoomId(serviceRoomId_2)
                .withStartedTimestamp(timestamp)
                ;

        task_1.execute();
        task_2.execute();

        var call_1 = task_1.getResult();
        var call_2 = task_2.getResult();
        var equals = call_1.equals(call_2);
        Assertions.assertTrue(equals);
    }

    @Test
    @Order(7)
    @DisplayName("When a valid serviceRoomId is given with a specific callId Then call is created with the specific callId")
    void shouldCreateCall_5() {
        var timestamp = Instant.now().toEpochMilli();
        var serviceRoomId = ServiceRoomId.make(SERVICE_ID, ROOM_ID);
        var task = this.createCallIfNotExistsTaskProvider.get()
                .withServiceRoomId(serviceRoomId)
                .withStartedTimestamp(timestamp)
                .withCallId(CALL_ID)

                ;

        task.execute();

        var actual = task.getResult();
        Assertions.assertEquals(CALL_ID, actual);
    }

    @Test
    @Order(8)
    @DisplayName("When a call is created it can be found in hazelcast and bound appropriately")
    void shouldBeBound() {
        var serviceRoomId = ServiceRoomId.make(SERVICE_ID, ROOM_ID);
        var callId = this.hazelcastMaps.getServiceRoomToCallIds().get(serviceRoomId.getKey());
        var callDTO = this.hazelcastMaps.getCalls().get(CALL_ID);

        Assertions.assertEquals(CALL_ID, callId);
        Assertions.assertEquals(CALL_ID, callDTO.callId);
    }
}