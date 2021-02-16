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

package org.observertc.webrtc.observer.evaluators;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.observertc.webrtc.observer.entities.OldPeerConnectionEntity;
import org.observertc.webrtc.observer.entities.SynchronizationSourceEntity;
import org.observertc.webrtc.observer.repositories.stores.PeerConnectionsRepository;
import org.observertc.webrtc.observer.repositories.stores.SynchronizationSourcesRepository;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

@MicronautTest
public class ActivePCsEvaluatorTest {

	static TestInputsGenerator generator = TestInputsGenerator.builder().build();

	@Inject
	Provider<ActivePCsEvaluator> subject;

	@Inject
	PeerConnectionsRepository peerConnectionsRepository;

	@Inject
	SynchronizationSourcesRepository synchronizationSourcesRepository;

	@Test
	public void shouldUpdateExistingPeerConnections() throws Throwable {
		// Given
		ActivePCsEvaluator evaluator = subject.get();
		SynchronizationSourceEntity ssrcEntity = generator.makeSynchronizationSourceEntity();
		OldPeerConnectionEntity pcEntity = generator.makePeerConnectionEntityFor(ssrcEntity);
		PCState pcState = generator.makePCStateFor(pcEntity, ssrcEntity);
		this.peerConnectionsRepository.save(pcEntity.peerConnectionUUID, pcEntity);
		this.synchronizationSourcesRepository.save(
				SynchronizationSourcesRepository.getKey(ssrcEntity.serviceUUID, ssrcEntity.SSRC),
				ssrcEntity
		);
		AtomicReference<Map<UUID, PCState>> newPcsHolder = new AtomicReference<>(null);

		// When
		evaluator.accept(Map.of(pcState.peerConnectionUUID, pcState));
//		evaluator.getObservableReports().subscribe(newPcsHolder::set);

		// Then
		Assertions.assertNull(newPcsHolder.get());
	}

	@Test
	public void shouldDetectNewPCs() throws Throwable {
		// Given
		ActivePCsEvaluator evaluator = subject.get();
		PCState pcState = generator.makePCState();

		// When
		evaluator.accept(Map.of(pcState.peerConnectionUUID, pcState));

		// Then
	}

}