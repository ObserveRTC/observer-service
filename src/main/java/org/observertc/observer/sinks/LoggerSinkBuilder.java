package org.observertc.observer.sinks;

import org.observertc.observer.configbuilders.AbstractBuilder;
import org.observertc.observer.configbuilders.Builder;
import org.slf4j.event.Level;

public class LoggerSinkBuilder extends AbstractBuilder implements Builder<Sink> {

    @Override
    public Sink build() {
        Config config = this.convertAndValidate(Config.class);
        LoggerSink result = new LoggerSink();
        Level level = Level.valueOf(config.logLevel);

        return result
                .witLogLevel(level)
                .withPrintTypeSummary(config.printTypeSummary)
                .withPrintReports(config.printReports);
    }

    @Override
    public void set(Object subject) {
        logger.warn("Unrecognized subject {}", subject.getClass().getSimpleName());
    }

    public static class Config {
        public boolean printTypeSummary = true;
        public boolean printReports = false;
        public String logLevel = Level.INFO.name();
    }
}
