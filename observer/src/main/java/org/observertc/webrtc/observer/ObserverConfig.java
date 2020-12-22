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
import org.jooq.SQLDialect;
import org.observertc.webrtc.common.ObjectToString;

@ConfigurationProperties("observer")
public class ObserverConfig {

	public HazelcastConfig hazelcast;

	public DatabaseConfig database;

	public KafkaTopicsConfiguration kafkaTopics;

	public OutboundReportsConfig outboundReportsConfig;

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
		public int mediaStreamsBufferDebounceTimeInMs = 1000;
	}

	@ConfigurationProperties("outboundsReports")
	public static class OutboundReportsConfig {
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
	}

	@ConfigurationProperties("kafkaTopics")
	public static class KafkaTopicsConfiguration {
		public boolean runAdminClient = false;
		public boolean createIfNotExists = false;

		public ReportsConfig reports;

		public static class TopicConfig {
			public String topicName;
			public int onCreatePartitionNums;
			public int onCreateReplicateFactor;
			public long retentionTimeInMs = 604800_000; // 1 week
		}

		@ConfigurationProperties("reports")
		public static class ReportsConfig extends TopicConfig {

		}

		@Override
		public String toString() {
			return ObjectToString.toString(this);
		}
	}

	@ConfigurationProperties("database")
	public class DatabaseConfig {

		public String poolName = "webrtc-observer";
		public int maxPoolSize = 100;
		public int minIdle = 10;
		public String username = "root";
		public String password = "password";
		public String jdbcURL = "jdbc:mysql://localhost:3306/WebRTCObserver";
		public String jdbcDriver = "com.mysql.cj.jdbc.Driver";
		public String dialect = SQLDialect.MYSQL.getName();

		@Override
		public String toString() {
			return ObjectToString.toString(this);
		}
	}
}

