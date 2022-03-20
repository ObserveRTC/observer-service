package org.observertc.observer.common;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import java.time.ZoneOffset;

@MicronautTest
class MinuteToTimeZoneOffsetTest {

    @Inject
    MinuteToTimeZoneOffset minuteToTimeZoneOffset;

    @Test
    void convertToZoneOffset_1() throws Throwable {
        var timeZoneOffsetInMinute = -180;
        var expectedZoneOffset = ZoneOffset.ofHoursMinutes(-3, 0);
        var actualZoneOffset = this.minuteToTimeZoneOffset.apply(timeZoneOffsetInMinute);

        Assertions.assertEquals(expectedZoneOffset, actualZoneOffset);
    }

    @Test
    void convertToZoneOffset_2() throws Throwable {
        var timeZoneOffsetInMinute = 180;
        var expectedZoneOffset = ZoneOffset.ofHoursMinutes(3, 0);
        var actualZoneOffset = this.minuteToTimeZoneOffset.apply(timeZoneOffsetInMinute);

        Assertions.assertEquals(expectedZoneOffset, actualZoneOffset);
    }

    @Test
    void convertToZoneOffset_3() throws Throwable {
        var timeZoneOffsetInMinute = 0;
        var expectedZoneOffset = ZoneOffset.UTC;
        var actualZoneOffset = this.minuteToTimeZoneOffset.apply(timeZoneOffsetInMinute);

        Assertions.assertEquals(expectedZoneOffset, actualZoneOffset);
    }

}