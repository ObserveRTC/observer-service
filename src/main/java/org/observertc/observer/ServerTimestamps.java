package org.observertc.observer;

import jakarta.inject.Singleton;

import javax.annotation.PostConstruct;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

@Singleton
public class ServerTimestamps {

    private Clock clock;

    @PostConstruct
    void setup() {
        this.clock = Clock.system(ZoneId.of("Europe/Helsinki"));
    }

    public Instant instant() {
        return this.clock.instant();
    }
}
