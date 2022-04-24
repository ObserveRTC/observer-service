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

package org.observertc.observer.configs;

import io.micronaut.context.annotation.ConfigurationProperties;

import javax.validation.constraints.Min;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@ConfigurationProperties("observer")
public class ObserverConfig {

	// Security Configurations
	public SecurityConfig security;

	@ConfigurationProperties("security")
	public static class SecurityConfig {

		public boolean printConfigs = true;
		public boolean allowExposeConfig = false;

		public ObfuscationsConfig obfuscations;

		@ConfigurationProperties("obfuscations")
		public static class ObfuscationsConfig {
			public String hashAlgorithm;
			public String hashSalt;
		}
	}

	// Sources Config
	public SourcesConfig sources;

	public static class SourceConfig {
		public boolean enabled = false;
    }

	@ConfigurationProperties("sources")
	public static class SourcesConfig {

		public List<String> allowedServiceIds = null;

		public RestConfig rest = new RestConfig();

		@ConfigurationProperties("rest")
		public static class RestConfig extends SourceConfig {

		}

		public WebsocketsConfig websocket = new WebsocketsConfig();

		@ConfigurationProperties("websocket")
		public static class WebsocketsConfig extends SourceConfig {

		}
	}

	// Repository config
	public RepositoryConfig repository;

	@ConfigurationProperties("repository")
	public static class RepositoryConfig {

		@Min(3)
		public int mediaTracksMaxIdleTimeInS = 300;

		@Min(3)
		public int peerConnectionsMaxIdleTime = 300;

		@Min(3)
		public int clientMaxIdleTimeInS = 300;

		@Min(3)
		public int sfuMaxIdleTimeInS = 600;

		@Min(3)
		public int sfuTransportMaxIdleTimeInS = 600;

		@Min(3)
		public int sfuRtpPadMaxIdleTimeInS = 600;

	}

	// Evaluators Config
	public EvaluatorsConfig evaluators;

	@ConfigurationProperties("evaluators")
	public static class EvaluatorsConfig {

		public Obfuscator obfuscator = new Obfuscator();

		@ConfigurationProperties(("obfuscator"))
		public static class Obfuscator {
			public boolean enabled = false;
			public ObfuscationType iceAddresses = ObfuscationType.ANONYMIZATION;
			public ObfuscationType userId = ObfuscationType.ANONYMIZATION;
			public ObfuscationType roomId = ObfuscationType.ANONYMIZATION;
		}

		public CallUpdater callUpdater = new CallUpdater();

		@ConfigurationProperties(("callUpdater"))
		public static class CallUpdater {
			public enum CallIdAssignMode {
				MASTER,
				SLAVE
			}
			public CallIdAssignMode callIdAssignMode = CallIdAssignMode.MASTER;
		}

	}

	// internal collectors config
	public InternalBuffersConfig buffers;

	@ConfigurationProperties(("buffers"))
	public static class InternalBuffersConfig {

		public DebouncersCollectorConfig debouncers = new DebouncersCollectorConfig();

		@ConfigurationProperties("debouncers")
		public static class DebouncersCollectorConfig extends CollectorConfig {

		}

		public SamplesBufferCollectorConfig samplesCollector = new SamplesBufferCollectorConfig();

		@ConfigurationProperties("samplesCollector")
		public static class SamplesBufferCollectorConfig extends CollectorConfig {

		}

		public ReportsCollectorConfig reportsCollector = new ReportsCollectorConfig();

		@ConfigurationProperties("reportsCollector")
		public static class ReportsCollectorConfig extends CollectorConfig {

		}
	}


	public static class CollectorConfig {

		@Min(1)
		public int maxItems = 2000;

		@Min(0)
		public int maxTimeInMs = 0;

	}

	public Map<String, Object> sinks;

	// Hazelcast Config
	public HazelcastConfig hazelcast;

	@ConfigurationProperties("hazelcast")
	public static class HazelcastConfig {
		public String configFile = null;
//		public Map<String, Object> config;
		public List<String> memberNamesPool = new ArrayList<>();
		public List<String> logs = new LinkedList<>();
	}

}

