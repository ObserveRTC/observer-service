package org.observertc.webrtc.observer.connectors.transformations;

import io.reactivex.rxjava3.functions.Predicate;
import org.observertc.webrtc.schemas.reports.Report;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.NotNull;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public class Filter extends Transformation {
    private static final Logger logger  = LoggerFactory.getLogger(Filter.class);
    private Predicate<Report> typeFilter = report -> true;
    private final List<Predicate<Report>> filters = new LinkedList<>();

    Filter addPredicate(@NotNull Predicate<Report> predicate) {
        this.filters.add(predicate);
        return this;
    }

    @Override
    protected Optional<Report> transform(Report report) throws Throwable {
        for (Predicate<Report> predicate : this.filters) {
            if (!predicate.test(report)) {
                return Optional.empty();
            }
        }
        return Optional.of(report);
    }
}
