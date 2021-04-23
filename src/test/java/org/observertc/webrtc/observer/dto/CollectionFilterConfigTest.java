package org.observertc.webrtc.observer.dto;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.observertc.webrtc.observer.configs.CollectionFilterConfig;

import javax.inject.Inject;

@MicronautTest
class CollectionFilterConfigTest {

    @Inject
    DTOTestUtils testUtils;

    @Test
    void shouldBuild_1() {
        CollectionFilterConfig collectionFilterConfig = CollectionFilterConfig.builder()
                .numOfElementsIsGreaterThan(1)
                .numOfElementsIsEqualTo(2)
                .numOfElementsIsLessThan(3)
                .allOfTheElementsAreMatchingTo("1", "2")
                .anyOfTheElementsAreMatchingTo("3", "4")
                .build();

        Assertions.assertEquals(1, collectionFilterConfig.gt);
        Assertions.assertEquals(2, collectionFilterConfig.eq);
        Assertions.assertEquals(3, collectionFilterConfig.lt);
        Assertions.assertArrayEquals(new String[]{"1", "2"}, collectionFilterConfig.allMatch);
        Assertions.assertArrayEquals(new String[]{"3", "4"}, collectionFilterConfig.anyMatch);
    }

    @Test
    void shouldBeEqual_1() {
        CollectionFilterConfig collectionFilterConfig_1 = CollectionFilterConfig.builder()
                .numOfElementsIsGreaterThan(1)
                .build();
        CollectionFilterConfig collectionFilterConfig_2 = CollectionFilterConfig.builder()
                .numOfElementsIsGreaterThan(1)
                .build();

        Assertions.assertEquals(collectionFilterConfig_1, collectionFilterConfig_2);
    }

    @Test
    void shouldNotBeEqual_1() {
        CollectionFilterConfig collectionFilterConfig_1 = CollectionFilterConfig.builder()
                .numOfElementsIsGreaterThan(1)
                .build();
        CollectionFilterConfig collectionFilterConfig_2 = CollectionFilterConfig.builder()
                .numOfElementsIsGreaterThan(2)
                .build();

        Assertions.assertNotEquals(collectionFilterConfig_1, collectionFilterConfig_2);
    }

}