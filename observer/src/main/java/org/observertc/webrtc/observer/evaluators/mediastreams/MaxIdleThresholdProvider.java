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

package org.observertc.webrtc.observer.evaluators.mediastreams;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.function.Supplier;
import javax.inject.Singleton;
import org.observertc.webrtc.observer.EvaluatorsConfig;
import org.observertc.webrtc.observer.jooq.tables.records.PeerconnectionsRecord;
import org.observertc.webrtc.observer.repositories.PeerConnectionsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
class MaxIdleThresholdProvider implements Supplier<Optional<Long>> {

	private static final Logger logger = LoggerFactory.getLogger(MaxIdleThresholdProvider.class);
	private final EvaluatorsConfig.CallCleanerConfig config;
	private final PeerConnectionsRepository peerConnectionsRepository;
	private volatile boolean noJoinedPCLogged = false;
	private volatile boolean joinedPCIsTooOldLogged = false;

	public MaxIdleThresholdProvider(
			EvaluatorsConfig.CallCleanerConfig config,
			PeerConnectionsRepository peerConnectionsRepository) {
		this.config = config;
		this.peerConnectionsRepository = peerConnectionsRepository;
	}

	@Override
	public Optional<Long> get() {
		Instant now = Instant.now();
		Optional<PeerconnectionsRecord> lastJoinedPCHolder = this.peerConnectionsRepository.getLastJoinedPC();
		if (!lastJoinedPCHolder.isPresent()) {
			if (!this.noJoinedPCLogged) {
				logger.info("No previous joined PC updated field can be used," +
						" thus the threshold to declare PC detached is based on wall clock");
				this.noJoinedPCLogged = true;
			}
			return Optional.of(now.minus(this.config.streamMaxIdleTimeInS, ChronoUnit.SECONDS).toEpochMilli());
		}
		this.noJoinedPCLogged = false;
		PeerconnectionsRecord lastJoinedPC = lastJoinedPCHolder.get();
		Long lastUpdateInEpoch = lastJoinedPC.getUpdated();
		Instant lastUpdate = Instant.ofEpochMilli(lastUpdateInEpoch);
		long elapsedTimeInS = ChronoUnit.SECONDS.between(lastUpdate, now);
		if (this.config.streamMaxAllowedGapInS < elapsedTimeInS) {
			if (this.joinedPCIsTooOldLogged) {
				logger.info("The last updated PC updated time ({}) is older than the actual wall clock time minus the max allowed time gap in" +
						"seconds {}, thereby the " +
						"actual wall clock is used as thresholds for detached PCs", lastUpdate, this.config.streamMaxAllowedGapInS);
				this.joinedPCIsTooOldLogged = true;
			}
			return Optional.of(now.minus(this.config.streamMaxIdleTimeInS, ChronoUnit.SECONDS).toEpochMilli());
		}
		this.joinedPCIsTooOldLogged = false;
		return Optional.of(lastUpdate.minus(this.config.streamMaxIdleTimeInS, ChronoUnit.SECONDS).toEpochMilli());
	}

}
