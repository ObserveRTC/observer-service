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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micronaut.context.annotation.ConfigurationProperties;
import io.micronaut.context.annotation.EachProperty;
import io.micronaut.context.annotation.Parameter;
import org.observertc.webrtc.observer.common.ObjectToString;

import java.util.*;

@ConfigurationProperties("observer")
public class ObserverConfig {

	public List<Map<String, Object>> connectors = new LinkedList<>();

	public HazelcastConfig hazelcast;

	public OutboundReportsConfig outboundReports;

	public PCObserverConfig pcObserver;

	@ConfigurationProperties("hazelcast")
	public static class HazelcastConfig {
		public String configFile = null;
	}

	@ConfigurationProperties("pcObserver")
	public static class PCObserverConfig {
		public int peerConnectionMaxIdleTimeInS = 60;
		public int mediaStreamUpdatesFlushInS = 15;
		public int mediaStreamsBufferNums = 0; // means it will be determined automatically
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
		public String forwardTopicName = null;

		public ServiceMappingConfiguration(@Parameter String name) {
			this.name = name;
		}

		@Override
		public String toString() {
			try {
				return new ObjectMapper().writeValueAsString(this);
			} catch (JsonProcessingException e) {
				e.printStackTrace();
				return super.toString();
			}
		}
	}
}

