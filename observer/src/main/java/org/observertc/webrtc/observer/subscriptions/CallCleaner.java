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

package org.observertc.webrtc.observer.subscriptions;

import io.micronaut.context.annotation.Requires;
import io.micronaut.context.env.Environment;
import io.micronaut.context.event.ApplicationEventListener;
import io.micronaut.discovery.event.ServiceReadyEvent;
import io.micronaut.scheduling.annotation.Async;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import javax.inject.Singleton;
import org.observertc.webrtc.common.Sleeper;
import org.observertc.webrtc.common.UUIDAdapter;
import org.observertc.webrtc.observer.EvaluatorsConfig;
import org.observertc.webrtc.observer.evaluators.ExpiredPCsEvaluator;
import org.observertc.webrtc.observer.jooq.tables.records.PeerconnectionsRecord;
import org.observertc.webrtc.observer.repositories.PeerConnectionsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
@Requires(notEnv = Environment.TEST) // Don't load data in tests.
public class CallCleaner implements ApplicationEventListener<ServiceReadyEvent> {

	private static final Logger logger = LoggerFactory.getLogger(CallCleaner.class);
	private final EvaluatorsConfig.CallCleanerConfig config;
	private final PeerConnectionsRepository peerConnectionsRepository;
	private final MaxIdleThresholdProvider maxIdleThresholdProvider;
	private final ExpiredPCsEvaluator expiredPCsEvaluator;

	public CallCleaner(
			ExpiredPCsEvaluator expiredPCsEvaluator,
			EvaluatorsConfig config,
			PeerConnectionsRepository peerConnectionsRepository,
			MaxIdleThresholdProvider maxIdleThresholdProvider) {
		this.config = config.callCleaner;
		this.expiredPCsEvaluator = expiredPCsEvaluator;
		this.peerConnectionsRepository = peerConnectionsRepository;
		this.maxIdleThresholdProvider = maxIdleThresholdProvider;
	}

	@Override
	@Async
	public void onApplicationEvent(ServiceReadyEvent event) {
		// To avoid execute the same query from different observer if several observer 
		// starts at the same time
		int sleepTimeInMs = new Random().nextInt(120_000) + 100;
		new Sleeper(() -> sleepTimeInMs, true).run();
		Long threshold = Instant.now().minus(this.config.pcRetentionTimeInDays, ChronoUnit.DAYS).toEpochMilli();
		try {
			this.peerConnectionsRepository.deletePCsDetachedOlderThan(threshold);
		} catch (Exception ex) {
			logger.error("Cannot execute remove old PCs", ex);
		}

		try {
			this.cleanCalls();
		} catch (Exception ex) {
			logger.error("Cannot execute clean detached PCs", ex);
		}
	}

	private void cleanCalls() {
		Optional<Long> thresholdHolder = this.maxIdleThresholdProvider.get();
		if (!thresholdHolder.isPresent()) {
			return;
		}
		Long threshold = thresholdHolder.get();
		Iterator<PeerconnectionsRecord> it = this.peerConnectionsRepository.findJoinedPCsUpdatedLowerThan(threshold).iterator();
		List<UUID> expiredPCs = new LinkedList<>();
		for (; it.hasNext(); ) {
			PeerconnectionsRecord record = it.next();
			UUID pcUUID = UUIDAdapter.toUUIDOrDefault(record.getPeerconnectionuuid(), null);
			if (pcUUID == null) {
				logger.warn("There was a null amongst the expired PCs.");
				continue;
			}
			expiredPCs.add(pcUUID);
		}
		this.expiredPCsEvaluator.onNext(expiredPCs);
	}


}
