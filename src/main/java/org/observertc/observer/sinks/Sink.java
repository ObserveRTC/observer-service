package org.observertc.observer.sinks;

import io.reactivex.rxjava3.functions.Consumer;
import org.observertc.observer.reports.Report;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public abstract class Sink implements Consumer<List<Report>> {
    private static final Logger DEFAULT_LOGGER = LoggerFactory.getLogger(Sink.class);
    protected Logger logger = DEFAULT_LOGGER;
    private Consumer<List<Report>> forward = this::process;
    private boolean enabled = true;

    Sink setEnabled(boolean value) {
        this.enabled = value;
        return this;
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    Sink withLogger(Logger logger) {
        this.logger.info("Default logger for {} is switched to {}", this.getClass().getSimpleName(), logger.getName());
        this.logger = logger;
        return this;
    }

    @Override
    public void accept(List<Report> reports) throws Throwable{
        this.forward.accept(reports);
    }

    protected abstract void process(List<Report> reports);

    Sink setReportsFilter(Predicate<Report> filter) {
        this.forward = reports -> {
            var filteredReports = reports.stream().filter(filter).collect(Collectors.toList());
            this.process(filteredReports);
        };
        return this;
    }

    public void close() {
        logger.info("Closed");
    }

    public void open() {
        logger.info("Opened");
    }

}
