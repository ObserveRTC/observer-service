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

import io.micronaut.context.annotation.ConfigurationProperties;
import io.micronaut.context.annotation.EachProperty;
import org.observertc.webrtc.observer.monitors.ReportMonitorConfig;

import javax.validation.constraints.Min;
import java.util.*;

@ConfigurationProperties("observer")
public class ObserverConfig {

	public List<Map<String, Object>> connectors = new LinkedList<>();

	public HazelcastConfig hazelcast;

	public OutboundReportsConfig outboundReports;

	public MonitorsConfig monitors;

	public EvaluatorsConfig evaluators;

	@ConfigurationProperties("evaluators")
	public static class EvaluatorsConfig {

		@Min(0)
		public int observedPCSBufferMaxTimeInS = 30;

		@Min(1)
		public int observedPCSBufferMaxItemNums = 10000;

		@Min(15)
		public int peerConnectionMaxIdleTimeInS = 60;

		public Map<String, Object> reportMonitor;

	}

	@ConfigurationProperties("hazelcast")
	public static class HazelcastConfig {
		public String configFile = null;
	}

	@ConfigurationProperties("monitors")
	public static class MonitorsConfig {

		public CallsMonitorConfig callsMonitor;

		@ConfigurationProperties("callsMonitor")
		public static class CallsMonitorConfig {
			public boolean enabled = false;
			public long reportPeriodInS = 300;
		}
	}

	@ConfigurationProperties("outboundReports")
	public static class OutboundReportsConfig {
		public boolean enabled = true;
		public String defaultServiceName = "defaultServiceName";
		public String defaultTopicName = "reports";
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
	}

	public List<ServiceMappingConfiguration> serviceMappings = new ArrayList<>();

	@EachProperty("serviceMappings")
	public static class ServiceMappingConfiguration {
		public String name;
		public List<UUID> uuids = new ArrayList<>();
	}
}

