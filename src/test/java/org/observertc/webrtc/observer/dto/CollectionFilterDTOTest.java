package org.observertc.webrtc.observer.dto;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;

@MicronautTest
class CollectionFilterDTOTest {

    @Inject
    DTOTestUtils testUtils;

    @Test
    void shouldBuild_1() {
        CollectionFilterDTO collectionFilterDTO = CollectionFilterDTO.builder()
                .numOfElementsIsGreaterThan(1)
                .numOfElementsIsEqualTo(2)
                .numOfElementsIsLessThan(3)
                .allOfTheElementsAreMatchingTo("1", "2")
                .anyOfTheElementsAreMatchingTo("3", "4")
                .build();

        Assertions.assertEquals(1, collectionFilterDTO.gt);
        Assertions.assertEquals(2, collectionFilterDTO.eq);
        Assertions.assertEquals(3, collectionFilterDTO.lt);
        Assertions.assertArrayEquals(new String[]{"1", "2"}, collectionFilterDTO.allMatch);
        Assertions.assertArrayEquals(new String[]{"3", "4"}, collectionFilterDTO.anyMatch);
    }

    @Test
    void shouldBeEqual_1() {
        CollectionFilterDTO collectionFilterDTO_1 = CollectionFilterDTO.builder()
                .numOfElementsIsGreaterThan(1)
                .build();
        CollectionFilterDTO collectionFilterDTO_2 = CollectionFilterDTO.builder()
                .numOfElementsIsGreaterThan(1)
                .build();

        Assertions.assertEquals(collectionFilterDTO_1, collectionFilterDTO_2);
    }

    @Test
    void shouldNotBeEqual_1() {
        CollectionFilterDTO collectionFilterDTO_1 = CollectionFilterDTO.builder()
                .numOfElementsIsGreaterThan(1)
                .build();
        CollectionFilterDTO collectionFilterDTO_2 = CollectionFilterDTO.builder()
                .numOfElementsIsGreaterThan(2)
                .build();

        Assertions.assertNotEquals(collectionFilterDTO_1, collectionFilterDTO_2);
    }

}