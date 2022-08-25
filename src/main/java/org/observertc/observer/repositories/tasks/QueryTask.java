package org.observertc.observer.repositories.tasks;

import io.micronaut.context.annotation.Prototype;
import jakarta.inject.Inject;
import org.observertc.observer.common.ChainedTask;
import org.observertc.observer.metrics.RepositoryMetrics;
import org.observertc.observer.repositories.HamokStorages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import java.util.Objects;
import java.util.function.Function;

@Prototype
public class QueryTask<T> extends ChainedTask<T> {
    private static final Logger logger = LoggerFactory.getLogger(FetchTracksRelationsTask.class);

    @Inject
    HamokStorages hazelcastMaps;

    @Inject
    RepositoryMetrics exposedMetrics;

    private Function<HamokStorages, T> query = null;


    @PostConstruct
    void setup() {
        this.withStatsConsumer(this.exposedMetrics::processTaskStats);
        new Builder<T>(this)
                .<Function<HamokStorages, T>> addConsumerEntry("Merge all provided inputs",
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

    public QueryTask<T> withQuery(Function<HamokStorages, T> inQuery) {
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
