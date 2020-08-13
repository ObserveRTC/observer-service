package org.observertc.webrtc.service.evaluators.mediastreams;

import io.micronaut.context.annotation.Prototype;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.observertc.webrtc.common.UUIDAdapter;
import org.observertc.webrtc.common.reports.InitiatedCallReport;
import org.observertc.webrtc.common.reports.JoinedPeerConnectionReport;
import org.observertc.webrtc.common.reports.Report;
import org.observertc.webrtc.service.EvaluatorsConfig;
import org.observertc.webrtc.service.dto.ActiveStreamDTO;
import org.observertc.webrtc.service.dto.PeerConnectionDTO;
import org.observertc.webrtc.service.jooq.enums.PeerconnectionsState;
import org.observertc.webrtc.service.jooq.tables.records.ActivestreamsRecord;
import org.observertc.webrtc.service.jooq.tables.records.PeerconnectionsRecord;
import org.observertc.webrtc.service.repositories.ActiveStreamKey;
import org.observertc.webrtc.service.repositories.ActiveStreamsRepository;
import org.observertc.webrtc.service.repositories.PeerConnectionsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Prototype
public class MediaStreamUpdateProcessor implements Consumer<Deque<MediaStreamUpdate>> {

	private static final Logger logger = LoggerFactory.getLogger(MediaStreamUpdateProcessor.class);
	private final EvaluatorsConfig.ActiveStreamsConfig config;
	private ProcessorContext context;
	private final ActiveStreamsRepository activeStreamsRepository;
	private final PeerConnectionsRepository peerConnectionsRepository;
	private ReportsBuffer reportsBuffer;

	public MediaStreamUpdateProcessor(
			EvaluatorsConfig.ActiveStreamsConfig config,
			ActiveStreamsRepository activeStreamsRepository,
			PeerConnectionsRepository peerConnectionsRepository) {
		this.config = config;
		this.activeStreamsRepository = activeStreamsRepository;
		this.peerConnectionsRepository = peerConnectionsRepository;
	}

	public void init(ProcessorContext context, ReportsBuffer reportsBuffer) {
		this.context = context;
		this.reportsBuffer = reportsBuffer;
	}

	@Override
	public void accept(Deque<MediaStreamUpdate> updates) {
		List<PeerconnectionsRecord> updatedPCs = new LinkedList<>();
		while (!updates.isEmpty()) {
			MediaStreamUpdate mediaStreamUpdate = updates.removeFirst();
			Optional<PeerconnectionsRecord> peerConnectionsRecordHolder =
					this.peerConnectionsRepository.findById(mediaStreamUpdate.peerConnectionUUID);
			if (peerConnectionsRecordHolder.isPresent()) {
				PeerconnectionsRecord record = peerConnectionsRecordHolder.get();
				if (record.getState().equals(PeerconnectionsState.detached)) {
					// REJOINED
				}
				// Set the update time for the peer connection
				record.setUpdated(mediaStreamUpdate.updated);
				updatedPCs.add(record);
				continue;
			}
			// Check if active streams available
			List<ActiveStreamKey> activeStreamKeys =
					mediaStreamUpdate.SSRCs.stream()
							.map(ssrc -> new ActiveStreamKey(mediaStreamUpdate.observerUUID, ssrc))
							.collect(Collectors.toList());
			List<ActivestreamsRecord> activeStreamsRecords
					= this.activeStreamsRepository.streamByIds(activeStreamKeys.stream()).collect(Collectors.toList());

			if (activeStreamsRecords != null && 0 < activeStreamsRecords.size()) {
				Optional<byte[]> callUUIDBytesHolder =
						activeStreamsRecords.stream().filter(record -> record.getCalluuid() != null).map(ActivestreamsRecord::getCalluuid).findFirst();
				if (!callUUIDBytesHolder.isPresent()) {
					logger.error("Active streams are detected without callUUID");
					continue;
				}
				this.joinPeerConnection(callUUIDBytesHolder.get(), mediaStreamUpdate);
				continue;
			}

			// INITIATED
			UUID callUUID = UUID.randomUUID();
			byte[] callUUIDBytes = UUIDAdapter.toBytes(callUUID);
			List<ActiveStreamDTO> newActiveStreams = activeStreamKeys.stream()
					.map(activeStreamKey -> new ActiveStreamDTO(
							activeStreamKey.getObserverUUIDBytes(),
							activeStreamKey.getSSRC(),
							callUUIDBytes)
					)
					.collect(Collectors.toList());
			try {
				this.activeStreamsRepository.saveAll(newActiveStreams);
			} catch (Exception ex) {
				logger.error("An exception caught during saving data", ex);
				continue;
			}

			Report initiatedCall = InitiatedCallReport.of(
					mediaStreamUpdate.observerUUID,
					callUUID,
					mediaStreamUpdate.created
			);
			updates.addLast(mediaStreamUpdate);
			this.reportsBuffer.accept(initiatedCall);
		}
		this.peerConnectionsRepository.updateAll(updatedPCs);
	}

	private void joinPeerConnection(byte[] callUUIDBytes, MediaStreamUpdate mediaStreamUpdate) {
		this.peerConnectionsRepository.save(new PeerConnectionDTO(
				UUIDAdapter.toBytes(mediaStreamUpdate.peerConnectionUUID),
				mediaStreamUpdate.created,
				mediaStreamUpdate.created,
				PeerconnectionsState.joined,
				mediaStreamUpdate.browserID,
				mediaStreamUpdate.timeZoneID,
				callUUIDBytes,
				UUIDAdapter.toBytes(mediaStreamUpdate.observerUUID)
		));

		Report joinedPeerConnectionReport = JoinedPeerConnectionReport.of(
				mediaStreamUpdate.observerUUID,
				UUIDAdapter.toUUID(callUUIDBytes),
				mediaStreamUpdate.peerConnectionUUID,
				mediaStreamUpdate.browserID,
				mediaStreamUpdate.created,
				mediaStreamUpdate.timeZoneID);

		this.context.forward(mediaStreamUpdate.observerUUID, joinedPeerConnectionReport);
	}

}
