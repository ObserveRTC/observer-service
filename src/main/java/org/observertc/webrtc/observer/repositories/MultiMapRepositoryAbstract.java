/*
 * Copyright  2020 Balazs Kreith
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.observertc.webrtc.observer.repositories;


import com.hazelcast.multimap.MultiMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.validation.constraints.NotNull;
import org.observertc.webrtc.observer.ObserverHazelcast;

public class MultiMapRepositoryAbstract<K, V> implements IMultiMap<K, V> {
	private final ObserverHazelcast observerHazelcast;
	private final MultiMap<K, V> entities;
	private final long operationTimeoutInMs = 3000L;

	public MultiMapRepositoryAbstract(ObserverHazelcast observerHazelcast, String name) {
		this.observerHazelcast = observerHazelcast;
		this.entities = observerHazelcast.getInstance().getMultiMap(name);
	}

	@Override
	public <R extends K, U extends V> void add(@NotNull R key, @NotNull U entity) {
		this.entities.put(key, entity);
	}

	@Override
	public <R extends K, U extends V> void addAll(@NotNull R key, @NotNull Collection<U> entities) {
		AtomicBoolean executed = new AtomicBoolean(false);
		Object signal = new Object();
		this.entities.putAllAsync(key, entities).thenRun(() -> {
			synchronized (signal) {
				signal.notify();
				executed.set(true);
			}

		});
		if (!executed.get()) {
			synchronized (signal) {
				if (executed.get()) {
					return;
				}
				try {
					signal.wait(this.operationTimeoutInMs);
				} catch (InterruptedException e) {
					throw new RuntimeException(e);
				}
			}
		}
		if (!executed.get()) {
			throw new RuntimeException("The operation has not performed in the allowed time, and therefore it cannot be guaranteed that " +
					"data has been saved");
		}
	}

	public <R extends K, U extends V> CompletionStage<Void> addAllAsync(@NotNull R key, @NotNull Collection<U> entities) {
		return this.entities.putAllAsync(key, entities);
	}

	@Override
	public <S extends K> Collection<V> find(@NotNull S key) {
		Collection<V> result = this.entities.get(key);
		if (Objects.isNull(result)) {
			return Collections.unmodifiableCollection(new ArrayList<>());
		}
		return result;
	}

	@Override
	public Map<K, Collection<V>> findAll(@NotNull Set<K> keys) {
		return keys.stream().collect(Collectors.toMap(
				Function.identity(),
				key -> this.entities.get(key)
		));
	}

	@Override
	public Map<K, Collection<V>> getLocalEntries() {
		Set<K> keys = this.entities.localKeySet();
		return this.findAll(keys);
	}

	@Override
	public <S extends K> boolean exists(@NotNull S key) {
		return this.entities.containsKey(key);
	}

	@Override
	public boolean existsAll(@NotNull Set<K> keys) {
		return keys.stream().allMatch(this.entities::containsKey);
	}

	@Override
	public <S extends K> void delete(@NotNull S key) {
		this.entities.delete(key);
	}

	@Override
	public <S extends K> void deleteAll(@NotNull Set<S> keys) {
		keys.stream().forEach(this.entities::delete);
	}

	@Override
	public <R extends K, U extends V> boolean remove(@NotNull R key, @NotNull U entity) {
		return this.entities.remove(key, entity);
	}

	@Override
	public <S extends K> Collection<V> removeAll(@NotNull K key) {
		Collection<V> result = this.entities.remove(key);
		if (Objects.isNull(result)) {
			return Collections.unmodifiableCollection(new ArrayList<>());
		}
		return result;
	}

	@Override
	public void purge() {
		this.entities.clear();
	}
}
