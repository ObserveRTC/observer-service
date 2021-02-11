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
import org.observertc.webrtc.observer.ObserverHazelcast;
import org.observertc.webrtc.observer.common.TaskAbstract;
import org.observertc.webrtc.observer.entities.SynchronizationSourceEntity;
import org.observertc.webrtc.observer.repositories.stores.CallNamesRepository;
import org.observertc.webrtc.observer.repositories.stores.RepositoryProvider;
import org.observertc.webrtc.observer.repositories.stores.SynchronizationSourcesRepository;

import javax.validation.constraints.NotNull;
import java.util.*;
import java.util.stream.Collectors;

@Prototype
public class SSRCEntityFinderTask extends TaskAbstract<Set<UUID>> {
	private final ObserverHazelcast observerHazelcast;
	private final SynchronizationSourcesRepository SSRCRepository;
	private final CallNamesRepository callNamesRepository;
	private Set<Long> SSRCs;
	private String callName;
	private UUID serviceUUID;
	private boolean isMultipleResultsAllowed = true;

	public SSRCEntityFinderTask(ObserverHazelcast observerHazelcast,
								RepositoryProvider repositoryProvider
	) {
		super();
		this.observerHazelcast = observerHazelcast;
		this.SSRCRepository = repositoryProvider.getSSRCRepository();
		this.callNamesRepository = repositoryProvider.getCallNamesRepository();
	}

	public SSRCEntityFinderTask forServiceUUID(@NotNull UUID serviceUUID) {
		this.serviceUUID = serviceUUID;
		return this;
	}

	public SSRCEntityFinderTask forCallName(String callName) {
		this.callName = callName;
		return this;
	}

	public SSRCEntityFinderTask withMultipleResultsAllowed(boolean value) {
		this.isMultipleResultsAllowed = value;
		return this;
	}

	public SSRCEntityFinderTask forSSRCs(@NotNull Set<Long> SSRCs) {
		this.SSRCs = SSRCs;
		return this;
	}

	@Override
	protected Set<UUID> perform() {
		Set<String> streamKeys = this.SSRCs.stream()
				.map(ssrc -> SynchronizationSourcesRepository.getKey(this.serviceUUID, ssrc))
				.collect(Collectors.toSet());
		Map<String, SynchronizationSourceEntity> activeStreams = this.SSRCRepository.rxFindAll(streamKeys)
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)).blockingGet();
//			activeStream.subscribe();
		// Find by SSRC
		if (activeStreams != null && 0 < activeStreams.size()) {
			Set<UUID> callUUIDs = activeStreams.values().stream()
					.map(c -> c.callUUID).collect(Collectors.toSet())
					;
			return callUUIDs;
		}
		if (this.callName != null) {
			Set<UUID> callUUIDs = this.callNamesRepository.find(this.callName).stream().collect(Collectors.toSet());
			int numberOfCallUUIDs = callUUIDs.size();
			if (0 < numberOfCallUUIDs) {
				if (this.isMultipleResultsAllowed || numberOfCallUUIDs == 1) {
					return callUUIDs;
				}
			}
		}
		return Collections.EMPTY_SET;
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