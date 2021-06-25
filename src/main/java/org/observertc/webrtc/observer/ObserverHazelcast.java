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

package org.observertc.webrtc.observer;

import com.hazelcast.config.Config;
import com.hazelcast.config.XmlConfigBuilder;
import com.hazelcast.config.YamlConfigBuilder;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.cp.CPSubsystem;
import org.observertc.webrtc.observer.configs.ObserverConfig;
import org.observertc.webrtc.observer.dto.PortableDTOFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Singleton;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.UUID;

@Singleton
public class ObserverHazelcast {

	private static final Logger logger = LoggerFactory.getLogger(ObserverHazelcast.class);

	private final HazelcastInstance instance;

	public ObserverHazelcast(ObserverConfig.HazelcastConfig observerHazelcastConfig) {
		Config config = this.makeConfig(observerHazelcastConfig);
		this.instance = Hazelcast.newHazelcastInstance(config);
	}

	@PostConstruct
	void setup() {

	}

	@PreDestroy
	void teardown() {

	}

	public HazelcastInstance getInstance() {
		return this.instance;
	}

	public CPSubsystem getCPSubsystem() {
		return this.instance.getCPSubsystem();
	}

	public int getSessionTimeToLiveSeconds() {
		return this.instance.getConfig().getCPSubsystemConfig().getSessionTimeToLiveSeconds();
	}

	@PreDestroy
	void close() {
		if (this.instance != null) {
			this.instance.shutdown();
		}
	}

	private Config makeConfig(ObserverConfig.HazelcastConfig observerHazelcastConfig) {
		String configPath = observerHazelcastConfig.configFile;
		// From ClassLoader, all paths are "absolute" already - there's no context
// from which they could be relative. Therefore you don't need a leading slash.

		Config result;
		if (configPath == null) {
			result = new XmlConfigBuilder().build();
		} else {
			InputStream inputStream = null;
			if (configPath.startsWith("classpath:")) {
				configPath = configPath.substring(10);
				inputStream = this.getClass().getClassLoader()
						.getResourceAsStream(configPath);
			}

			try {
				if (inputStream == null) {
					inputStream = new FileInputStream(configPath);
				}
				YamlConfigBuilder builder = new YamlConfigBuilder(inputStream);
				result = builder.build();
				logger.info("Hazelcast config file at {} is loaded", configPath);
			} catch (FileNotFoundException e) {
				logger.warn("Config file cannot be loaded", e);
				result = new XmlConfigBuilder().build();
			}
		}

		result.getSerializationConfig().addPortableFactory(PortableDTOFactory.FACTORY_ID, new PortableDTOFactory());
		return result;
	}

	@Override
	public String toString() {
		return this.instance.getConfig().toString();
	}

	public UUID getLocalEndpointUUID() {
		return this.instance.getLocalEndpoint().getUuid();
	}
}
