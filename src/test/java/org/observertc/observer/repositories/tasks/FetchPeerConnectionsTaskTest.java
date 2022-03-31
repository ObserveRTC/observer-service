package org.observertc.observer.repositories.tasks;

import io.micronaut.context.BeanProvider;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.*;
import org.observertc.observer.entities.PeerConnectionEntity;
import org.observertc.observer.repositories.HazelcastMaps;
import org.observertc.observer.utils.DTOMapGenerator;

@MicronautTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class FetchPeerConnectionsTaskTest {

    @Inject
    HazelcastMaps hazelcastMaps;

    @Inject
    BeanProvider<FetchPeerConnectionsTask> fetchPeerConnectionsTaskProvider;

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
    @DisplayName("Whgen CallEntity can be created Then it can be fetched")
    void shouldFetchEntity() {
        var peerConnectionDTO = dtoMapGenerator.getPeerConnectionDTOs().values().stream().findFirst().get();
        var expected = PeerConnectionEntity.from(
                peerConnectionDTO,
                dtoMapGenerator.getMediaTrackDTOs()
        );
        var actual = fetchPeerConnectionsTaskProvider.get()
                .wherePeerConnectionIds(expected.getPeerConnectionId())
                .execute()
                .getResult()
                .get(expected.getPeerConnectionId())
                ;

        boolean equals = expected.equals(actual);
        Assertions.assertTrue(equals);
    }
}