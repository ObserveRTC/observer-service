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
import org.observertc.webrtc.observer.common.TaskAbstract;
import org.observertc.webrtc.observer.models.CallEntity;
import org.observertc.webrtc.observer.models.SynchronizationSourceEntity;
import org.observertc.webrtc.observer.repositories.hazelcast.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.NotNull;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

@Prototype
public class CallInitializerTask extends TaskAbstract<UUID> {
	private static final Logger DEFAULT_LOGGER = LoggerFactory.getLogger(CallInitializerTask.class);
	private static final String LOCK_NAME = CallInitializerTask.class.getSimpleName() + "-lock";
	private enum State {
		CREATED,
		CALL_ENTITY_IS_REGISTERED,
		CALL_NAME_IS_REGISTERED,
		SSRCS_ARE_REGISTERED,
		CALL_TO_SSRCS_IS_REGISTERED,
		EXECUTED,
		ROLLEDBACK,
	}




	private final CallEntitiesRepository callEntitiesRepository;
	private final SynchronizationSourcesRepository SSRCRepository;
	private final CallFinderTask callFinderTask;
	private final CallNamesRepository callNamesRepository;
	private final CallSynchronizationSourcesRepository callSynchronizationSourcesRepository;
	private final WeakLockProvider lockProvider;
	private CallEntity callEntity;
	private Set<Long> SSRCs;
	private State state = State.CREATED;
	private final long operationTimeoutInMs = 10000L;


	public CallInitializerTask(
			RepositoryProvider repositoryProvider,
			CallFinderTask callFinderTask,
			WeakLockProvider lockProvider
	) {
		super();
		this.lockProvider = lockProvider;
		this.callFinderTask = callFinderTask;
		this.callSynchronizationSourcesRepository = repositoryProvider.getCallSynchronizationSourcesRepository();
		this.callEntitiesRepository = repositoryProvider.getCallEntitiesRepository();
		this.SSRCRepository = repositoryProvider.getSSRCRepository();
		this.callNamesRepository = repositoryProvider.getCallNamesRepository();
		this.setDefaultLogger(DEFAULT_LOGGER);
	}

	public CallInitializerTask forCallEntity(CallEntity callEntity) {
		this.callEntity = callEntity;
		return this;
	}

	public CallInitializerTask forSSRCs(@NotNull Set<Long> SSRCs) {
		this.SSRCs = SSRCs;
		return this;
	}


	@Override
	protected UUID perform() throws Throwable {
//		try (FencedLockAcquirer lock = this.lockProvider.get().forLockName(LOCK_NAME).acquire()) {
		try (var lock = this.lockProvider.autoLock(LOCK_NAME)) {
			Set<UUID> callUUIDs = this.callFinderTask.forSSRCs(this.SSRCs)
					.forServiceUUID(this.callEntity.serviceUUID)
					.forCallName(this.callEntity.callName)
					.withMultipleResultsAllowed(false)
					.execute().getResultOrDefault(Collections.EMPTY_SET);

			if (0 < callUUIDs.size()) {
				UUID result = callUUIDs.stream().findFirst().get();
				return result;
			}

			this.doPerform();
			return this.callEntity.callUUID;

		}
	}

	private void doPerform() {
		switch (this.state) {
			default:
			case CREATED:
				this.registerCallEntity();
				this.state = State.CALL_ENTITY_IS_REGISTERED;
			case CALL_ENTITY_IS_REGISTERED:
				this.registerCallName();
				this.state = State.CALL_NAME_IS_REGISTERED;
			case CALL_NAME_IS_REGISTERED:
				this.registerSSRRCs();
				this.state = State.SSRCS_ARE_REGISTERED;
			case SSRCS_ARE_REGISTERED:
				this.registerCallToSSRCs();
				this.state = State.CALL_TO_SSRCS_IS_REGISTERED;
			case CALL_TO_SSRCS_IS_REGISTERED:
				this.state = State.EXECUTED;
			case EXECUTED:
			case ROLLEDBACK:
				return;
		}
	}

	@Override
	protected void rollback(Throwable t) {
		try {
			switch (this.state) {
				case EXECUTED:
				case CALL_TO_SSRCS_IS_REGISTERED:
					this.unregisterCallToSSRCs(t);
					this.state = State.SSRCS_ARE_REGISTERED;
				case SSRCS_ARE_REGISTERED:
					this.unregisterSSRCs(t);
					this.state = State.CALL_NAME_IS_REGISTERED;
				case CALL_NAME_IS_REGISTERED:
					this.unregisterCallName(t);
					this.state = State.CALL_ENTITY_IS_REGISTERED;
				case CALL_ENTITY_IS_REGISTERED:
					this.unregisterCallEntity(t);
					this.state = State.ROLLEDBACK;
				case CREATED:
				case ROLLEDBACK:
				default:
					return;
			}
		} catch (Throwable another) {
			this.getLogger().error("During rollback an error is occured", another);
		}

	}

	private void registerCallEntity() {
		this.callEntitiesRepository.add(this.callEntity.callUUID, this.callEntity);
	}

	private void unregisterCallEntity(Throwable exceptionInExecution) {
		this.callEntitiesRepository.delete(this.callEntity.callUUID);
	}

	private void registerCallName() {
		if (Objects.isNull(this.callEntity.callName)) {
			return;
		}
		this.callNamesRepository.add(this.callEntity.callName, this.callEntity.callUUID);
	}

	private void unregisterCallName(Throwable exceptionInExecution) {
		if (Objects.isNull(this.callEntity.callName)) {
			return;
		}
		this.callNamesRepository.remove(this.callEntity.callName, this.callEntity.callUUID);
	}

	private void registerSSRRCs() {
		Map<String, SynchronizationSourceEntity> synchronizationSourceEntities =
				this.SSRCs.stream()
						.collect(Collectors.toMap(
								ssrc -> SynchronizationSourcesRepository.getKey(this.callEntity.serviceUUID, ssrc),
								ssrc -> SynchronizationSourceEntity.of(this.callEntity.serviceUUID, ssrc, this.callEntity.callUUID)
						));
		this.SSRCRepository.saveAll(synchronizationSourceEntities);
	}

	private void unregisterSSRCs(Throwable exceptionInExecution) {
		Set<String> streamKeys = this.SSRCs.stream().map(SSRC -> SynchronizationSourcesRepository.getKey(this.callEntity.serviceUUID,
				SSRC)).collect(Collectors.toSet());
		this.SSRCRepository.deleteAll(streamKeys);
	}

	private void registerCallToSSRCs() {
		AtomicBoolean performed = new AtomicBoolean(false);
		Set<String> synchronizationSourceKeys =
				this.SSRCs.stream()
						.map(ssrc -> SynchronizationSourcesRepository.getKey(this.callEntity.serviceUUID, ssrc))
						.collect(Collectors.toSet());
		this.callSynchronizationSourcesRepository
				.addAll(this.callEntity.callUUID, synchronizationSourceKeys);
	}

	private void unregisterCallToSSRCs(Throwable exceptionInExecution) {
		this.callSynchronizationSourcesRepository.removeAll(this.callEntity.callUUID);
	}

	@Override
	protected void validate() {
		super.validate();
		if (this.callEntity == null) {
			throw new IllegalStateException("To perform the task it is required to have a callEntity");
		}
		if (this.SSRCs == null) {
			throw new IllegalStateException("To perform the task it is required to have a SSRCs");
		}
	}


}