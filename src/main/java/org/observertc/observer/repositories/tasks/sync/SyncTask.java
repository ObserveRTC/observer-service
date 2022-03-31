package org.observertc.observer.repositories.tasks.sync;

import jakarta.inject.Inject;
import org.observertc.observer.common.ChainedTask;
import org.observertc.observer.repositories.HazelcastMaps;
import org.observertc.observer.repositories.tasks.FetchTracksRelationsTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class SyncTask<T> extends ChainedTask<Void> {
    private static final Logger logger = LoggerFactory.getLogger(FetchTracksRelationsTask.class);

    @Inject
    HazelcastMaps hazelcastMaps;

    private SyncTaskReducer<T> reducer;
    private Map<String, T> subjects;
    private Map<String, SyncTaskState> loadedStates = new HashMap<>();
    private Map<String, String> reducedStates = new HashMap<>();
    private SyncSelector<T> query;

    @PostConstruct
    void setup() {
        new Builder<>(this)
                .<SyncSelector<T>> addConsumerEntry("Merge all provided inputs",
                        () -> {}, // no input was invoked
                        input -> { // input was invoked, so we may got some names through that
                            if (Objects.isNull(input)) {
                                return;
                            }
                            if (Objects.nonNull(this.query)) {
                                throw new RuntimeException("TaskId has already been defined");
                            }
                            this.query = input;
                        })
                .addActionStage("Collect Subjects", () -> {
                    Objects.requireNonNull(this.query);
                    this.subjects = this.query.apply(this.hazelcastMaps);
                })
                .addBreakCondition((resultHolder) -> {
                    if (Objects.isNull(this.subjects) || this.subjects.size() < 1) {
                        resultHolder.set(null);
                        return true;
                    }
                    return false;
                })
                .addActionStage("Load States", () -> {
                    Set<String> taskIds = this.subjects.keySet();
                    this.hazelcastMaps
                            .getSyncTaskStates()
                            .getAll(taskIds)
                            .entrySet()
                            .forEach(entry -> {
                                String taskId = entry.getKey();
                                SyncTaskState state = SyncTaskState.tryParse(entry.getValue());
                                if (Objects.isNull(state)) {
                                    return;
                                }
                                this.loadedStates.put(taskId, state);
                            });

                })
                .addBreakCondition((resultHolder) -> {
                    if (Objects.isNull(this.loadedStates) || this.loadedStates.size() < 1) {
                        resultHolder.set(null);
                        return true;
                    }
                    return false;
                })
                .addActionStage("Execute Pending or Created States", () -> {
                    this.subjects.forEach((taskId, subject) -> {
                        SyncTaskState state = this.loadedStates.get(taskId);
                        if (SyncTaskState.DONE.equals(state)) {
                            return;
                        }
                        SyncTaskState newState = this.reducer.reduce(state, this.hazelcastMaps, subject);
                        Objects.requireNonNull(newState);
                        this.reducedStates.put(taskId, newState.name());
                    });
                })
                .addTerminalSupplier("Apply new states", () -> {
                    this.hazelcastMaps.getSyncTaskStates().putAll(this.reducedStates);
                    return null;
                })
                .build();
    }

    public SyncTask withQuery(SyncSelector<T> query) {
        if (Objects.nonNull(this.query)) {
            throw new RuntimeException("Only one query function can be given");
        }
        this.query = query;
        return this;
    }

//    public SyncTask withReducer(BiFunction<SyncTaskState, HazelcastMaps, SyncTaskState> reducer) {
//        if (Objects.nonNull(this.taskId)) {
//            throw new RuntimeException("Only one reducer function can be given");
//        }
//        this.reducer = reducer;
//        return this;
//    }

    public SyncTask withReducer(SyncTaskReducer<T> reducer) {
        if (Objects.nonNull(this.reducer)) {
            throw new RuntimeException("Only one reducer function can be given");
        }

        this.reducer = reducer;
        return this;
    }

    @Override
    protected void validate() {
        Objects.requireNonNull(this.reducer);
    }
}
