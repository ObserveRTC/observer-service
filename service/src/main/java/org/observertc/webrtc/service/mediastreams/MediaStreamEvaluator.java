package org.observertc.webrtc.service.mediastreams;

import java.time.Duration;
import java.util.UUID;
import javax.inject.Singleton;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.kstream.Transformer;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.apache.kafka.streams.processor.PunctuationType;
import org.observertc.webrtc.common.reports.Report;
import org.observertc.webrtc.service.dto.webextrapp.RTCStats;
import org.observertc.webrtc.service.dto.webextrapp.RTCStatsType;
import org.observertc.webrtc.service.samples.InboundStreamMeasurement;
import org.observertc.webrtc.service.samples.MediaStreamKey;
import org.observertc.webrtc.service.samples.ObserveRTCMediaStreamStatsSample;
import org.observertc.webrtc.service.samples.OutboundStreamMeasurement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class MediaStreamEvaluator implements Transformer<UUID, ObserveRTCMediaStreamStatsSample, KeyValue<UUID, Report>> {

	private static final Logger logger = LoggerFactory.getLogger(MediaStreamEvaluator.class);

	private final CallsReporter callsReporter;
	private final MediaStreamsReporter mediaStreamsReporter;
	private final MediaStreamEvaluatorConfiguration configuration;

	public MediaStreamEvaluator(
			MediaStreamEvaluatorConfiguration configuration,
			CallsReporter callsReporter,
			MediaStreamsReporter mediaStreamsReporter) {
		this.callsReporter = callsReporter;
		this.mediaStreamsReporter = mediaStreamsReporter;
		this.configuration = configuration;
	}

	@Override
	public void init(ProcessorContext context) {
		this.callsReporter.init(context, this.configuration.callReports);
		this.mediaStreamsReporter.init(context, this.configuration);

		if (this.configuration.callReports.enabled) {
			int updatePeriodInS = this.configuration.callReports.runPeriodInS;
			context.schedule(Duration.ofSeconds(updatePeriodInS), PunctuationType.WALL_CLOCK_TIME, this.callsReporter);
		}
		int updatePeriodInS = this.configuration.reportPeriodInS;
		context.schedule(Duration.ofSeconds(updatePeriodInS), PunctuationType.WALL_CLOCK_TIME, this.mediaStreamsReporter);
	}

	@Override
	public KeyValue<UUID, Report> transform(UUID peerConnectionUUID, ObserveRTCMediaStreamStatsSample sample) {
		Long SSRC = null;
		if (sample != null) {
			if (sample.rtcStats != null) {
				if (sample.rtcStats.getSsrc() != null) {
					SSRC = sample.rtcStats.getSsrc().longValue();
				}
			}
		}
		this.callsReporter.add(peerConnectionUUID, sample);
		MediaStreamKey key = MediaStreamKey.of(sample.observerUUID, peerConnectionUUID, SSRC);
		RTCStats rtcStats = sample.rtcStats;
		if (rtcStats == null) {
			logger.warn("The RTCStats for observer {}, peerConnection {} is null");
			return null;
		}

		switch (rtcStats.getType()) {
			case INBOUND_RTP:
			case REMOTE_INBOUND_RTP:
				InboundStreamMeasurement inboundStreamMeasurement = new InboundStreamMeasurement();
				if (rtcStats.getType().equals(RTCStatsType.REMOTE_INBOUND_RTP)) {
					inboundStreamMeasurement.RTTInMs = this.extractRTTInMs(rtcStats);
				}
				inboundStreamMeasurement.bytesReceived = this.extractBytesReceived(rtcStats);
				inboundStreamMeasurement.packetsReceived = this.extractPacketsReceived(rtcStats);
				inboundStreamMeasurement.packetsLost = this.extractPacketsLost(rtcStats);
				inboundStreamMeasurement.sampled = sample.sampled;
				this.mediaStreamsReporter.addInboundStreamMeasurement(key, inboundStreamMeasurement);
				break;
			case OUTBOUND_RTP:
				OutboundStreamMeasurement outboundStreamMeasurement = new OutboundStreamMeasurement();
				outboundStreamMeasurement.RTTInMs = this.extractRTTInMs(rtcStats);
				outboundStreamMeasurement.bytesSent = this.extractBytesSent(rtcStats);
				outboundStreamMeasurement.packetsSent = this.extractPacketsSent(rtcStats);
				outboundStreamMeasurement.sampled = sample.sampled;
				this.mediaStreamsReporter.addOutboundStreamMeasurement(key, outboundStreamMeasurement);
				break;
		}
		return null;
	}

	@Override
	public void close() {

	}

	private Integer extractRTTInMs(RTCStats sample) {
		Double RTT = sample.getRoundTripTime();
		if (RTT == null) {
			return null;
		}
		return RTT.intValue();
	}

	private Integer extractBytesSent(RTCStats sample) {
		Double bytesSent = sample.getBytesSent();
		if (bytesSent == null) {
			return null;
		}
		return bytesSent.intValue();
	}

	private Integer extractBytesReceived(RTCStats sample) {
		Double bytesReceived = sample.getBytesReceived();
		if (bytesReceived == null) {
			return null;
		}
		return bytesReceived.intValue();
	}

	private Integer extractPacketsSent(RTCStats sample) {
		Double packetsSent = sample.getPacketsSent();
		if (packetsSent == null) {
			return null;
		}
		return packetsSent.intValue();
	}

	private Integer extractPacketsReceived(RTCStats sample) {
		Double packetsReceived = sample.getPacketsReceived();
		if (packetsReceived == null) {
			return null;
		}
		return packetsReceived.intValue();
	}

	private Integer extractPacketsLost(RTCStats sample) {
		Double packetsLost = sample.getPacketsLost();
		if (packetsLost == null) {
			return null;
		}
		return packetsLost.intValue();
	}
}
