package org.observertc.webrtc.service.processors;

import io.micronaut.context.annotation.Prototype;
import java.util.UUID;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.kstream.Transformer;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.observertc.webrtc.common.reports.ICECandidatePairReport;
import org.observertc.webrtc.common.reports.Report;
import org.observertc.webrtc.service.dto.webextrapp.CandidatePair;
import org.observertc.webrtc.service.dto.webextrapp.ObserveRTCCIceStats;
import org.observertc.webrtc.service.processors.mediastreams.valueadapters.CandidatePairStateConverter;
import org.observertc.webrtc.service.processors.mediastreams.valueadapters.NumberConverter;
import org.observertc.webrtc.service.samples.ICEStatsSample;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Prototype
public class ICEStatsTransformer implements Transformer<UUID, ICEStatsSample, KeyValue<UUID, Report>> {

	private static final Logger logger = LoggerFactory.getLogger(ICEStatsTransformer.class);
	private ProcessorContext context;

	public ICEStatsTransformer() {

	}

	@Override
	public void init(ProcessorContext context) {
		this.context = context;
	}

	@Override
	public KeyValue<UUID, Report> transform(UUID peerConnectionUUID, ICEStatsSample sample) {
		Report report = null;
		if (sample == null) {
			return null;
		}
		ObserveRTCCIceStats iceStats = sample.iceStats;
		if (iceStats == null) {
			return null;
		}
		if (iceStats.getIceCandidatePair() != null) {
			CandidatePair[] candidatePairs = iceStats.getIceCandidatePair();
			for (int i = 0; i < candidatePairs.length; ++i) {
				CandidatePair candidatePair = candidatePairs[i];
				if (candidatePair == null) {
					continue;
				}
				Report iceCandidatePairReport = ICECandidatePairReport.of(sample.observerUUID,
						sample.peerConnectionUUID,
						sample.sampled,
						candidatePair.getNominated(),
						NumberConverter.toInt(candidatePair.getAvailableOutgoingBitrate()),
						NumberConverter.toInt(candidatePair.getBytesReceived()),
						NumberConverter.toInt(candidatePair.getBytesSent()),
						NumberConverter.toInt(candidatePair.getConsentRequestsSent()),
						candidatePair.getCurrentRoundTripTime(),
						NumberConverter.toInt(candidatePair.getPriority()),
						NumberConverter.toInt(candidatePair.getRequestsReceived()),
						NumberConverter.toInt(candidatePair.getRequestsSent()),
						NumberConverter.toInt(candidatePair.getResponsesReceived()),
						NumberConverter.toInt(candidatePair.getResponsesSent()),
						CandidatePairStateConverter.fromState(candidatePair.getState()),
						candidatePair.getTotalRoundTripTime(),
						candidatePair.getWritable()
				);
				this.context.forward(sample.observerUUID, iceCandidatePairReport);
			}
		}
		if (report == null) {
			return null;
		}
		return new KeyValue<>(sample.observerUUID, report);
	}

	@Override
	public void close() {

	}

}
