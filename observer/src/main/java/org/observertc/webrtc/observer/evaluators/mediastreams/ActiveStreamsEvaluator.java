package org.observertc.webrtc.observer.evaluators.mediastreams;

import io.micronaut.configuration.kafka.annotation.KafkaListener;
import io.micronaut.configuration.kafka.annotation.Topic;
import io.micronaut.context.annotation.Property;
import io.micronaut.context.annotation.Prototype;
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
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.observertc.webrtc.common.UUIDAdapter;
import org.observertc.webrtc.common.reports.avro.JoinedPeerConnection;
import org.observertc.webrtc.common.reports.avro.ReportType;
import org.observertc.webrtc.observer.ReportDraftSink;
import org.observertc.webrtc.observer.ReportSink;
import org.observertc.webrtc.observer.dto.AbstractPeerConnectionSampleVisitor;
import org.observertc.webrtc.observer.dto.v20200114.PeerConnectionSample;
import org.observertc.webrtc.observer.evaluators.reportdrafts.InitiatedCallReportDraft;
import org.observertc.webrtc.observer.evaluators.reportdrafts.ReportDraft;
import org.observertc.webrtc.observer.jooq.tables.records.ActivestreamsRecord;
import org.observertc.webrtc.observer.jooq.tables.records.PeerconnectionsRecord;
import org.observertc.webrtc.observer.repositories.ActiveStreamKey;
import org.observertc.webrtc.observer.repositories.ActiveStreamsRepository;
import org.observertc.webrtc.observer.repositories.PeerConnectionsRepository;
import org.observertc.webrtc.observer.samples.ObservedPCS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@KafkaListener(
		groupId = "observertc-webrtc-observer-ActiveStreamsEvaluator",
		batch = true,
		pollTimeout = "5000ms",
		threads = 2,
		properties = {
				@Property(name = ConsumerConfig.FETCH_MAX_WAIT_MS_CONFIG, value = "5000"),
				@Property(name = ConsumerConfig.FETCH_MIN_BYTES_CONFIG, value = "10485760"),
				@Property(name = ConsumerConfig.MAX_POLL_RECORDS_CONFIG, value = "5000")}
)
@Prototype
public class ActiveStreamsEvaluator {

	private static final Logger logger = LoggerFactory.getLogger(ActiveStreamsEvaluator.class);
	private final PeerConnectionsRepository peerConnectionsRepository;
	private final ActiveStreamsRepository activeStreamsRepository;
	private final ReportSink reportSink;
	private final ReportDraftSink reportDraftSink;

	public ActiveStreamsEvaluator(
			PeerConnectionsRepository peerConnectionsRepository,
			ActiveStreamsRepository activeStreamsRepository,
			ReportSink reportSink,
			ReportDraftSink reportDraftSink
	) {
		this.peerConnectionsRepository = peerConnectionsRepository;
		this.activeStreamsRepository = activeStreamsRepository;
		this.reportSink = reportSink;
		this.reportDraftSink = reportDraftSink;
	}


	@Topic("${kafkaTopics.webExtrAppSamples.topicName}")
	public void receive(List<ObservedPCS> samples) {
		Map<UUID, MediaStreamUpdate> updatedPCs = this.makeMediaStreamUpdates(samples);

		if (updatedPCs.size() < 1) {
			return;
		}

		this.updateAndRemoveExistingPCs(updatedPCs);

		if (updatedPCs.size() < 1) {
			return;
		}

		Deque<MediaStreamUpdate> mediaStreamUpdates = new LinkedList<>();
		updatedPCs.values().stream().forEach(mediaStreamUpdates::addLast);
		this.processMediaStreamUpdates(mediaStreamUpdates);
	}

	private Map<UUID, MediaStreamUpdate> makeMediaStreamUpdates(List<ObservedPCS> samples) {
		Map<UUID, MediaStreamUpdate> result = new HashMap<>();
		for (int i = 0; i < samples.size(); i++) {
			ObservedPCS sample = samples.get(i);
			PeerConnectionSample pcSample = sample.peerConnectionSample;
			if (pcSample == null) {
				logger.warn("Peer connection sample is null");
				continue;
			}
			MediaStreamUpdate mediaStreamUpdate = result.get(sample.peerConnectionUUID);
			if (mediaStreamUpdate == null) {
				mediaStreamUpdate = MediaStreamUpdate.of(
						sample.serviceUUID,
						sample.peerConnectionUUID,
						sample.timestamp,
						pcSample.browserID,
						pcSample.callId,
						sample.timeZoneID,
						pcSample.userId,
						sample.mediaUnit,
						sample.serviceName
				);
			} else {
				mediaStreamUpdate.updated = sample.timestamp;
			}

			MediaStreamUpdate finalMediaStreamUpdate = mediaStreamUpdate;
			new AbstractPeerConnectionSampleVisitor<ObservedPCS>() {
				@Override
				public void visitRemoteInboundRTP(ObservedPCS obj, PeerConnectionSample sample, PeerConnectionSample.RemoteInboundRTPStreamStats subject) {
					finalMediaStreamUpdate.SSRCs.add(subject.ssrc);
				}

				@Override
				public void visitInboundRTP(ObservedPCS obj, PeerConnectionSample sample, PeerConnectionSample.InboundRTPStreamStats subject) {
					finalMediaStreamUpdate.SSRCs.add(subject.ssrc);
				}

				@Override
				public void visitOutboundRTP(ObservedPCS obj, PeerConnectionSample sample, PeerConnectionSample.OutboundRTPStreamStats subject) {
					finalMediaStreamUpdate.SSRCs.add(subject.ssrc);
				}
			}.accept(sample, pcSample);
			result.put(sample.peerConnectionUUID, mediaStreamUpdate);
		}
		return result;
	}


	public void updateAndRemoveExistingPCs(Map<UUID, MediaStreamUpdate> updates) {
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
				// something is wrong, log it!
				logger.warn("The PC returned by the repository is not existing in the current update. WTF?!?");
				continue;
			}
			// Set the update time for the peer connection
			record.setUpdated(mediaStreamUpdate.updated);
			updatedPCs.add(record);
			updates.remove(pcUUID);
		}
		this.peerConnectionsRepository.updateAll(updatedPCs);
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
					logger.error("Active streams are detected without callUUID");
					continue;
				}
				callUUIDBytes = callUUIDBytesHolder.get();
			} else {

				Optional<PeerconnectionsRecord> pcHolder =
						this.peerConnectionsRepository.findByJoinedBrowserIDOrProvidedCallID(mediaStreamUpdate.created,
								mediaStreamUpdate.browserID, mediaStreamUpdate.providedCallID);
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
				logger.error("An exception caught during saving data", ex);
				continue;
			}

			ReportDraft reportDraft = InitiatedCallReportDraft.of(
					mediaStreamUpdate.serviceUUID,
					callUUID,
					mediaStreamUpdate.created
			);

			this.reportDraftSink.send(mediaStreamUpdate.peerConnectionUUID, reportDraft);
			newPCs.addLast(mediaStreamUpdate);
		}

	}

	private void joinPeerConnection(byte[] callUUIDBytes, MediaStreamUpdate mediaStreamUpdate) {
		if (this.peerConnectionsRepository.existsById(mediaStreamUpdate.peerConnectionUUID)) {
			logger.warn("Attempted to join a PC {} twice!", mediaStreamUpdate.peerConnectionUUID);
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
					mediaStreamUpdate.providedUserID,
					mediaStreamUpdate.providedCallID,
					mediaStreamUpdate.timeZoneID,
					mediaStreamUpdate.serviceName
			));
		} catch (Exception ex) {
			logger.error("Exception happened at saving new peer connection, report won't be sent", ex);
			return;
		}

		JoinedPeerConnection joinedPC = JoinedPeerConnection.newBuilder()
				.setMediaUnitID(mediaStreamUpdate.mediaUnitID)
				.setCallUUID(new String(callUUIDBytes))
				.setPeerConnectionUUID(mediaStreamUpdate.peerConnectionUUID.toString())
				.build();

		this.reportSink.sendReport(
				mediaStreamUpdate.serviceUUID,
				mediaStreamUpdate.providedCallID,
				mediaStreamUpdate.serviceUUID.toString(),
				ReportType.JOINED_PEER_CONNECTION,
				mediaStreamUpdate.created,
				joinedPC
		);
	}
}
