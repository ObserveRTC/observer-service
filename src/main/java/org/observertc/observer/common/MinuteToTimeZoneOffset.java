package org.observertc.observer.common;

import io.micronaut.context.annotation.Prototype;
import io.reactivex.rxjava3.functions.Function;
import org.observertc.observer.micrometer.FlawMonitor;
import org.observertc.observer.micrometer.MonitorProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;

import java.time.ZoneOffset;
import java.util.Objects;

@Prototype
public class MinuteToTimeZoneOffset implements Function<Integer, ZoneOffset> {

    private static final Logger logger = LoggerFactory.getLogger(MinuteToTimeZoneOffset.class);

    private final FlawMonitor flawMonitor;

    public MinuteToTimeZoneOffset(MonitorProvider monitorProvider) {
        this.flawMonitor = monitorProvider.makeFlawMonitorFor(this.getClass());
    }

    @Override
    public ZoneOffset apply(Integer timeZoneOffsetInMinute) throws Throwable {
        try {
            if (Objects.isNull(timeZoneOffsetInMinute)) {
                return ZoneOffset.UTC;
            }
            int timeZoneOffsetInHours = timeZoneOffsetInMinute / 60;
            if (timeZoneOffsetInHours == 0) {
                return ZoneOffset.UTC;
            }
            return ZoneOffset.ofHoursMinutes(timeZoneOffsetInHours, timeZoneOffsetInMinute % 60);
        } catch (Throwable t) {
            String message = String.format("The provided value %d is failed to convert to a zone", timeZoneOffsetInMinute);
            this.flawMonitor
                    .makeLogEntry()
                    .withLogger(logger)
                    .withException(t)
                    .withLogLevel(Level.WARN)
                    .withMessage(message)
                    .complete();
            return null;
        }
    }
}
