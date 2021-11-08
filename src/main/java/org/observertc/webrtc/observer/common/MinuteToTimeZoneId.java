package org.observertc.webrtc.observer.common;

import io.micronaut.context.annotation.Prototype;
import io.reactivex.rxjava3.functions.Function;
import org.observertc.webrtc.observer.micrometer.FlawMonitor;
import org.observertc.webrtc.observer.micrometer.MonitorProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;

import java.time.ZoneOffset;

@Prototype
public class MinuteToTimeZoneId implements Function<Integer, ZoneOffset> {

    private static final Logger logger = LoggerFactory.getLogger(MinuteToTimeZoneId.class);

    private final FlawMonitor flawMonitor;

    public MinuteToTimeZoneId(MonitorProvider monitorProvider) {
        this.flawMonitor = monitorProvider.makeFlawMonitorFor(this.getClass());
    }

    @Override
    public ZoneOffset apply(Integer timeZoneOffsetInMinute) throws Throwable {
        try {
            int timeZoneOffsetInHours = timeZoneOffsetInMinute / 60;
            if (timeZoneOffsetInHours == 0) {
                return ZoneOffset.UTC;
            }
            String offsetID;
            char sign = '+';
            if (timeZoneOffsetInHours < 0) {
                sign = '-';
                timeZoneOffsetInHours *= -1;
            }
            if (9 < timeZoneOffsetInHours) {
                offsetID = String.format("%c%d:00", sign, timeZoneOffsetInHours);
            } else {
                offsetID = String.format("%c%02d:00", sign, timeZoneOffsetInHours);
            }
            ZoneOffset zoneOffset = ZoneOffset.of(offsetID);
            return zoneOffset;
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
