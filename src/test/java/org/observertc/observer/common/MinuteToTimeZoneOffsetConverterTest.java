package org.observertc.observer.common;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.ZoneOffset;

@MicronautTest
class MinuteToTimeZoneOffsetConverterTest {

    MinuteToTimeZoneOffsetConverter minuteToTimeZoneOffsetConverter = new MinuteToTimeZoneOffsetConverter();

    @Test
    void convertToZoneOffset_1() throws Throwable {
        var timeZoneOffsetInMinute = -180;
        var expectedZoneOffset = ZoneOffset.ofHoursMinutes(-3, 0).toString();
        var actualZoneOffset = this.minuteToTimeZoneOffsetConverter.apply(timeZoneOffsetInMinute);

        Assertions.assertEquals(expectedZoneOffset, actualZoneOffset);
    }

    @Test
    void convertToZoneOffset_2() throws Throwable {
        var timeZoneOffsetInMinute = 180;
        var expectedZoneOffset = ZoneOffset.ofHoursMinutes(3, 0).toString();
        var actualZoneOffset = this.minuteToTimeZoneOffsetConverter.apply(timeZoneOffsetInMinute);

        Assertions.assertEquals(expectedZoneOffset, actualZoneOffset);
    }

    @Test
    void convertToZoneOffset_3() throws Throwable {
        var timeZoneOffsetInMinute = 0;
        var expectedZoneOffset = ZoneOffset.UTC.toString();
        var actualZoneOffset = this.minuteToTimeZoneOffsetConverter.apply(timeZoneOffsetInMinute);

        Assertions.assertEquals(expectedZoneOffset, actualZoneOffset);
    }

}