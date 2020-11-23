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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.inject.Singleton;
import org.observertc.webrtc.observer.EvaluatorsConfig;
import org.observertc.webrtc.observer.ServiceConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class ServicesRepository {
	private static final Logger logger = LoggerFactory.getLogger(ServicesRepository.class);

	private final Map<UUID, String> serviceMap = new HashMap<>();
	private final String defaultServiceName;

	public ServicesRepository(EvaluatorsConfig evaluatorsConfig,
							  List<ServiceConfiguration> services) {
		this.defaultServiceName = evaluatorsConfig.defaultServiceName;
		this.init(services);
	}

	public String getServiceName(UUID serviceUUID) {
		String result = this.serviceMap.getOrDefault(serviceUUID, this.defaultServiceName);
		return result;
	}

	private void init(List<ServiceConfiguration> services) {
		ServiceConfiguration defaultConfig = null;
		for (ServiceConfiguration serviceConfiguration : services) {
			if (serviceConfiguration.name == null) {
				logger.warn("name cannot be null for service");
				continue;
			}
			for (UUID uuid : serviceConfiguration.UUIDs) {
				if (this.serviceMap.containsKey(uuid)) {
					logger.warn("Duplicated UUID {} in serviceMap for names: {} (old), {} (new). " +
									"Actual behaviour: Overriding the old one.",
							serviceConfiguration.name, this.serviceMap.get(uuid));
				}
				this.serviceMap.put(uuid, serviceConfiguration.name);
			}
		}
	}

}
