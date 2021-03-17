package org.observertc.webrtc.observer.sentinels;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.reactivex.rxjava3.functions.Predicate;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.observertc.webrtc.observer.dto.CallFilterDTO;
import org.observertc.webrtc.observer.dto.CollectionFilterDTO;
import org.observertc.webrtc.observer.entities.CallEntity;
import org.observertc.webrtc.observer.entities.EntitiesTestUtils;

import javax.inject.Inject;
import javax.inject.Provider;

@MicronautTest
class CallFilterBuilderTest {

    @Inject
    Provider<CallFilterBuilder> sentinelFilterBuilderProvider;

    @Inject
    EntitiesTestUtils entitiesTestUtils;

    @Test
    void shouldFilterPeerToPeer() throws Throwable {
        CollectionFilterDTO collectionFilterDTO = CollectionFilterDTO.builder()
                .numOfElementsIsLessThan(3)
                .numOfElementsIsGreaterThan(0)
                .build();
        CallFilterDTO callFilterDTO = CallFilterDTO.builder()
                .withBrowserIdsCollectionFilter(collectionFilterDTO)
                .build();

        Predicate<CallEntity> filter = sentinelFilterBuilderProvider.get().apply(callFilterDTO);

        var c1 = entitiesTestUtils.generateCallEntity(2);
        var c2 = entitiesTestUtils.generateCallEntity(3);
        Assertions.assertTrue(filter.test(c1));
        Assertions.assertFalse(filter.test(c2));
    }
}