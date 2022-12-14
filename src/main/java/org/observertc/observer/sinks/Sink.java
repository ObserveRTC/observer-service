package org.observertc.observer.sinks;

import io.reactivex.rxjava3.functions.Function;
import org.observertc.observer.reports.Report;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public abstract class Sink implements Function<List<Report>, Integer> {
    private static final Logger DEFAULT_LOGGER = LoggerFactory.getLogger(Sink.class);

    protected Logger logger = DEFAULT_LOGGER;
    private Function<List<Report>, Integer> forward;
    private boolean enabled = true;

    protected Sink() {
        this.forward = reports -> {
            this.process(reports);
            return reports.size();
        };
    }

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
    public Integer apply(List<Report> reports) throws Throwable{
        return this.forward.apply(reports);
    }

    protected abstract void process(List<Report> reports);

    Sink setReportsFilter(Predicate<Report> filter) {
        this.forward = reports -> {
            var filteredReports = reports.stream().filter(filter).collect(Collectors.toList());
            this.process(filteredReports);
            return filteredReports.size();
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
