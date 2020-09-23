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

package org.observertc.webrtc.reporter;

import io.micronaut.context.annotation.ConfigurationProperties;

@ConfigurationProperties("reporter")
public class ReporterConfig {

	public String observeRTCReportsTopic;

	public String profile = "bigQuery";

	public BigQueryReporterConfig bigQuery = null;

	@ConfigurationProperties("bigQuery")
	public static class BigQueryReporterConfig {

		public String projectName = null;

		public String datasetName = null;

		public String initiatedCallsTable = "InitiatedCalls";

		public String finishedCallsTable = "FinishedCalls";

		public String joinedPeerConnectionsTable = "JoinedPeerConnections";

		public String detachedPeerConnectionsTable = "DetachedPeerConnections";

		public String remoteInboundRTPSamplesTable = "RemoteInboundRTPSamples";

		public String outboundRTPSamplesTable = "OutboundRTPSamples";

		public boolean createDatasetIfNotExists = true;

		public boolean createTableIfNotExists = true;

		public String inboundRTPSamplesTable = "InboundRTPSamples";

		public String iceCandidatePairsTable = "ICECandidatePairs";

		public String iceLocalCandidatesTable = "ICELocalCandidates";

		public String iceRemoteCandidatesTable = "ICERemoteCandidates";

		public String mediaSourcesTable = "MediaSources";

		public String trackReportsTable = "TrackReports";
	}


}
