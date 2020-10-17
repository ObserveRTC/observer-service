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

package org.observertc.webrtc.observer.evaluators;

import io.micronaut.context.annotation.Prototype;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.observertc.webrtc.common.UUIDAdapter;
import org.observertc.webrtc.observer.ReportSink;
import org.observertc.webrtc.observer.evaluators.reportdrafts.FinishedCallReportDraft;
import org.observertc.webrtc.observer.jooq.tables.records.PeerconnectionsRecord;
import org.observertc.webrtc.observer.repositories.ActiveStreamsRepository;
import org.observertc.webrtc.observer.repositories.PeerConnectionsRepository;
import org.observertc.webrtc.schemas.reports.DetachedPeerConnection;
import org.observertc.webrtc.schemas.reports.ReportType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Prototype
public class ExpiredPCsConsumer {

	private static final Logger logger = LoggerFactory.getLogger(ExpiredPCsConsumer.class);

	private final ActiveStreamsRepository activeStreamsRepository;
	private final PeerConnectionsRepository peerConnectionsRepository;
	private final ReportSink reportSink;
	private final ReportDraftsEvaluator reportDraftsEvaluator;

	public ExpiredPCsConsumer(
			ReportDraftsEvaluator reportDraftsEvaluator,
			ActiveStreamsRepository activeStreamsRepository,
			PeerConnectionsRepository peerConnectionsRepository,
			ReportSink reportSink) {
		this.reportSink = reportSink;
		this.activeStreamsRepository = activeStreamsRepository;
		this.peerConnectionsRepository = peerConnectionsRepository;
		this.reportDraftsEvaluator = reportDraftsEvaluator;
	}

	public void processExpiredPCs(List<byte[]> peerConnectionUUIDs) {
		if (peerConnectionUUIDs.size() < 1) {
			return;
		}
		Iterator<PeerconnectionsRecord> it = this.peerConnectionsRepository.findAll(peerConnectionUUIDs).iterator();
		for (; it.hasNext(); ) {
			PeerconnectionsRecord record = it.next();
			record.setDetached(record.getUpdated());
			record.setUpdated(record.getUpdated());
			record.store();

			UUID serviceUUID = UUIDAdapter.toUUID(record.getServiceuuid());
			UUID callUUID = UUIDAdapter.toUUID(record.getCalluuid());
			UUID peerConnectionUUID = UUIDAdapter.toUUID(record.getPeerconnectionuuid());

			Object payload = DetachedPeerConnection.newBuilder()
					.setMediaUnitId(record.getMediaunitid())
					.setCallName(record.getProvidedcallid())
					.setCallUUID(callUUID.toString())
					.setUserId(record.getProvideduserid())
					.setBrowserId(record.getBrowserid())
					.setPeerConnectionUUID(peerConnectionUUID.toString())
					.build();

			this.reportSink.sendReport(serviceUUID,
					serviceUUID,
					record.getProvidedcallid(),
					serviceUUID.toString(),
					ReportType.DETACHED_PEER_CONNECTION,
					record.getUpdated(),
					payload);

			Optional<PeerconnectionsRecord> joinedPCHolder =
					this.peerConnectionsRepository.findJoinedPCsByCallUUIDBytes(record.getCalluuid()).findFirst();

			if (joinedPCHolder.isPresent()) {
				continue;
			}

			//finished call
			FinishedCallReportDraft reportDraft = FinishedCallReportDraft.of(serviceUUID, callUUID, record.getUpdated());
			this.reportDraftsEvaluator.add(reportDraft);
			this.activeStreamsRepository.deleteByCallUUIDBytes(record.getCalluuid());
		}
	}
}
