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

package org.observertc.observer;

import com.hazelcast.config.Config;
import com.hazelcast.config.XmlConfigBuilder;
import com.hazelcast.config.YamlConfigBuilder;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.cp.CPSubsystem;
import com.hazelcast.logging.LogEvent;
import com.hazelcast.logging.LogListener;
import org.observertc.observer.configs.ObserverConfig;
import org.observertc.observer.dto.PortableDTOFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Singleton;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Objects;
import java.util.UUID;
import java.util.logging.Level;

@Singleton
public class ObserverHazelcast {

	private static final Logger logger = LoggerFactory.getLogger(ObserverHazelcast.class);

	private final HazelcastInstance instance;
	private final String memberName;

	public ObserverHazelcast(ObserverConfig.HazelcastConfig observerHazelcastConfig) {
		Config config = this.makeConfig(observerHazelcastConfig);
		this.instance = Hazelcast.newHazelcastInstance(config);
		this.memberName = this.makeMemberName(observerHazelcastConfig);
		if (Objects.nonNull(observerHazelcastConfig.logs)) {
			observerHazelcastConfig.logs.stream().forEach(this::addLogPrinters);
		}
	}

	@PostConstruct
	void setup() {
		logger.info("Hazelcast configuration: {}", this.toString());
		logger.info("{} is ready", this.memberName);
	}

	@PreDestroy
	void teardown() {
		logger.info("Closed");
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

	public String makeMemberName(ObserverConfig.HazelcastConfig hazelcastConfig) {
		UUID memberId = this.getLocalEndpointId();
		if (Objects.isNull(hazelcastConfig.memberNamesPool) || hazelcastConfig.memberNamesPool.size() < 1) {
			return memberId.toString();
		}
		int index = Math.abs(((int)  memberId.getMostSignificantBits()) % hazelcastConfig.memberNamesPool.size());
		var result = hazelcastConfig.memberNamesPool.get(index);
		logger.info("Member Id {} is bound to human readable member name {} ", memberId, result);
		return result;
	}

	@Override
	public String toString() {
		var config = this.instance.getConfig().toString();
		return config;
//		return config.replace(", ", ", \n");
//		return JsonUtils.beautifyJsonString(this.instance.getConfig().toString().substring(6));
//		return this.instance.getConfig().toString();
	}

	public String getMemberName() { return this.memberName; }

	public UUID getLocalEndpointId() {
		return this.instance.getLocalEndpoint().getUuid();
	}

	private Config makeConfig(ObserverConfig.HazelcastConfig observerHazelcastConfig) {
		String configPath = observerHazelcastConfig.configFile;
		// From ClassLoader, all paths are "absolute" already - there's no context
		// from which they could be relative. Therefore you don't need a leading slash.

		Config result;
//		if (Objects.isNull(observerHazelcastConfig.configFile) && Objects.isNull(observerHazelcastConfig.config)) {
//			logger.warn("No configuration for hazelcast is given, the default is used");
//			result = new XmlConfigBuilder().build();
//		}

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

	private void addLogPrinters(String level) {
		if (Objects.isNull(level)) {
			return;
		}
		try {
			logger.info("Printing logs for level {}", level);
			Level logLevel = Level.parse(level);
			this.instance.getLoggingService().addLogListener(logLevel, new LogListener() {
				@Override
				public void log(LogEvent logEvent) {
					var member = logEvent.getMember();
					var record =  logEvent.getLogRecord();
					logger.info("{}: Member: {}, Logger: {} Message: {}", record.getLevel(), member.getUuid(), record.getLoggerName(), record.getMessage());
				}
			});
		} catch (Exception ex) {
			logger.warn("Cannot add logging level {}", level, ex);
		}

	}
}
