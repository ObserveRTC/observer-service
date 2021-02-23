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
                .withAllMatchFilterNames("1", "2")
                .withAnyMatchFilterNames("3", "4")
                .withExpose(true)
                .withReport(true)
                .withStreamMetrics(true)
                .build();

        Assertions.assertEquals(true, sentinelDTO.expose);
        Assertions.assertEquals(true, sentinelDTO.report);
        Assertions.assertEquals(true, sentinelDTO.streamMetrics);
        Assertions.assertArrayEquals(new String[]{"1", "2"}, sentinelDTO.allMatchFilters);
        Assertions.assertArrayEquals(new String[]{"3", "4"}, sentinelDTO.anyMatchFilters);
    }

    @Test
    void shouldCheckEquality_1() {
        SentinelFilterDTO sentinelFilterDTO_1 = testUtils.generateSentinelFilterDTO();
        SentinelFilterDTO sentinelFilterDTO_2 = testUtils.generateSentinelFilterDTO();

        Assertions.assertEquals(sentinelFilterDTO_1, sentinelFilterDTO_1);
        Assertions.assertNotEquals(sentinelFilterDTO_1, sentinelFilterDTO_2);
    }

}