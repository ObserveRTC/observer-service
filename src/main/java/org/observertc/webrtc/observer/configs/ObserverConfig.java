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
import java.util.Map;

@ConfigurationProperties("observer")
public class ObserverConfig {

	// Security Configurations
	public SecurityConfig security;

	@ConfigurationProperties("security")
	public static class SecurityConfig {

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
		public ObfuscationsAnonymizationConfig anonymization;

		public ObfuscationType obfuscateIceAddresses = ObfuscationType.ANONYMIZATION;
		public ObfuscationType obfuscateUserId = ObfuscationType.ANONYMIZATION;
		public ObfuscationType obfuscateRoomId = ObfuscationType.ANONYMIZATION;

		@ConfigurationProperties("anonymization")
		public static class ObfuscationsAnonymizationConfig {
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
			public boolean enabled = true;
			public String defaultServiceId = "defaultServiceId";
		}

		public SourcesConfig.ClientSamplesConfig clientSamples;

		@ConfigurationProperties("clientSamples")
		public static class ClientSamplesConfig {
			public boolean enabled = true;
		}
	}

	// Evaluators Config
	public EvaluatorsConfig evaluators;

	@ConfigurationProperties("evaluators")
	public static class EvaluatorsConfig {

		@Min(30)
		public int clientSamplesBufferMaxTimeInS = 30;

		@Min(1)
		public int clientSamplesBufferMaxItems = 10000;

		@Min(60)
		public int mediaTracksMaxIdleTime = 300;

		@Min(60)
		public int peerConnectionsMaxIdleTime = 300;

		@Min(60)
		public int clientMaxIdleTime = 300;

		@Min(1)
		public int reportsBufferMaxItems = 10000;

		@Min(1)
		public int reportsBufferMaxRetainInS = 30;
	}

	public Map<String, Object> sinks;

	// Outbound Reports Config
	public OutboundReportsConfig outboundReports;

	@ConfigurationProperties("outboundReports")
	public static class OutboundReportsConfig {
		public boolean reportObserverEvents = true;
		public boolean reportCallEvents = true;
		public boolean reportCallMeta = true;
		public boolean reportClientExtensions = true;
		public boolean reportInboundAudioTracks = false;
		public boolean reportInboundVideoTracks = false;
		public boolean reportOutboundAudioTracks = false;
		public boolean reportOutboundVideoTracks = false;
		public boolean reportPeerConnectionTransport = true;
		public boolean reportPeerConnectionDataChannel = true;
		public boolean reportMediaTracks = true;
		public String defaultServiceName = "defaultServiceName";
	}

	// Hazelcast Config
	public HazelcastConfig hazelcast;

	@ConfigurationProperties("hazelcast")
	public static class HazelcastConfig {
		public String configFile = null;
	}

}

