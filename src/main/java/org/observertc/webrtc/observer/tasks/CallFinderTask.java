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

package org.observertc.webrtc.observer.tasks;

import io.micronaut.context.annotation.Prototype;
import io.reactivex.rxjava3.core.Observable;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.validation.constraints.NotNull;
import org.observertc.webrtc.observer.ObserverHazelcast;
import org.observertc.webrtc.observer.models.SynchronizationSourceEntity;
import org.observertc.webrtc.observer.repositories.hazelcast.CallNamesRepository;
import org.observertc.webrtc.observer.repositories.hazelcast.RepositoryProvider;
import org.observertc.webrtc.observer.repositories.hazelcast.SynchronizationSourcesRepository;

@Prototype
public class CallFinderTask extends TaskAbstract<Observable<UUID>> {

	private final ObserverHazelcast observerHazelcast;
	private final SynchronizationSourcesRepository SSRCRepository;
	private final CallNamesRepository callNamesRepository;
	private Set<Long> SSRCs;
	private String callName;
	private UUID serviceUUID;
	private boolean isMultipleResultsAllowed = true;

	public CallFinderTask(ObserverHazelcast observerHazelcast,
						  RepositoryProvider repositoryProvider
	) {
		super();
		this.observerHazelcast = observerHazelcast;
		this.SSRCRepository = repositoryProvider.getSSRCRepository();
		this.callNamesRepository = repositoryProvider.getCallNamesRepository();
	}

	public CallFinderTask forServiceUUID(@NotNull UUID serviceUUID) {
		this.serviceUUID = serviceUUID;
		return this;
	}

	public CallFinderTask forCallName(String callName) {
		this.callName = callName;
		return this;
	}

	public CallFinderTask withMultipleResultsAllowed(boolean value) {
		this.isMultipleResultsAllowed = value;
		return this;
	}

	public CallFinderTask forSSRCs(@NotNull Set<Long> SSRCs) {
		this.SSRCs = SSRCs;
		return this;
	}

	@Override
	protected Observable<UUID> doPerform() {
		try {
			Set<String> streamKeys = this.SSRCs.stream()
					.map(ssrc -> SynchronizationSourcesRepository.getKey(this.serviceUUID, ssrc))
					.collect(Collectors.toSet());
			Map<String, SynchronizationSourceEntity> activeStreams = this.SSRCRepository.rxFindAll(streamKeys)
					.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)).blockingGet();
//			activeStream.subscribe();
			// Find by SSRC
			if (activeStreams != null && 0 < activeStreams.size()) {
				Set<UUID> callUUIDs = activeStreams.values().stream()
						.map(c -> c.callUUID).collect(Collectors.toSet());
				return Observable.fromIterable(callUUIDs);
			}
			if (this.callName != null) {
				Set<UUID> callUUIDs = this.callNamesRepository.find(this.callName).stream().collect(Collectors.toSet());
				int numberOfCallUUIDs = callUUIDs.size();
				if (0 < numberOfCallUUIDs) {
					if (this.isMultipleResultsAllowed || numberOfCallUUIDs == 1) {
						return Observable.fromIterable(callUUIDs);
					}
				}
			}
			return Observable.empty();
		} catch (Exception ex) {
			return Observable.error(ex);
		} finally {

		}
	}

	@Override
	protected void validate() {
		super.validate();
		if (this.SSRCs == null) {
			throw new IllegalStateException("To perform the task it is required to have a SSRCs");
		}
		if (this.serviceUUID == null) {
			throw new IllegalStateException("To perform the task it is required to have a serviceUUID");
		}
	}
}