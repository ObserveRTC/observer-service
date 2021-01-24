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

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

@Prototype
public class CallFinisherTask extends TaskAbstract<CallEntity> {
	private static final Logger DEFAULT_LOGGER = LoggerFactory.getLogger(CallFinisherTask.class);
	private static final String LOCK_NAME = CallFinisherTask.class.getCanonicalName() + "-lock";
	private enum State {
		CREATED,
		CALL_ENTITY_IS_UNREGISTERED,
		CALL_NAME_IS_UNREGISTERED,
		SSRCS_ARE_UNREGISTERED,
		CALL_TO_SSRCS_IS_UNREGISTERED,
		EXECUTED,
		ROLLEDBACK,
	}


	private final CallSynchronizationSourcesRepository callSynchronizationSourcesRepository;
	private final CallEntitiesRepository callEntitiesRepository;
	private final SynchronizationSourcesRepository SSRCRepository;
	private final SSRCEntityFinderTask SSRCEntityFinderTask;
	private final CallNamesRepository callNamesRepository;
	private final WeakLockProvider lockProvider;
	private final long operationTimeoutInMs = 10000;
	private UUID callUUID;
	private Set<Long> unregisteredSSRCs;
	private Set<String> unregisteredSSRCKeys;
	private CallEntity unregisteredCallEntity;
	private State state = State.CREATED;


	public CallFinisherTask(
			RepositoryProvider repositoryProvider,
			SSRCEntityFinderTask SSRCEntityFinderTask,
			WeakLockProvider lockProvider
	) {
		super();
		this.lockProvider = lockProvider;
		this.SSRCEntityFinderTask = SSRCEntityFinderTask;
		this.callEntitiesRepository = repositoryProvider.getCallEntitiesRepository();
		this.SSRCRepository = repositoryProvider.getSSRCRepository();
		this.callNamesRepository = repositoryProvider.getCallNamesRepository();
		this.callSynchronizationSourcesRepository = repositoryProvider.getCallSynchronizationSourcesRepository();
		this.setDefaultLogger(DEFAULT_LOGGER);
	}

	public CallFinisherTask forCallEntity(UUID callUUID) {
		this.callUUID = callUUID;
		return this;
	}

	@Override
	protected CallEntity perform() throws Exception {
//		try (FencedLockAcquirer lock = this.lockProvider.get().forLockName(LOCK_NAME).acquire()) {
		try (var lock = this.lockProvider.autoLock(LOCK_NAME)) {
			Optional<CallEntity> entityHolder = this.callEntitiesRepository.find(this.callUUID);

			if (!entityHolder.isPresent()) {
				return null;
			}

			this.unregisteredCallEntity = entityHolder.get();
			this.doPerform();
			return this.unregisteredCallEntity;
		}
	}

	private void doPerform() {
		switch (this.state) {
			case CREATED:
				this.unregisteredSSRCKeys = this.callSynchronizationSourcesRepository
						.removeAll(this.callUUID).stream().collect(Collectors.toSet());
				this.state = State.CALL_TO_SSRCS_IS_UNREGISTERED;
			case CALL_TO_SSRCS_IS_UNREGISTERED:
				this.unregisterSSRCs();
				this.state = State.SSRCS_ARE_UNREGISTERED;
			case SSRCS_ARE_UNREGISTERED:
				if (Objects.nonNull(this.unregisteredCallEntity.callName)) {
					this.callNamesRepository.remove(this.unregisteredCallEntity.callName, this.unregisteredCallEntity.callUUID);
				}
				this.state = State.CALL_NAME_IS_UNREGISTERED;
			case CALL_NAME_IS_UNREGISTERED:
				this.callEntitiesRepository.delete(this.unregisteredCallEntity.callUUID);
				this.state = State.CALL_ENTITY_IS_UNREGISTERED;
			case CALL_ENTITY_IS_UNREGISTERED:
				this.state = State.EXECUTED;
			case ROLLEDBACK:
			case EXECUTED:
			default:
				return;
		}

	}

	@Override
	protected void rollback(Throwable t) {
		switch (this.state) {
			case EXECUTED:
			case CALL_TO_SSRCS_IS_UNREGISTERED:
				this.registerCallToSSRCs(t);
				this.state = State.SSRCS_ARE_UNREGISTERED;
			case SSRCS_ARE_UNREGISTERED:
				this.registerSSRRCs(t);
				this.state = State.CALL_NAME_IS_UNREGISTERED;
			case CALL_NAME_IS_UNREGISTERED:
				if (Objects.nonNull(this.unregisteredCallEntity.callName)) {
					this.callNamesRepository.add(this.unregisteredCallEntity.callName, this.unregisteredCallEntity.callUUID);
				}
				this.state = State.CALL_ENTITY_IS_UNREGISTERED;
			case CALL_ENTITY_IS_UNREGISTERED:
				this.callEntitiesRepository.save(this.unregisteredCallEntity.callUUID, this.unregisteredCallEntity);
				this.state = State.ROLLEDBACK;
			default:
			case CREATED:
			case ROLLEDBACK:
				return;
		}

	}


	private void registerSSRRCs(Throwable exceptionInExecution) {
		Map<String, SynchronizationSourceEntity> synchronizationSourceEntities =
				this.unregisteredSSRCs.stream()
						.collect(Collectors.toMap(
								ssrc -> SynchronizationSourcesRepository.getKey(this.unregisteredCallEntity.serviceUUID, ssrc),
								ssrc -> SynchronizationSourceEntity.of(this.unregisteredCallEntity.serviceUUID, ssrc,
										this.unregisteredCallEntity.callUUID)));
		this.SSRCRepository.saveAll(synchronizationSourceEntities);
	}

	private void unregisterSSRCs() {
		if (this.unregisteredSSRCKeys.size() < 1) {
			return;
		}
		Map<String, SynchronizationSourceEntity> entities = this.SSRCRepository.findAll(this.unregisteredSSRCKeys);
		this.unregisteredSSRCs = entities.values().stream().map(ssrc -> ssrc.SSRC).collect(Collectors.toSet());
		if (unregisteredSSRCs.size() < 1) {
			this.getLogger().warn("There was no SSRC for call {} ", this.callUUID);
		}
		this.SSRCRepository.deleteAll(this.unregisteredSSRCKeys);
	}

	private void registerCallToSSRCs(Throwable exceptionInExecution) {
		AtomicBoolean performed = new AtomicBoolean(false);
		try {
			this.callSynchronizationSourcesRepository
					.addAllAsync(this.callUUID, this.unregisteredSSRCKeys)
					.thenRun(() -> performed.set(true))
					.wait(this.operationTimeoutInMs);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
		if (!performed.get()) {
			throw new RuntimeException("The operation has not been finished properly");
		}
	}

	@Override
	protected void validate() {
		super.validate();
		Objects.requireNonNull(this.callUUID);
	}

}