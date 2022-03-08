package org.observertc.observer.repositories.tasks;

import org.observertc.observer.common.ChainedTask;
import org.observertc.observer.micrometer.ExposedMetrics;
import org.observertc.observer.repositories.HazelcastMaps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.Objects;
import java.util.function.Function;

public class QueryTask<T> extends ChainedTask<T> {
    private static final Logger logger = LoggerFactory.getLogger(FetchTracksRelationsTask.class);

    @Inject
    HazelcastMaps hazelcastMaps;

    @Inject
    ExposedMetrics exposedMetrics;

    private Function<HazelcastMaps, T> query = null;


    @PostConstruct
    void setup() {
        this.withStatsConsumer(this.exposedMetrics::processTaskStats);
        new Builder<T>(this)
                .<Function<HazelcastMaps, T>> addConsumerEntry("Merge all provided inputs",
                        () -> {}, // no input was invoked
                        query -> { // input was invoked, so we may got some names through that
                            if (Objects.isNull(query)) {
                                return;
                            }
                            this.withQuery(query);
                        })
                .<T>addTerminalSupplier("Completed", () -> {
                    Objects.requireNonNull(this.query);
                    return this.query.apply(this.hazelcastMaps);
                })
                .build();
    }

    public QueryTask<T> withQuery(Function<HazelcastMaps, T> inQuery) {
        if (Objects.nonNull(this.query)) {
            throw new RuntimeException("Only one query function can be given");
        }
        this.query = inQuery;
        return this;
    }


    @Override
    protected void validate() {

    }
}
