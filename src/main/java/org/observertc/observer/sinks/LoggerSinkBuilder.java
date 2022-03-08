package org.observertc.observer.sinks;

import org.observertc.observer.codecs.Decoder;
import org.observertc.observer.configbuilders.AbstractBuilder;
import org.observertc.observer.configbuilders.Builder;
import org.slf4j.event.Level;

import java.util.Objects;

public class LoggerSinkBuilder extends AbstractBuilder implements Builder<Sink> {

    private Decoder decoder = null;

    @Override
    public Sink build() {
        Config config = this.convertAndValidate(Config.class);
        LoggerSink result = new LoggerSink();
        Level level = Level.valueOf(config.logLevel);

        if (Objects.nonNull(this.decoder)) {
            result.withDecoder(this.decoder);
        }

        return result
                .witLogLevel(level)
                .withPrintTypeSummary(config.printTypeSummary)
                .withPrintReports(config.printReports);
    }

    @Override
    public void set(Object subject) {
        if (subject instanceof Decoder) {
            this.decoder = (Decoder) subject;
        } else {
            logger.warn("Unrecognized subject {}", subject.getClass().getSimpleName());
        }
    }

    public static class Config {
        public boolean printTypeSummary = true;
        public boolean printReports = false;
        public String logLevel = Level.INFO.name();
    }
}
