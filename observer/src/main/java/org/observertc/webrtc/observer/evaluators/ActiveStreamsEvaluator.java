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
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.observertc.webrtc.common.UUIDAdapter;
import org.observertc.webrtc.observer.ReportSink;
import org.observertc.webrtc.observer.evaluators.reportdrafts.InitiatedCallReportDraft;
import org.observertc.webrtc.observer.evaluators.reportdrafts.ReportDraft;
import org.observertc.webrtc.observer.jooq.tables.records.ActivestreamsRecord;
import org.observertc.webrtc.observer.jooq.tables.records.PeerconnectionsRecord;
import org.observertc.webrtc.observer.micrometer.CountedLogMonitor;
import org.observertc.webrtc.observer.micrometer.ObserverMetricsReporter;
import org.observertc.webrtc.observer.repositories.ActiveStreamKey;
import org.observertc.webrtc.observer.repositories.ActiveStreamsRepository;
import org.observertc.webrtc.observer.repositories.PeerConnectionsRepository;
import org.observertc.webrtc.schemas.reports.JoinedPeerConnection;
import org.observertc.webrtc.schemas.reports.ReportType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;

@Prototype
public class ActiveStreamsEvaluator {

	private static final Logger logger = LoggerFactory.getLogger(ActiveStreamsEvaluator.class);
	private final PeerConnectionsRepository peerConnectionsRepository;
	private final ActiveStreamsRepository activeStreamsRepository;
	private final ReportSink reportSink;
	private final ReportDraftsEvaluator reportDraftsEvaluator;
	private final ObserverMetricsReporter observerMetricsReporter;
	private final CountedLogMonitor countedLogMonitor;

	public ActiveStreamsEvaluator(
			ObserverMetricsReporter observerMetricsReporter,
			ReportDraftsEvaluator reportDraftsEvaluator,
			PeerConnectionsRepository peerConnectionsRepository,
			ActiveStreamsRepository activeStreamsRepository,
			ReportSink reportSink
	) {
		this.observerMetricsReporter = observerMetricsReporter;
		this.reportDraftsEvaluator = reportDraftsEvaluator;
		this.peerConnectionsRepository = peerConnectionsRepository;
		this.activeStreamsRepository = activeStreamsRepository;
		this.reportSink = reportSink;

		this.countedLogMonitor = observerMetricsReporter
				.makeCountedLogMonitor(logger)
				.withDefaultMetricName("activeStreamsEvaluator")
		;
	}

	public void update(Map<UUID, MediaStreamUpdate> updates) {
		List<PeerconnectionsRecord> updatedPCs = new LinkedList<>();
		Stream<PeerconnectionsRecord> existingPCs =
				this.peerConnectionsRepository.findAll(
						updates.values().stream()
								.map(u -> UUIDAdapter.toBytesOrDefault(u.peerConnectionUUID, null))
								.filter(Objects::nonNull).collect(Collectors.toList())
				);
		Iterator<PeerconnectionsRecord> it = existingPCs.iterator();
		for (; it.hasNext(); ) {
			PeerconnectionsRecord record = it.next();
			if (record.getDetached() != null) {
				// REJOINED
				record.setDetached(null);

			}
			UUID pcUUID = UUIDAdapter.toUUIDOrDefault(record.getPeerconnectionuuid(), null);
			MediaStreamUpdate mediaStreamUpdate = updates.get(pcUUID);
			if (mediaStreamUpdate == null) {
				this.countedLogMonitor
						.makeEntry()
						.withCategory("notExistingPC")
						.withLogLevel(Level.ERROR)
						.withMessage("The PC returned by the repository is not existing in the current update.")
						.log();
				continue;
			}
			// Set the update time for the peer connection
			record.setUpdated(mediaStreamUpdate.updated);
			updatedPCs.add(record);
			updates.remove(pcUUID);
		}
		this.peerConnectionsRepository.updateAll(updatedPCs);

		if (updates.size() < 1) {
			return;
		}

		Deque<MediaStreamUpdate> mediaStreamUpdates = new LinkedList<>();
		updates.values().stream().forEach(mediaStreamUpdates::addLast);
		this.processMediaStreamUpdates(mediaStreamUpdates);
	}

	private void processMediaStreamUpdates(Deque<MediaStreamUpdate> newPCs) {
		List<PeerconnectionsRecord> updatedPCs = new LinkedList<>();
		while (!newPCs.isEmpty()) {
			MediaStreamUpdate mediaStreamUpdate = newPCs.removeFirst();
			// Check if active streams available
			List<ActiveStreamKey> activeStreamKeys =
					mediaStreamUpdate.SSRCs.stream()
							.map(ssrc -> new ActiveStreamKey(mediaStreamUpdate.serviceUUID, ssrc))
							.collect(Collectors.toList());
			List<ActivestreamsRecord> activeStreamsRecords
					= this.activeStreamsRepository.streamByIds(activeStreamKeys.stream()).collect(Collectors.toList());
			byte[] callUUIDBytes = null;

			if (activeStreamsRecords != null && 0 < activeStreamsRecords.size()) {
				Optional<byte[]> callUUIDBytesHolder =
						activeStreamsRecords.stream().filter(record -> record.getCalluuid() != null).map(ActivestreamsRecord::getCalluuid).findFirst();
				if (!callUUIDBytesHolder.isPresent()) {
					this.countedLogMonitor
							.makeEntry()
							.withCategory("notExistingCallUUID")
							.withLogLevel(Level.ERROR)
							.withMessage("Active streams are detected without callUUID.")
							.log();
					continue;
				}
				callUUIDBytes = callUUIDBytesHolder.get();
			} else {

				Optional<PeerconnectionsRecord> pcHolder =
						this.peerConnectionsRepository.findByJoinedBrowserIDOrProvidedCallID(mediaStreamUpdate.created,
								mediaStreamUpdate.browserID, mediaStreamUpdate.callName);
				if (pcHolder.isPresent()) {
					callUUIDBytes = pcHolder.get().getCalluuid();
				}

			}

			if (callUUIDBytes != null) {
				this.joinPeerConnection(callUUIDBytes, mediaStreamUpdate);
				continue;
			}

			// INITIATED
			UUID callUUID = UUID.randomUUID();
			callUUIDBytes = UUIDAdapter.toBytes(callUUID);
			final byte[] finalCallUUIDBytes = callUUIDBytes;
			List<ActivestreamsRecord> newActiveStreams = activeStreamKeys.stream()
					.map(activeStreamKey -> new ActivestreamsRecord(
							activeStreamKey.getServiceUUIDBytes(),
							activeStreamKey.getSSRC(),
							finalCallUUIDBytes)
					)
					.collect(Collectors.toList());
			try {
				this.activeStreamsRepository.updateAll(newActiveStreams);
			} catch (Exception ex) {
				this.countedLogMonitor
						.makeEntry()
						.withCategory("repositoryException")
						.withLogLevel(Level.ERROR)
						.withException(ex)
						.withMessage("An exception caught during saving data.")
						.log();
				continue;
			}

			ReportDraft reportDraft = InitiatedCallReportDraft.of(
					mediaStreamUpdate.serviceUUID,
					callUUID,
					mediaStreamUpdate.customProvided,
					mediaStreamUpdate.created
			);

			this.reportDraftsEvaluator.add(reportDraft);
			newPCs.addLast(mediaStreamUpdate);
		}

	}

	private void joinPeerConnection(byte[] callUUIDBytes, MediaStreamUpdate mediaStreamUpdate) {
		if (this.peerConnectionsRepository.existsById(mediaStreamUpdate.peerConnectionUUID)) {
			this.countedLogMonitor
					.makeEntry()
					.withTag("peerConnectionUUID", mediaStreamUpdate.peerConnectionUUID)
					.withCategory("invalidOperation")
					.withLogLevel(Level.WARN)
					.withMessage("Attempted to join a PC {} twice!")
					.log();
			return;
		}
		try {
			this.peerConnectionsRepository.save(new PeerconnectionsRecord(
					UUIDAdapter.toBytes(mediaStreamUpdate.peerConnectionUUID),
					callUUIDBytes,
					UUIDAdapter.toBytes(mediaStreamUpdate.serviceUUID),
					mediaStreamUpdate.created,
					mediaStreamUpdate.updated,
					null,
					mediaStreamUpdate.mediaUnitID,
					mediaStreamUpdate.browserID,
					mediaStreamUpdate.userId,
					mediaStreamUpdate.callName,
					mediaStreamUpdate.timeZoneID,
					mediaStreamUpdate.serviceName
			));
		} catch (Exception ex) {
			this.countedLogMonitor
					.makeEntry()
					.withTag("peerConnectionUUID", mediaStreamUpdate.peerConnectionUUID)
					.withCategory("repositoryException")
					.withLogLevel(Level.ERROR)
					.withException(ex)
					.withMessage("Exception happened at saving new peer connection, report won't be sent")
					.log();
			return;
		}
		UUID callUUID = UUIDAdapter.toUUIDOrDefault(callUUIDBytes, null);
		String callUUIDStr;
		if (callUUID != null) {
			callUUIDStr = callUUID.toString();
		} else {
			callUUIDStr = "NOT VALID UUID";
		}
		JoinedPeerConnection joinedPC = JoinedPeerConnection.newBuilder()
				.setMediaUnitId(mediaStreamUpdate.mediaUnitID)
				.setCallUUID(callUUIDStr)
				.setPeerConnectionUUID(mediaStreamUpdate.peerConnectionUUID.toString())
				.build();

		this.reportSink.sendReport(
				mediaStreamUpdate.serviceUUID,
				mediaStreamUpdate.serviceUUID,
				mediaStreamUpdate.serviceName,
				mediaStreamUpdate.customProvided,
				ReportType.JOINED_PEER_CONNECTION,
				mediaStreamUpdate.created,
				joinedPC
		);
	}
}
