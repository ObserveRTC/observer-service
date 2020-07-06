package org.observertc.webrtc.observer.service.processors;

import org.observertc.webrtc.observer.service.dto.webextrapp.RTCStats;
import org.observertc.webrtc.observer.service.samples.InboundStreamMeasurement;
import org.observertc.webrtc.observer.service.samples.MediaStreamKey;
import org.observertc.webrtc.observer.service.samples.ObserveRTCMediaStreamStatsSample;
import org.observertc.webrtc.observer.service.samples.OutboundStreamMeasurement;
import java.time.Duration;
import java.util.UUID;
import javax.inject.Singleton;
import org.apache.kafka.streams.processor.Processor;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.apache.kafka.streams.processor.PunctuationType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class InAndOutboundStreamStatsSamplesProcessor implements Processor<UUID, ObserveRTCMediaStreamStatsSample> {

	private static final Logger logger = LoggerFactory.getLogger(InAndOutboundStreamStatsSamplesProcessor.class);

	private final MediaStreamSampleReportPunctuator mediaStreamSampleReportPunctuator;
	private ProcessorContext context;

	public InAndOutboundStreamStatsSamplesProcessor(MediaStreamSampleReportPunctuator mediaStreamSampleReportPunctuator) {
		this.mediaStreamSampleReportPunctuator = mediaStreamSampleReportPunctuator;
	}

	@Override
	public void init(ProcessorContext context) {
		// keep the processor context locally because we need it in punctuate() and commit()
		this.context = context;
		this.mediaStreamSampleReportPunctuator.init(context);
		int updatePeriodInS = 30;
		this.context.schedule(Duration.ofSeconds(updatePeriodInS), PunctuationType.WALL_CLOCK_TIME, this.mediaStreamSampleReportPunctuator);
	}

	@Override
	public void close() {

	}

	@Override
	public void process(UUID pcUUID, ObserveRTCMediaStreamStatsSample sample) {
		Long SSRC = null;
		if (sample != null) {
			if (sample.rtcStats != null) {
				if (sample.rtcStats.getSsrc() != null) {
					SSRC = sample.rtcStats.getSsrc().longValue();
				}
			}
		}

		MediaStreamKey key = MediaStreamKey.of(sample.observerUUID, pcUUID, SSRC);
		RTCStats rtcStats = sample.rtcStats;
		if (rtcStats == null) {
			logger.warn("The RTCStats for observer {}, peerConnection {} is null");
			return;
		}
		switch (rtcStats.getType()) {
			case INBOUND_RTP:
			case REMOTE_INBOUND_RTP:
				InboundStreamMeasurement inboundStreamMeasurement = new InboundStreamMeasurement();
				inboundStreamMeasurement.RTTInMs = this.extractRTTInMs(rtcStats);
				inboundStreamMeasurement.bytesReceived = this.extractBytesReceived(rtcStats);
				inboundStreamMeasurement.packetsReceived = this.extractPacketsReceived(rtcStats);
				inboundStreamMeasurement.packetsLost = this.extractPacketsLost(rtcStats);
				inboundStreamMeasurement.sampled = sample.sampled;
				this.mediaStreamSampleReportPunctuator.addInboundStreamMeasurement(key, inboundStreamMeasurement);
				break;
			case OUTBOUND_RTP:
				OutboundStreamMeasurement outboundStreamMeasurement = new OutboundStreamMeasurement();
				outboundStreamMeasurement.RTTInMs = this.extractRTTInMs(rtcStats);
				outboundStreamMeasurement.bytesSent = this.extractBytesSent(rtcStats);
				outboundStreamMeasurement.packetsSent = this.extractPacketsSent(rtcStats);
				outboundStreamMeasurement.sampled = sample.sampled;
				this.mediaStreamSampleReportPunctuator.addOutboundStreamMeasurement(key, outboundStreamMeasurement);
				break;
		}
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
