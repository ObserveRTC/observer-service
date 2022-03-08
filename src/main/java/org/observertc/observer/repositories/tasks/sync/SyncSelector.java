package org.observertc.observer.repositories.tasks.sync;

import org.observertc.observer.repositories.HazelcastMaps;

import java.util.Map;
import java.util.function.Function;

@FunctionalInterface
public interface SyncSelector<T> extends Function<HazelcastMaps, Map<String, T>> {

}
