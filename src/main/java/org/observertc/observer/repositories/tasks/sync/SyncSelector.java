package org.observertc.observer.repositories.tasks.sync;

import org.observertc.observer.repositories.HamokStorages;

import java.util.Map;
import java.util.function.Function;

@FunctionalInterface
public interface SyncSelector<T> extends Function<HamokStorages, Map<String, T>> {

}
