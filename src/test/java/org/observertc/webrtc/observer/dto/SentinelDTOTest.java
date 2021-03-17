package org.observertc.webrtc.observer.dto;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;

@MicronautTest
class SentinelDTOTest {

    @Inject
    DTOTestUtils testUtils;

    @Test
    void shouldBuild_1() {
        SentinelDTO sentinelDTO = SentinelDTO.builder()
                .withAllCallMatchFilterNames("1", "2")
                .withAnyCallMatchFilterNames("3", "4")
                .withExpose(true)
                .withReport(true)
                .build();

        Assertions.assertEquals(true, sentinelDTO.expose);
        Assertions.assertEquals(true, sentinelDTO.report);
        Assertions.assertArrayEquals(new String[]{"1", "2"}, sentinelDTO.callFilters.allMatch);
        Assertions.assertArrayEquals(new String[]{"3", "4"}, sentinelDTO.callFilters.anyMatch);
    }

    @Test
    void shouldCheckEquality_1() {
        CallFilterDTO callFilterDTO_1 = testUtils.generateSentinelFilterDTO();
        CallFilterDTO callFilterDTO_2 = testUtils.generateSentinelFilterDTO();

        Assertions.assertEquals(callFilterDTO_1, callFilterDTO_1);
        Assertions.assertNotEquals(callFilterDTO_1, callFilterDTO_2);
    }

}