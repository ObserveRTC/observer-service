package org.observertc.observer.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.ZoneOffset;
import java.util.Objects;
import java.util.function.Function;

public class MinuteToTimeZoneOffsetConverter implements Function<Integer, String> {

    private static final Logger logger = LoggerFactory.getLogger(MinuteToTimeZoneOffsetConverter.class);

    @Override
    public String apply(Integer timeZoneOffsetInMinute) {
        try {
            if (Objects.isNull(timeZoneOffsetInMinute)) {
                return null;
            }
            int timeZoneOffsetInHours = timeZoneOffsetInMinute / 60;
            if (timeZoneOffsetInHours == 0) {
                return ZoneOffset.UTC.getId();
            }
            var zoneOffset = ZoneOffset.ofHoursMinutes(timeZoneOffsetInHours, timeZoneOffsetInMinute % 60);
            if (Objects.isNull(zoneOffset)) {
                return null;
            }
            return zoneOffset.getId();
        } catch (Throwable t) {
            String message = String.format("The provided value %d is failed to convert to a zone", timeZoneOffsetInMinute);
            logger.warn(message);
            return null;
        }
    }
}
