package org.observertc.observer.repositories.tasks;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.*;
import org.observertc.observer.entities.CallEntity;
import org.observertc.observer.repositories.HazelcastMaps;
import org.observertc.observer.utils.DTOMapGenerator;

import javax.inject.Inject;
import javax.inject.Provider;

@MicronautTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class FetchCallsTaskTest {

    @Inject
    HazelcastMaps hazelcastMaps;

    @Inject
    Provider<FetchCallsTask> fetchCallsTaskProvider;

    static final DTOMapGenerator dtoMapGenerator = new DTOMapGenerator().generateP2pCase();


    @BeforeEach
    void setup() {
        dtoMapGenerator.saveTo(hazelcastMaps);
    }

    @AfterEach
    void teardown() {
        dtoMapGenerator.deleteFrom(hazelcastMaps);
    }

    @Test
    @Order(1)
    @DisplayName("When DTOs are added to the hazelcast Then clients can be found")
    void shouldFindEntities() {
        var expected = CallEntity.from(
                dtoMapGenerator.getCallDTO(),
                dtoMapGenerator.getClientDTOs(),
                dtoMapGenerator.getPeerConnectionDTOs(),
                dtoMapGenerator.getMediaTrackDTOs()
        );
        var actual = fetchCallsTaskProvider.get()
                .whereCallId(expected.getCallId())
                .execute()
                .getResult()
                .get(expected.getCallId())
                ;

        boolean equals = expected.equals(actual);
        Assertions.assertTrue(equals);
    }
}