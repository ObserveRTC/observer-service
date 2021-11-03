package org.observertc.webrtc.observer.repositories.tasks.sync;

import org.observertc.webrtc.observer.repositories.HazelcastMaps;

import java.util.Map;
import java.util.function.Function;

@FunctionalInterface
public interface SyncSelector<T> extends Function<HazelcastMaps, Map<String, T>> {

}
