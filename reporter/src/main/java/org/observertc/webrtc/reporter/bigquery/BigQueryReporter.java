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

package org.observertc.webrtc.reporter.bigquery;

import io.micronaut.context.annotation.Prototype;
import org.observertc.webrtc.common.reports.DetachedPeerConnectionReport;
import org.observertc.webrtc.common.reports.JoinedPeerConnectionReport;
import org.observertc.webrtc.common.reports.avro.DetachedPeerConnection;
import org.observertc.webrtc.common.reports.avro.FinishedCall;
import org.observertc.webrtc.common.reports.avro.ICECandidatePair;
import org.observertc.webrtc.common.reports.avro.ICELocalCandidate;
import org.observertc.webrtc.common.reports.avro.ICERemoteCandidate;
import org.observertc.webrtc.common.reports.avro.InboundRTP;
import org.observertc.webrtc.common.reports.avro.InitiatedCall;
import org.observertc.webrtc.common.reports.avro.JoinedPeerConnection;
import org.observertc.webrtc.common.reports.avro.MediaSource;
import org.observertc.webrtc.common.reports.avro.OutboundRTP;
import org.observertc.webrtc.common.reports.avro.RemoteInboundRTP;
import org.observertc.webrtc.common.reports.avro.Report;
import org.observertc.webrtc.common.reports.avro.Track;
import org.observertc.webrtc.reporter.Reporter;
import org.observertc.webrtc.reporter.ReporterConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Prototype
public class BigQueryReporter implements Reporter {

	private static final Logger logger = LoggerFactory.getLogger(BigQueryReporter.class);

	private final BigQueryService bigQueryService;
	private final BigQueryTable<InitiatedCallEntry> initiatedCalls;
	private final BigQueryTable<FinishedCallEntry> finishedCalls;
	private final BigQueryTable<JoinedPeerConnectionEntry> joinedPeerConnections;
	private final BigQueryTable<DetachedPeerConnectionEntry> detachedPeerConnections;
	private final BigQueryTable<RemoteInboundRTPReportEntry> remoteInboundRTPSamples;
	private final BigQueryTable<InboundRTPReportEntry> inboundRTPSamples;
	private final BigQueryTable<OutboundRTPReportEntry> outboundRTPSamples;
	private final BigQueryTable<ICECandidatePairEntry> iceCandidatePairs;
	private final BigQueryTable<ICELocalCandidateEntry> iceLocalCandidates;
	private final BigQueryTable<ICERemoteCandidateEntry> iceRemoteCandidates;
	private final BigQueryTable<MediaSourceEntry> mediaSources;
	private final BigQueryTable<TrackReportEntry> trackReports;

	public BigQueryReporter(ReporterConfig.BigQueryReporterConfig config) {
		this.bigQueryService = new BigQueryService(config.projectName, config.datasetName);
		this.initiatedCalls = new BigQueryTable(bigQueryService, config.initiatedCallsTable);
		this.finishedCalls = new BigQueryTable(bigQueryService, config.finishedCallsTable);
		this.joinedPeerConnections = new BigQueryTable(bigQueryService, config.joinedPeerConnectionsTable);
		this.detachedPeerConnections = new BigQueryTable(bigQueryService, config.detachedPeerConnectionsTable);
		this.remoteInboundRTPSamples = new BigQueryTable<>(bigQueryService, config.remoteInboundRTPSamplesTable);
		this.inboundRTPSamples = new BigQueryTable<>(bigQueryService, config.inboundRTPSamplesTable);
		this.outboundRTPSamples = new BigQueryTable<>(bigQueryService, config.outboundRTPSamplesTable);
		this.iceCandidatePairs = new BigQueryTable<>(bigQueryService, config.iceCandidatePairsTable);
		this.iceLocalCandidates = new BigQueryTable<>(bigQueryService, config.iceLocalCandidatesTable);
		this.iceRemoteCandidates = new BigQueryTable<>(bigQueryService, config.iceRemoteCandidatesTable);
		this.trackReports = new BigQueryTable<>(bigQueryService, config.trackReportsTable);
		this.mediaSources = new BigQueryTable<>(bigQueryService, config.mediaSourcesTable);
	}

	@Override
	public void flush() {
		this.joinedPeerConnections.flush();
		this.detachedPeerConnections.flush();
		this.initiatedCalls.flush();
		this.finishedCalls.flush();
		this.iceRemoteCandidates.flush();
		this.iceLocalCandidates.flush();
		this.iceCandidatePairs.flush();
		this.inboundRTPSamples.flush();
		this.outboundRTPSamples.flush();
		this.remoteInboundRTPSamples.flush();
		this.trackReports.flush();
		this.mediaSources.flush();
	}

	@Override
	public void processJoinedPeerConnectionReport(Report report, JoinedPeerConnection joinedPeerConnection) {
		
		JoinedPeerConnectionEntry joinedPeerConnection = JoinedPeerConnectionEntry.from((JoinedPeerConnection) report);
//				joinedPeerConnections.insert(joinedPeerConnection);
		joinedPeerConnections.add(joinedPeerConnection);
	}

	@Override
	public Void processDetachedPeerConnectionReport(DetachedPeerConnection report) {
		DetachedPeerConnectionEntry detachedPeerConnectionEntry = DetachedPeerConnectionEntry.from((DetachedPeerConnection) report);
//				detachedPeerConnections.insert(detachedPeerConnectionEntry);
		detachedPeerConnections.add(detachedPeerConnectionEntry);
		return null;
	}

	@Override
	public Void processInitiatedCallReport(InitiatedCall report) {
		InitiatedCallEntry initiatedCallEntry = InitiatedCallEntry.from((InitiatedCall) report);
//				initiatedCalls.insert(initiatedCallEntry);
		initiatedCalls.add(initiatedCallEntry);
		return null;
	}

	@Override
	public Void processFinishedCallReport(FinishedCall report) {
		FinishedCallEntry finishedCallEntry = FinishedCallEntry.from((FinishedCall) report);
//				finishedCalls.insert(finishedCallEntry);
		finishedCalls.add(finishedCallEntry);
		return null;
	}

	@Override
	public Void processRemoteInboundRTPReport(RemoteInboundRTP report) {
		RemoteInboundRTPReportEntry remoteInboundRTPReportEntry = RemoteInboundRTPReportEntry.from(report);
//				remoteInboundRTPSamples.insert(remoteInboundRTPReportEntry);
		remoteInboundRTPSamples.add(remoteInboundRTPReportEntry);
		return null;
	}

	@Override
	public Void processInboundRTPReport(InboundRTP report) {
		InboundRTPReportEntry inboundRTPReportEntry = InboundRTPReportEntry.from(report);
//				inboundRTPSamples.insert(inboundRTPReportEntry);
		inboundRTPSamples.add(inboundRTPReportEntry);
		return null;
	}

	@Override
	public Void processOutboundRTPReport(OutboundRTP report) {
		OutboundRTPReportEntry outboundRTPReportEntry = OutboundRTPReportEntry.from(report);
//				outboundRTPSamples.insert(outboundRTPReportEntry);
		outboundRTPSamples.add(outboundRTPReportEntry);
		return null;
	}

	@Override
	public Void processICECandidatePairReport(ICECandidatePair report) {
		ICECandidatePairEntry iceCandidatePairEntry = ICECandidatePairEntry.from(report);
//				iceCandidatePairs.insert(iceCandidatePairEntry);
		iceCandidatePairs.add(iceCandidatePairEntry);
		return null;
	}

	@Override
	public Void processICELocalCandidateReport(ICELocalCandidate report) {
		ICELocalCandidateEntry iceCandidatePairEntry = ICELocalCandidateEntry.from(report);
//				iceLocalCandidates.insert(iceCandidatePairEntry);
		iceLocalCandidates.add(iceCandidatePairEntry);
		return null;
	}

	@Override
	public Void processICERemoteCandidateReport(ICERemoteCandidate report) {
		ICERemoteCandidateEntry iceRemoteCandidateEntry = ICERemoteCandidateEntry.from(report);
//				iceRemoteCandidates.insert(iceRemoteCandidateEntry);
		iceRemoteCandidates.add(iceRemoteCandidateEntry);
		return null;
	}

	@Override
	public Void processTrackReport(Track report) {
		TrackReportEntry reportEntry = TrackReportEntry.from(report);
//				trackReports.insert(reportEntry);
		trackReports.add(reportEntry);
		return null;
	}

	@Override
	public Void processMediaSourceReport(MediaSource report) {
		MediaSourceEntry reportEntry = MediaSourceEntry.from(report);
//				mediaSources.insert(reportEntry);
		mediaSources.add(reportEntry);
		return null;
	}

}
