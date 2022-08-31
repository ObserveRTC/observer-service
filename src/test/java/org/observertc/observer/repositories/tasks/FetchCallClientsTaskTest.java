package org.observertc.observer.repositories.tasks;

import io.micronaut.context.BeanProvider;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.*;
import org.observertc.observer.utils.DTOMapGenerator;

@MicronautTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class FetchCallClientsTaskTest {

    private static final int SETUP_STEP = -1;
    private static final int TEARDOWN_STEP = 9999;

    @Inject
    HamokStorages hamokStorages;

    @Inject
    BeanProvider<FetchCallClientsTask> fetchCallClientsTaskProvider;

    static final DTOMapGenerator dtoMapGenerator = new DTOMapGenerator().generateP2pCase();


    @Test
    @Order(SETUP_STEP)
    void setup() {
        dtoMapGenerator.saveTo(hamokStorages);
    }

    @Test
    @Order(TEARDOWN_STEP)
    void teardown() {
        dtoMapGenerator.deleteFrom(hamokStorages);
    }

    @Test
    @Order(1)
    @DisplayName("When DTOs are added to the hazelcast Then clients can be found")
    void shouldFindEntities() {
        var call = dtoMapGenerator.getCallDTO();
        var expectedIds = dtoMapGenerator.getClientDTOs().keySet();
        var actualIds = fetchCallClientsTaskProvider.get()
                .whereCallIds(call.callId)
                .execute()
                .getResult()
                .get(call.callId);

        boolean foundAll = expectedIds.stream().allMatch(actualIds::contains);
        Assertions.assertTrue(foundAll);
    }

    @Test
    @Order(TEARDOWN_STEP + 1)
    @DisplayName("When DTOs are cleared from to the hazelcast Then clients can be found")
    void shouldNotFindEntities() {
        var call = dtoMapGenerator.getCallDTO();
        var expectedIds = dtoMapGenerator.getClientDTOs().keySet();
        var actualIds = fetchCallClientsTaskProvider.get()
                .whereCallIds(call.callId)
                .execute()
                .getResult()
                .get(call.callId);

        boolean notFoundAny = expectedIds.stream().anyMatch(actualIds::contains) == false;
        Assertions.assertTrue(notFoundAny);
    }
}