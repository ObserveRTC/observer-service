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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@ConfigurationProperties("observer")
public class ObserverConfig {

	// Metrics Configurations
	public MetricsConfig metrics;

	@ConfigurationProperties("metrics")
	public static class MetricsConfig {

		public String prefix = "observertc";
		public String serviceIdTagName = "serviceId";
		public String mediaUnitTagName = "mediaUnit";

		public RepositoryMetricsConfig repositoryMetrics;

        @ConfigurationProperties("repositoryMetrics")
		public static class RepositoryMetricsConfig {
			public boolean enabled;

			@Min(1)
			public int exposePeriodInMin = 5;
		}

		public SourceMetricsConfig sourceMetrics;

		@ConfigurationProperties("sourceMetrics")
		public static class SourceMetricsConfig {
			public boolean enabled;
		}

		public SinkMetricsConfig sinkMetrics;

		@ConfigurationProperties("sinkMetrics")
		public static class SinkMetricsConfig {
			public boolean enabled;
		}

		public ReportMetricsConfig reportMetrics;

		@ConfigurationProperties("reportMetrics")
		public static class ReportMetricsConfig {
			public boolean enabled;
		}

		public FlawMetricsConfig flawMetrics;

		@ConfigurationProperties("flawMetrics")
		public static class FlawMetricsConfig {
			public boolean enabled;
		}
	}

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
		public boolean acceptSfuSamples = true;
		public boolean acceptClientSamples = true;

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

		public boolean useBackups = false;

		@Min(3)
		public int callMaxIdleTimeInS = 3;

//		@Min(3)
//		public int mediaTracksMaxIdleTimeInS;
//
//		@Min(3)
//		public int peerConnectionsMaxIdleTime;
//
//		@Min(3)
//		public int clientMaxIdleTimeInS;
//
//		@Min(3)
//		public int sfuMaxIdleTimeInS;
//
		@Min(3)
		public int sfuTransportMaxIdleTimeInS;
//
//		@Min(3)
//		public int sfuRtpPadMaxIdleTimeInS;
//
//		@Min(-1)
//		public long evictExpiredEntriesPeriodInMs;
//
//		@Min(-1)
//		public long evictExpiredEntriesThresholdOffsetInMs = 0;
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

		public ClientSamplesAnalyserConfig clientSamplesAnalyser = new ClientSamplesAnalyserConfig();

		@ConfigurationProperties(("clientSamplesAnalyser"))
		public static class ClientSamplesAnalyserConfig {
			public boolean dropUnmatchedReports = false;
		}

		public SfuSamplesAnalyserConfig sfuSamplesAnalyser = new SfuSamplesAnalyserConfig();

		@ConfigurationProperties(("sfuSamplesAnalyser"))
		public static class SfuSamplesAnalyserConfig {
			public boolean dropUnmatchedInternalInboundReports = false;
			public boolean dropUnmatchedInboundReports = false;
			public boolean dropUnmatchedOutboundReports = false;
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
		public int maxTimeInMs = 100;

	}

	public Map<String, Object> sinks;

	// Hazelcast Config
	public HamokConfig hamok;

	@ConfigurationProperties("hamok")
	public static class HamokConfig {
		public List<String> memberNamesPool = new LinkedList<>();
		public Map<String, Object> endpoint;

		public StorageGridConfig storageGrid = new StorageGridConfig();

		@ConfigurationProperties(("storageGrid"))
		public static class StorageGridConfig {
			public int raftMaxLogEntriesRetentionTimeInMinutes = 30;
			public int heartbeatInMs = 50;
			public int followerMaxIdleInMs = 300;
			public int peerMaxIdleInMs = 1000;
			public int sendingHelloTimeoutInMs = 1500;
			public int applicationCommitIndexSyncTimeoutInMs = 60 * 1000;
			public int requestTimeoutInMs = 3000;
		}
	}
}

