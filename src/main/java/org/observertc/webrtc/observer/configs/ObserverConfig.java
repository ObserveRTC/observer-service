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

package org.observertc.webrtc.observer.configs;

import io.micronaut.context.annotation.ConfigurationProperties;

import javax.validation.constraints.Min;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@ConfigurationProperties("observer")
public class ObserverConfig {

	// Security Configurations
	public SecurityConfig security;

	@ConfigurationProperties("security")
	public static class SecurityConfig {

		public boolean allowExposeConfig = false;

		public SecurityConfig.WebsocketSecurityConfig websockets = new SecurityConfig.WebsocketSecurityConfig();


        @ConfigurationProperties("websockets")
		public static class WebsocketSecurityConfig {
			public int expirationInMin = 0; // 0 means the access token provided is used
		}
	}

	public ObfuscationsConfig obfuscations;

	@ConfigurationProperties("obfuscations")
	public static class ObfuscationsConfig {
		public enum ObfuscationType {
			ANONYMIZATION,
			NONE,
		}
		public boolean enabled = false;
		public ObfuscationsMaskConfig maskConfig;

		public ObfuscationType maskedIceAddresses = ObfuscationType.ANONYMIZATION;
		public ObfuscationType maskedUserId = ObfuscationType.ANONYMIZATION;
		public ObfuscationType maskedRoomId = ObfuscationType.ANONYMIZATION;

		@ConfigurationProperties("anonymization")
		public static class ObfuscationsMaskConfig {
			public String hashAlgorithm;
			public String salt;
		}
	}

	// Sources Config
	public SourcesConfig sources;

	@ConfigurationProperties("sources")
	public static class SourcesConfig {

		@Deprecated(since = "1.0.0")
		public SourcesConfig.PCSamplesConfig pcSamples;

		@ConfigurationProperties("pcSamples")
		public static class PCSamplesConfig {
			public boolean enabled = false;
			public String defaultServiceId = "defaultServiceId";
		}

		public SourcesConfig.ClientSamplesConfig clientSamples;

		@ConfigurationProperties("clientSamples")
		public static class ClientSamplesConfig {
			public boolean enabled = true;
		}

		public SourcesConfig.SfuSamplesConfig sfuSamples;

		@ConfigurationProperties("sfuSamples")
		public static class SfuSamplesConfig {
			public boolean enabled = true;
		}
	}

	// Repository config
	public RepositoryConfig repositories;

	@ConfigurationProperties("repositories")
	public static class RepositoryConfig {

		@Min(60)
		public int mediaTracksMaxIdleTime = 300;

		@Min(60)
		public int peerConnectionsMaxIdleTime = 300;

		@Min(60)
		public int clientMaxIdleTime = 300;

		@Min(60)
		public int sfuMaxIdleTime = 600;

		@Min(60)
		public int sfuTransportMaxIdleTime = 600;

		@Min(60)
		public int sfuRtpPadMaxIdleTime = 600;

		@Min(0)
		public int eventsCollectingTimeInS = 5;
	}

	// Evaluators Config
	public EvaluatorsConfig evaluators;

	@ConfigurationProperties("evaluators")
	public static class EvaluatorsConfig {

		@Min(5)
		public int clientSamplesBufferMaxTimeInS = 5;

		@Min(1)
		public int clientSamplesBufferMaxItems = 10000;

		@Min(5)
		public int sfuSamplesBufferMaxTimeInS = 5;

		@Min(1)
		public int sfuSamplesBufferMaxItems = 10000;

		@Min(1)
		public int reportsBufferMaxItems = 10000;

		@Min(1)
		public int reportsBufferMaxRetainInS = 30;

		@Min(1)
		public int sfuReportsPreCollectingTimeInS = 3;

		@Min(1)
		public int sfuReportsPreCollectingMaxItems = 1000;

		@Min(1)
		public int clientReportsPreCollectingTimeInS = 3;

		@Min(1)
		public int clientReportsPreCollectingMaxItems = 1000;
	}

	// internal collectors config
	public InternalCollectorConfigs internalCollectors;

	@ConfigurationProperties(("internalCollectors"))
	public static class InternalCollectorConfigs {
		public CollectorConfig clientSamples = new CollectorConfig();
		public CollectorConfig clientProcessDebouncers = new CollectorConfig();

		public CollectorConfig sfuSamples = new CollectorConfig();
		public CollectorConfig sfuProcessDebouncers = new CollectorConfig();

		public CollectorConfig outboundReports = new CollectorConfig();
	}

	public static class CollectorConfig {

		@Min(1)
		public int maxItems = 1000;

		@Min(0)
		public int maxTimeInS = 0;

	}

	public Map<String, Object> sinks;

	// Outbound Reports Config
	public OutboundReportsConfig outboundReports;

	@ConfigurationProperties("outboundReports")
	public static class OutboundReportsConfig {
		public ReportFormat reportFormat = ReportFormat.JSON;
		public boolean reportObserverEvents = true;
		public boolean reportCallEvents = true;
		public boolean reportCallMeta = true;
		public boolean reportClientExtensions = true;
		public boolean reportInboundAudioTracks = true;
		public boolean reportInboundVideoTracks = true;
		public boolean reportOutboundAudioTracks = true;
		public boolean reportOutboundVideoTracks = true;
		public boolean reportClientTransports = true;
		public boolean reportClientDataChannels = true;
		public boolean reportMediaTracks = false;

		public boolean reportSfuEvents = true;
		public boolean reportSfuMeta = true;
		public boolean reportSfuTransports = true;
		public boolean reportSfuSctpStreams = true;

		public boolean reportSfuInboundRtpStreams = true;
		public boolean reportSfuOutboundRtpStreams = true;

		public boolean reportSfuRtpPadOnlyWithCallId = true;

		public String defaultServiceName = "defaultServiceName";

    }

	// Hazelcast Config
	public HazelcastConfig hazelcast;

	@ConfigurationProperties("hazelcast")
	public static class HazelcastConfig {
		public String configFile = null;
		public List<String> memberNamesPool = new ArrayList<>();
	}

}

