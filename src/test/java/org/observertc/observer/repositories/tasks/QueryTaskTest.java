package org.observertc.observer.repositories.tasks;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.observertc.observer.dto.ClientDTO;
import org.observertc.observer.repositories.HazelcastMaps;
import org.observertc.observer.utils.DTOMapGenerator;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.Map;
import java.util.UUID;

@MicronautTest
class QueryTaskTest {

    @Inject
    HazelcastMaps hazelcastMaps;

    @Inject
    Provider<QueryTask<Map<UUID, ClientDTO>>> queryTaskProvider;

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
    public void shouldExecuteAndExtract() {
        var expected = dtoMapGenerator.getClientDTOs();
        var actual = this.queryTaskProvider.get()
                        .withQuery(hazelcastMaps1 -> {
                            return hazelcastMaps1.getClients().getAll(expected.keySet());
                        })
                        .execute()
                        .getResult();

        for (var expectedClient : expected.values()) {
            boolean equals = expectedClient.equals(actual.get(expectedClient.clientId));
            Assertions.assertTrue(equals);
        }
    }
}