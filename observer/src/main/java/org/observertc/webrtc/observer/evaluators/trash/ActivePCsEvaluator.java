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

package org.observertc.webrtc.observer.evaluators.trash;

import io.micronaut.context.annotation.Prototype;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.subjects.PublishSubject;
import io.reactivex.rxjava3.subjects.Subject;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.jooq.lambda.tuple.Tuple2;
import org.observertc.webrtc.common.UUIDAdapter;
import org.observertc.webrtc.observer.evaluators.trash.reportdrafts.InitiatedCallReportDraft;
import org.observertc.webrtc.observer.evaluators.trash.reportdrafts.ReportDraft;
import org.observertc.webrtc.observer.jooq.tables.records.ActivestreamsRecord;
import org.observertc.webrtc.observer.jooq.tables.records.PeerconnectionsRecord;
import org.observertc.webrtc.observer.micrometer.CountedLogMonitor;
import org.observertc.webrtc.observer.micrometer.MetricsReporter;
import org.observertc.webrtc.observer.repositories.mysql.ActiveStreamKey;
import org.observertc.webrtc.observer.repositories.mysql.ActiveStreamsRepository;
import org.observertc.webrtc.observer.repositories.mysql.PeerConnectionsRepository;
import org.observertc.webrtc.schemas.reports.JoinedPeerConnection;
import org.observertc.webrtc.schemas.reports.Report;
import org.observertc.webrtc.schemas.reports.ReportType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;

@Prototype
@Deprecated
public class ActivePCsEvaluator implements Observer<List<MediaStreamUpdate>> {

	private static final Logger logger = LoggerFactory.getLogger(ActivePCsEvaluator.class);

	private final PublishSubject<ReportDraft> initiatedCallsSubject = PublishSubject.create();
	private final PublishSubject<Tuple2<UUID, Report>> joinedPeerConnections = PublishSubject.create();
	private Disposable subscription;

	private final PeerConnectionsRepository peerConnectionsRepository;
	private final ActiveStreamsRepository activeStreamsRepository;
	private final MetricsReporter metricsReporter;
	private final CountedLogMonitor countedLogMonitor;

	public ActivePCsEvaluator(
			MetricsReporter metricsReporter,
			PeerConnectionsRepository peerConnectionsRepository,
			ActiveStreamsRepository activeStreamsRepository
	) {
		this.metricsReporter = metricsReporter;
		this.peerConnectionsRepository = peerConnectionsRepository;
		this.activeStreamsRepository = activeStreamsRepository;

		this.countedLogMonitor = metricsReporter
				.makeCountedLogMonitor(logger)
				.withDefaultMetricName("activeStreamsEvaluator")
		;
	}

	@Override
	public void onSubscribe(@NonNull Disposable d) {
		this.subscription = d;
	}

	@Override
	public void onNext(@NonNull List<MediaStreamUpdate> mediaStreamUpdates) {
		if (this.subscription != null && this.subscription.isDisposed()) {
			logger.warn("Updates arrived, however the subscription is disposed");
			return;
		}
		if (mediaStreamUpdates.size() < 1) {
			return;
		}
		Map<UUID, MediaStreamUpdate> updates = new HashMap<>();
		for (MediaStreamUpdate mediaStreamUpdate : mediaStreamUpdates) {
			MediaStreamUpdate prevValue = updates.put(mediaStreamUpdate.peerConnectionUUID, mediaStreamUpdate);
			if (prevValue != null) {
				logger.warn("Duplicated value occured for PC {}, the new one overrode the old", mediaStreamUpdate.peerConnectionUUID);
			}
		}
		int toProcess = updates.size();
		try {
			this.update(updates);
			this.metricsReporter.incrementProcessedMediaUpdates(toProcess);
		} catch (Exception ex) {
			logger.error("Error happened during update", ex);
		}

	}

	@Override
	public void onError(@NonNull Throwable e) {
		logger.error("Error is reported in the observer pipeline", e);
	}

	@Override
	public void onComplete() {
		logger.info("onComplete event is called");
	}


	public Subject<Tuple2<UUID, Report>> getJoinedPeerConnectionSubject() {
		return this.joinedPeerConnections;
	}

	public Subject<ReportDraft> getInitiatedCallSubject() {
		return this.initiatedCallsSubject;
	}

	private void update(Map<UUID, MediaStreamUpdate> updates) {
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
		if (0 < updatedPCs.size()) {
			this.peerConnectionsRepository.updateAll(updatedPCs);
		}


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
				this.activeStreamsRepository.saveAll(newActiveStreams);
			} catch (Exception ex) {
				this.countedLogMonitor
						.makeEntry()
						.withCategory("registerCallUUID." + ex.getClass().getSimpleName())
						.withLogLevel(Level.ERROR)
						.withException(ex)
						.withMessage("An exception caught during saving data.")
						.log();
				continue;
			}

			if (activeStreamKeys.size() < 1) {
				logger.warn("An update indicated a new call, but it does not have any SSRC. Theerefore the update will be drpped. {}", mediaStreamUpdate);
				continue;
			}

			ReportDraft reportDraft = InitiatedCallReportDraft.of(
					mediaStreamUpdate.serviceUUID,
					callUUID,
					mediaStreamUpdate.marker,
					mediaStreamUpdate.created
			);

			this.initiatedCallsSubject.onNext(reportDraft);
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
			PeerconnectionsRecord record = new PeerconnectionsRecord(
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
			);
			this.peerConnectionsRepository.save(record);
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
				.setBrowserId(mediaStreamUpdate.browserID)
				.setMediaUnitId(mediaStreamUpdate.mediaUnitID)
				.setCallUUID(callUUIDStr)
				.setPeerConnectionUUID(mediaStreamUpdate.peerConnectionUUID.toString())
				.build();

		Report report = Report.newBuilder()
				.setServiceUUID(mediaStreamUpdate.serviceUUID.toString())
				.setServiceName(mediaStreamUpdate.serviceName)
				.setMarker(mediaStreamUpdate.marker)
				.setType(ReportType.JOINED_PEER_CONNECTION)
				.setTimestamp(mediaStreamUpdate.created)
				.setPayload(joinedPC)
				.build();
		Tuple2<UUID, Report> tuple = new Tuple2<>(mediaStreamUpdate.peerConnectionUUID, report);
		this.joinedPeerConnections.onNext(tuple);
	}


}
