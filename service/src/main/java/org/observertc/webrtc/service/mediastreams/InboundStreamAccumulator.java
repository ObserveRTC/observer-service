package org.observertc.webrtc.service.mediastreams;

import io.micronaut.context.annotation.Prototype;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.apache.kafka.streams.processor.Punctuator;
import org.javatuples.Triplet;
import org.observertc.webrtc.service.dto.InboundStreamMeasurementDTO;
import org.observertc.webrtc.service.repositories.InboundStreamMeasurementsRepository;
import org.observertc.webrtc.service.samples.InboundStreamMeasurement;
import org.observertc.webrtc.service.samples.MediaStreamKey;

@Prototype
public class InboundStreamAccumulator implements Punctuator {

	private final HashMap<MediaStreamKey, List<InboundStreamMeasurement>> measurements;
	private final InboundStreamMeasurementsRepository repository;

	public InboundStreamAccumulator(InboundStreamMeasurementsRepository repository) {
		this.measurements = new HashMap<>();
		this.repository = repository;
	}

	public void init(ProcessorContext context, MediaStreamEvaluatorConfiguration configuration) {

	}

	public void add(MediaStreamKey key, InboundStreamMeasurement measurement) {
		List<InboundStreamMeasurement> measurements = this.measurements.getOrDefault(key, new LinkedList<>());
		measurements.add(measurement);
		this.measurements.put(key, measurements);
	}

	@Override
	public void punctuate(long timestamp) {
		Iterable<Triplet<UUID, UUID, Long>> keys = () ->
				this.measurements.keySet().stream().map(mediaStreamKey -> Triplet.with(mediaStreamKey.observerUUID,
						mediaStreamKey.peerConnectionUUID, mediaStreamKey.SSRC)).iterator();
		Map<Triplet<UUID, UUID, Long>, InboundStreamMeasurementDTO> inboundStreamMeasurementDTOs = this.repository.findByIds(keys);
		Iterator<Map.Entry<MediaStreamKey, List<InboundStreamMeasurement>>> it = this.measurements.entrySet().iterator();
		for (; it.hasNext(); ) {
			Map.Entry<MediaStreamKey, List<InboundStreamMeasurement>> entry = it.next();
			MediaStreamKey mediaStreamKey = entry.getKey();
			List<InboundStreamMeasurement> inboundStreamMeasurements = entry.getValue();
			InboundStreamMeasurementDTO inboundStreamMeasurementDTO = inboundStreamMeasurementDTOs.get(Triplet.with(mediaStreamKey.observerUUID,
					mediaStreamKey.peerConnectionUUID, mediaStreamKey.SSRC));
			if (inboundStreamMeasurementDTO == null) {
				inboundStreamMeasurementDTO = new InboundStreamMeasurementDTO();
				inboundStreamMeasurementDTO.SSRC = mediaStreamKey.SSRC;
				inboundStreamMeasurementDTO.peerConnectionUUID = mediaStreamKey.peerConnectionUUID;
				inboundStreamMeasurementDTO.observerUUID = mediaStreamKey.observerUUID;
			} else if (inboundStreamMeasurementDTO.reported != null) {
				if (inboundStreamMeasurementDTO.lastSample.compareTo(inboundStreamMeasurementDTO.reported) < 0) {
					this.cleanMeasurementDTO(inboundStreamMeasurementDTO);
				}
			}
			inboundStreamMeasurementDTO = this.aggregate(inboundStreamMeasurementDTO, inboundStreamMeasurements);
			inboundStreamMeasurementDTOs.put(Triplet.with(mediaStreamKey.observerUUID, mediaStreamKey.peerConnectionUUID,
					mediaStreamKey.SSRC), inboundStreamMeasurementDTO);
		}
		this.repository.saveAll(inboundStreamMeasurementDTOs.values());
		this.measurements.clear();
	}

	private InboundStreamMeasurementDTO aggregate(InboundStreamMeasurementDTO savedMeasurement, List<InboundStreamMeasurement> inboundStreamMeasurements) {
		Iterator<InboundStreamMeasurement> it = inboundStreamMeasurements.iterator();
		for (; it.hasNext(); ) {
			InboundStreamMeasurement measurement = it.next();
			if (savedMeasurement.firstSample == null) {
				savedMeasurement.firstSample = measurement.sampled;
			}
			savedMeasurement.lastSample = measurement.sampled;
			++savedMeasurement.samples_count;
			this.updatePacketsReceived(savedMeasurement, measurement.packetsReceived);
			this.updatePacketsLost(savedMeasurement, measurement.packetsLost);
			this.updateBytesReceived(savedMeasurement, measurement.bytesReceived);
			this.updateQpSum(savedMeasurement, measurement.qpSum);
		}
		return savedMeasurement;
	}

	private void updateQpSum(InboundStreamMeasurementDTO result, Integer qpSum) {
		if (qpSum == null) {
			return;
		}
		++result.qpSum_count;
		if (result.qpSum_last == null) {
			result.qpSum_last = qpSum;
			return;
		}
		Integer dQpSum = qpSum - result.qpSum_last;
		result.qpSum_sum += dQpSum;
		if (result.qpSum_min == null || dQpSum < result.qpSum_min) {
			result.qpSum_min = dQpSum;
		}
		if (result.qpSum_max == null || result.qpSum_max < dQpSum	) {
			result.qpSum_max = dQpSum;
		}
	}


	private void updatePacketsLost(InboundStreamMeasurementDTO result, Integer packetsLost) {
		if (packetsLost == null) {
			return;
		}
		++result.packetsLost_count;
		if (result.packetsLost_last == null) {
			result.bytesReceived_last = packetsLost;
			return;
		}
		Integer dPacketsLost = packetsLost - result.packetsLost_last;
		result.packetsLost_sum += dPacketsLost;
		if (result.packetsLost_min == null || dPacketsLost < result.packetsLost_min) {
			result.packetsLost_min = dPacketsLost;
		}
		if (result.packetsLost_max == null || result.packetsLost_max < dPacketsLost) {
			result.packetsLost_max = dPacketsLost;
		}
	}

	private void updatePacketsReceived(InboundStreamMeasurementDTO result, Integer packetsReceived) {
		if (packetsReceived == null) {
			return;
		}
		++result.packetsReceived_count;
		if (result.packetsReceived_last == null) {
			result.packetsReceived_last = packetsReceived;
			return;
		}
		Integer dPacketsReceived = packetsReceived - result.packetsReceived_last;
		result.packetsReceived_sum += dPacketsReceived;
		if (result.packetsReceived_min == null || dPacketsReceived < result.packetsReceived_min) {
			result.packetsReceived_min = dPacketsReceived;
		}
		if (result.packetsReceived_max == null || result.packetsReceived_max < dPacketsReceived) {
			result.packetsReceived_max = dPacketsReceived;
		}
	}

	private void updateBytesReceived(InboundStreamMeasurementDTO result, Integer bytesReceived) {
		if (bytesReceived == null) {
			return;
		}
		++result.bytesReceived_count;
		if (result.bytesReceived_last == null) {
			result.bytesReceived_last = bytesReceived;
			return;
		}
		Integer dBytesReceived = bytesReceived - result.bytesReceived_last;
		result.bytesReceived_sum += dBytesReceived;
		if (result.bytesReceived_min == null || dBytesReceived < result.bytesReceived_min) {
			result.bytesReceived_min = dBytesReceived;
		}
		if (result.bytesReceived_max == null || result.bytesReceived_max < dBytesReceived) {
			result.bytesReceived_max = dBytesReceived;
		}
	}


	private void cleanMeasurementDTO(InboundStreamMeasurementDTO inboundStreamMeasurementDTO) {
		inboundStreamMeasurementDTO.firstSample = null;
		inboundStreamMeasurementDTO.lastSample = null;
		inboundStreamMeasurementDTO.packetsLost_count = 0;
		inboundStreamMeasurementDTO.packetsLost_sum = 0;
		inboundStreamMeasurementDTO.packetsLost_min = null;
		inboundStreamMeasurementDTO.packetsLost_max = null;

		inboundStreamMeasurementDTO.packetsReceived_count = 0;
		inboundStreamMeasurementDTO.packetsReceived_sum = 0;
		inboundStreamMeasurementDTO.packetsReceived_min = null;
		inboundStreamMeasurementDTO.packetsReceived_max = null;

		inboundStreamMeasurementDTO.bytesReceived_count = 0;
		inboundStreamMeasurementDTO.bytesReceived_sum = 0L;
		inboundStreamMeasurementDTO.bytesReceived_min = null;
		inboundStreamMeasurementDTO.bytesReceived_max = null;

		inboundStreamMeasurementDTO.qpSum_count = 0;
		inboundStreamMeasurementDTO.qpSum_sum = 0;
		inboundStreamMeasurementDTO.qpSum_min = null;
		inboundStreamMeasurementDTO.qpSum_max = null;
	}

}
