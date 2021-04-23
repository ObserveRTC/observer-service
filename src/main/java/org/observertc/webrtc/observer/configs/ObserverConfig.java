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
import io.micronaut.context.annotation.EachProperty;
import org.observertc.webrtc.observer.configbuilders.ConfigAssent;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.*;

@ConfigurationProperties("observer")
public class ObserverConfig {

	// Connectors Config
	@ConfigAssent(keyField = "name")
	public List<Map<String, Object>> connectors = new ArrayList<>();

	@Min(1)
	@Max(60)
	public int sentinelsCheckingPeriodInMin = 1;

	// Sentinels Config
	@Valid
	@ConfigAssent(keyField = "name")
	public List<SentinelConfig> sentinels = new ArrayList<>();

	// CallFilters Config
	@Valid
	@ConfigAssent(keyField = "name")
	public List<CallFilterConfig> callFilters = new ArrayList<>();

	// PC Filters Config
	@Valid
	@ConfigAssent(keyField = "name")
	public List<PeerConnectionFilterConfig> pcFilters = new ArrayList<>();

	// Security Configurations
	public SecurityConfig security;

	// IP Address Converter Config
	public IPAddressConverterConfig ipAddressConverter;

	@ConfigurationProperties("ipAddressConverter")
	public static class IPAddressConverterConfig {
		public boolean enabled = false;
		public String algorithm = "SHA-256";
		public String salt = "mySalt";
	}

	@ConfigurationProperties("security")
	public static class SecurityConfig {

		@Deprecated(since = "0.7.2")
		public boolean dropUnknownServices = false;
	}

	// Evaluators Config
	public EvaluatorsConfig evaluators;

	@ConfigAssent(mutable = false)
	@ConfigurationProperties("evaluators")
	public static class EvaluatorsConfig {

		@Min(0)
		public int observedPCSBufferMaxTimeInS = 30;

		@Min(1)
		public int observedPCSBufferMaxItemNums = 10000;

		@Min(15)
		public int peerConnectionMaxIdleTimeInS = 300;

		public String impairablePCsCallName = "impairable-peer-connections-default-call-name";
	}

	// Hazelcast Config
	public HazelcastConfig hazelcast;

	@ConfigAssent(mutable = false)
	@ConfigurationProperties("hazelcast")
	public static class HazelcastConfig {
		public String configFile = null;
	}


	// Outbound Reports Config
	public OutboundReportsConfig outboundReports;

	@ConfigAssent(mutable = false)
	@ConfigurationProperties("outboundReports")
	public static class OutboundReportsConfig {
		public boolean enabled = true;
		public String defaultServiceName = "defaultServiceName";
		public boolean reportOutboundRTPs = true;
		public boolean reportInboundRTPs = true;
		public boolean reportRemoteInboundRTPs = true;
		public boolean reportTracks = true;
		public boolean reportMediaSources = true;
		public boolean reportCandidatePairs = true;
		public boolean reportLocalCandidates = true;
		public boolean reportRemoteCandidates = true;
		public boolean reportUserMediaErrors = true;

		public boolean reportInitiatedCalls = true;
		public boolean reportFinishedCalls = true;
		public boolean reportJoinedPeerConnections = true;
		public boolean reportDetachedPeerConnections = true;
		public boolean reportObserverEvents = true;
		public boolean reportExtensions = true;
		public boolean reportMediaDevices = true;
		public boolean reportClientDetails = true;
	}

	// Service Mappings Config
	@Valid
	@ConfigAssent(keyField = "name")
	public List<ServiceMapConfiguration> servicemappings = new ArrayList<>();

	@EachProperty("servicemappings")
	public static class ServiceMapConfiguration {

		@NotNull
		public String name;
		public List<UUID> uuids = new ArrayList<>();
	}

	// Inbound RTP Monitoring Config
	public InboundRtpMonitorConfig inboundRtpMonitor;

	@ConfigurationProperties("inboundRtpMonitor")
	public static class InboundRtpMonitorConfig extends RtpMonitorConfig{

	}

	// Outbound RTP Monitoring Config
	public OutboundRtpMonitorConfig outboundRtpMonitor;

	@ConfigurationProperties("outboundRtpMonitor")
	public static class OutboundRtpMonitorConfig extends RtpMonitorConfig{

	}

	// Remote Inbound RTP Monitoring Config
	public RemoteInboundRtpMonitorConfig remoteInboundRtpMonitor;

	@ConfigurationProperties("remoteInboundRtpMonitor")
	public static class RemoteInboundRtpMonitorConfig extends RtpMonitorConfig {
		public double weightFactor = 0.5;
	}

	// Report Monitor Config
	public ReportMonitorConfig reportMonitor;

	@ConfigurationProperties("reportMonitor")
	public static class ReportMonitorConfig extends ReportCounterMonitorConfig {

	}

	// User Media Errors Monitoring Config
	public UserMediaErrorsMonitorConfig userMediaErrorsMonitor;

	@ConfigurationProperties("userMediaErrorsMonitor")
	public static class UserMediaErrorsMonitorConfig extends ReportCounterMonitorConfig {

	}

	// Sources Config
	public SourcesConfig sources;

	@ConfigurationProperties("sources")
	public static class SourcesConfig {

		public PCSamplesConfig pcSamples;

		@ConfigurationProperties("pcSamples")
		public static class PCSamplesConfig {
			public boolean enabled = false;
			public String defaultServiceName = "defaultServiceName";
			public boolean dropUnknownServiceName = false;
		}
	}

	public static class RtpMonitorConfig {
		public boolean enabled = false;
		public int retentionTimeInS = 300;
	}

	@ConfigAssent(mutable = false)
	public static class ReportCounterMonitorConfig {
		public boolean enabled = false;
		public boolean tagByType = false;
		public boolean tagByServiceName = false;
		public boolean tagByServiceUUID = false;
	}

}

