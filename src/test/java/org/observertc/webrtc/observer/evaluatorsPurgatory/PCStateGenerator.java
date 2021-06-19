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

package org.observertc.webrtc.observer.evaluatorsPurgatory;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import org.jeasy.random.EasyRandom;
import org.jeasy.random.EasyRandomParameters;
import org.jeasy.random.api.Randomizer;
import org.jeasy.random.randomizers.misc.UUIDRandomizer;
import org.jeasy.random.randomizers.number.LongRandomizer;
import org.jeasy.random.randomizers.time.TimeZoneRandomizer;
import org.observertc.webrtc.observer.evaluatorsPurgatory.pcSampleToReportsV2.PCState;

public class PCStateGenerator {

	private final EasyRandom generator;
	private final Randomizer<Set<Long>> SSRCsRandomizer;

	public PCStateGenerator(Instant startDate, Instant endDate, Set<Long> SSRCs, List<UUID> peerConnections) {
		this.SSRCsRandomizer = this.makeSSRCsRandomizer(SSRCs);
		EasyRandomParameters parameters = new EasyRandomParameters()
				.randomize(field ->
								field.getName().equals("updated") ||
										field.getName().equals("created"),
						new Randomizer<Long>() {
							@Override
							public Long getRandomValue() {
								Instant randomDate = between(startDate, endDate);
								return randomDate.toEpochMilli();
							}
						})
				.randomize(field ->
								field.getName().equals("timeZoneID"),
						new Randomizer<String>() {
							TimeZoneRandomizer randomizer = new TimeZoneRandomizer();

							@Override
							public String getRandomValue() {
								return randomizer.getRandomValue().toZoneId().toString();
							}
						})
				.randomize(field ->
								field.getName().equals("peerConnectionUUID"),
						new Randomizer<UUID>() {
							UUIDRandomizer randomizer = new UUIDRandomizer();

							@Override
							public UUID getRandomValue() {
								if (peerConnections == null || peerConnections.size() < 1) {
									return randomizer.getRandomValue();
								}
								if (peerConnections.size() < 2) {
									return peerConnections.get(0);
								}
								int index = new Random().nextInt(peerConnections.size());
								UUID result = peerConnections.get(index);
								return result;
							}
						})
				//
				;
		this.generator = new EasyRandom(parameters);
	}

	public PCState getNext() {
		PCState result = this.generator.nextObject(PCState.class);
		result.SSRCs = this.SSRCsRandomizer.getRandomValue();
		return result;
	}

	public<T> T getNext(Class<T> klass) {
		return this.generator.nextObject(klass);
	}

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {
		private final Map<String, Object> values;
		private static final String START_DATE = "startDate";
		private static final String END_DATE = "endDate";
		private static final String SSRCs = "SSRCs";
		private static final String PEER_CONNECTIONS_LIST = "peerConnectionsList";

		public Builder() {
			this.values = new HashMap<>();
			this.values.put(START_DATE, Instant.now().minus(Duration.ofSeconds(60)));
			this.values.put(END_DATE, Instant.now());
		}

		public Builder withStartDate(Instant value) {
			this.values.put(START_DATE, value);
			return this;
		}

		public Builder withEndDate(Instant value) {
			this.values.put(END_DATE, value);
			return this;
		}

		public Builder withSSRCs(Set<Long> value) {
			this.values.put(SSRCs, value);
			return this;
		}

		public Builder withPeerConnections(List<UUID> value) {
			this.values.put(PEER_CONNECTIONS_LIST, value);
			return this;
		}

		public PCStateGenerator build() {
			Instant startDate = (Instant) this.values.get(START_DATE);
			Instant endDate = (Instant) this.values.get(END_DATE);
			Set<Long> SSRCSet = (Set<Long>) this.values.get(SSRCs);
			List<UUID> peerConnections = (List<UUID>) this.values.get(PEER_CONNECTIONS_LIST);
			return new PCStateGenerator(startDate, endDate, SSRCSet, peerConnections);
		}
	}

	private Randomizer<Set<Long>> makeSSRCsRandomizer(Set<Long> SSRCs) {
		return new Randomizer<Set<Long>>() {
			LongRandomizer randomizer = new LongRandomizer();

			@Override
			public Set<Long> getRandomValue() {
				if (SSRCs != null) {
					return new HashSet<>(SSRCs);
				}
				HashSet<Long> result = new HashSet<>();
				long c = (Math.abs(randomizer.getRandomValue()) % 8L) + 1;
				for (long i = 0; i < c; ++i) {
					Long SSRC = Math.abs(randomizer.getRandomValue());
					result.add(SSRC);
				}
				return result;
			}
		};
	}

	private static Instant between(Instant startInclusive, Instant endExclusive) {
		long startSeconds = startInclusive.getEpochSecond();
		long endSeconds = endExclusive.getEpochSecond();
		long random = ThreadLocalRandom
				.current()
				.nextLong(startSeconds, endSeconds);

		return Instant.ofEpochSecond(random);
	}
}
