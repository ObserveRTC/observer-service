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

package org.observertc.webrtc.observer.entities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

// To avoid exposing hazelcast serialization specific fields
public class ServiceMapEntity {
	private static final Logger logger = LoggerFactory.getLogger(SentinelEntity.class);

	public final String name;
	public final Set<UUID> uuids;


	public static ServiceMapEntity.Builder builder() { return new ServiceMapEntity.Builder();}

	public static ServiceMapEntity.Builders builders() { return new ServiceMapEntity.Builders();}

	public ServiceMapEntity(String name, Set<UUID> uuids) {
		this.name = name;
		this.uuids = uuids;
	}

	public static ServiceMapEntity from(ServiceMapEntity.DTO serviceMapDTO) {
		return new ServiceMapEntity.Builder(serviceMapDTO).build();
	}

	public static class DTO {
		public String name;
		public Set<UUID> uuids = new HashSet<>();
	}

	public static class Builder extends DTO {

		private Builder(DTO source) {
			this.name = source.name;
			this.uuids = source.uuids;
		}

		public Builder() {

		}
		public ServiceMapEntity build() {
			Objects.requireNonNull(this.name);
			Objects.requireNonNull(this.uuids);
			return new ServiceMapEntity(this.name, this.uuids);
		}

		public Builder withServiceName(String serviceName) {
			this.name = serviceName;
			return this;
		}

		public Builder withUUID(UUID uuid) {
			this.uuids.add(uuid);
			return this;
		}

		public Builder withUUIDs(Collection<UUID> uuids) {
			this.uuids.addAll(uuids);
			return this;
		}
	}

	public static class Builders {
		private final Map<String, ServiceMapEntity.Builder> builders = new HashMap<>();

		public Builders withUUID(String serviceName, UUID serviceUUID) {
			ServiceMapEntity.Builder builder = this.builders.get(serviceName);
			if (Objects.isNull(builder)) {
				builder = new Builder().withServiceName(serviceName);
				this.builders.put(serviceName, builder);
			}
			builder.withUUID(serviceUUID);
			return this;
		}

		public Builders withUUIDs(String serviceName, Collection<UUID> serviceUUIDs) {
			ServiceMapEntity.Builder builder = this.builders.get(serviceName);
			if (Objects.isNull(builder)) {
				builder = new Builder().withServiceName(serviceName);
				this.builders.put(serviceName, builder);
			}
			builder.withUUIDs(serviceUUIDs);
			return this;
		}

		public Map<String, ServiceMapEntity> build() {
			return this.builders.values().stream()
					.map(builder -> builder.build())
					.collect(Collectors.toMap(
							entity -> entity.name,
							Function.identity()
					));
		}
	}
}
