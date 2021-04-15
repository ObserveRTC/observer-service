package org.observertc.webrtc.observer.dto;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.observertc.webrtc.observer.configs.CallFilterConfig;
import org.observertc.webrtc.observer.configs.SentinelConfig;

import javax.inject.Inject;

@MicronautTest
class SentinelConfigTest {

    @Inject
    DTOTestUtils testUtils;

    @Test
    void shouldBuild_1() {
        SentinelConfig sentinelConfig = SentinelConfig.builder()
                .withAllCallMatchFilterNames("1", "2")
                .withAnyCallMatchFilterNames("3", "4")
                .withExpose(true)
                .withReport(true)
                .build();

        Assertions.assertEquals(true, sentinelConfig.expose);
        Assertions.assertEquals(true, sentinelConfig.report);
        Assertions.assertArrayEquals(new String[]{"1", "2"}, sentinelConfig.callFilters.allMatch);
        Assertions.assertArrayEquals(new String[]{"3", "4"}, sentinelConfig.callFilters.anyMatch);
    }

    @Test
    void shouldCheckEquality_1() {
        CallFilterConfig callFilterConfig_1 = testUtils.generateSentinelFilterDTO();
        CallFilterConfig callFilterConfig_2 = testUtils.generateSentinelFilterDTO();

        Assertions.assertEquals(callFilterConfig_1, callFilterConfig_1);
        Assertions.assertNotEquals(callFilterConfig_1, callFilterConfig_2);
    }

}