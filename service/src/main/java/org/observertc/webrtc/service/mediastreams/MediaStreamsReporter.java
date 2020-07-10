package org.observertc.webrtc.service.mediastreams;

import io.micronaut.context.annotation.Prototype;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import org.apache.kafka.streams.kstream.Aggregator;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.apache.kafka.streams.processor.Punctuator;
import org.observertc.webrtc.common.reports.MediaStreamSampleRecordReport;
import org.observertc.webrtc.common.reports.MediaStreamSampleReport;
import org.observertc.webrtc.service.samples.InboundStreamMeasurement;
import org.observertc.webrtc.service.samples.MediaStreamAggregate;
import org.observertc.webrtc.service.samples.MediaStreamAggregateRecord;
import org.observertc.webrtc.service.samples.MediaStreamKey;
import org.observertc.webrtc.service.samples.OutboundStreamMeasurement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Prototype
public class MediaStreamsReporter implements Punctuator {
	private static final Logger logger = LoggerFactory.getLogger(MediaStreamsReporter.class);

	private LocalDateTime reported = null;
	private Map<MediaStreamKey, MediaStreamAggregate> mediaStreamSamples;
	private final Aggregator<MediaStreamKey, InboundStreamMeasurement, MediaStreamAggregate> inboundStreamMeasurementAggregator;
	private final Aggregator<MediaStreamKey, OutboundStreamMeasurement, MediaStreamAggregate> outboundStreamMeasurementAggregator;
	private ProcessorContext context;

	public MediaStreamsReporter() {
		this.mediaStreamSamples = new HashMap<>();
		this.inboundStreamMeasurementAggregator = new InboundStreamAggregator();
		this.outboundStreamMeasurementAggregator = new OutboundStreamAggregator();
	}

	public void init(ProcessorContext context, MediaStreamEvaluatorConfiguration configuration) {
		this.context = context;
	}

	@Override
	public void punctuate(long timestamp) {
		Iterator<Map.Entry<MediaStreamKey, MediaStreamAggregate>> it = this.mediaStreamSamples.entrySet().iterator();
		for (; it.hasNext(); ) {
			Map.Entry<MediaStreamKey, MediaStreamAggregate> entry = it.next();
			MediaStreamKey mediaStreamKey = entry.getKey();
			MediaStreamAggregate mediaStreamAggregate = entry.getValue();
			if (this.reported != null && mediaStreamAggregate.last.compareTo(this.reported) < 0) {
				it.remove();
				continue;
			}
			MediaStreamSampleReport report = this.makeMediaStreamSampleReport(mediaStreamKey, mediaStreamAggregate);
			this.context.forward(mediaStreamKey.observerUUID, report);
			this.cleanMediaStream(mediaStreamAggregate);
		}

		this.reported = LocalDateTime.now();
	}

	private void cleanMediaStream(MediaStreamAggregate mediaStreamAggregate) {
		mediaStreamAggregate.first = null;
		this.cleanSampleDescription(mediaStreamAggregate.bytesReceived);
		this.cleanSampleDescription(mediaStreamAggregate.bytesSent);
		this.cleanSampleDescription(mediaStreamAggregate.packetsSent);
		this.cleanSampleDescription(mediaStreamAggregate.packetsReceived);
		this.cleanSampleDescription(mediaStreamAggregate.packetsLost);
		this.cleanSampleDescription(mediaStreamAggregate.RTTInMs);
	}

	private void cleanSampleDescription(MediaStreamAggregateRecord mediaStreamAggregateRecord) {
		mediaStreamAggregateRecord.sum = 0;
		mediaStreamAggregateRecord.min = null;
		mediaStreamAggregateRecord.max = null;
		mediaStreamAggregateRecord.presented = 0;
		mediaStreamAggregateRecord.empty = 0;
	}

	public void addInboundStreamMeasurement(MediaStreamKey key, InboundStreamMeasurement measurement) {
		this.executeAggregator(this.inboundStreamMeasurementAggregator, key, measurement);
	}

	public void addOutboundStreamMeasurement(MediaStreamKey key, OutboundStreamMeasurement measurement) {
		this.executeAggregator(this.outboundStreamMeasurementAggregator, key, measurement);
	}


	private <T> void executeAggregator(Aggregator<MediaStreamKey, T, MediaStreamAggregate> aggregator, MediaStreamKey key,
									   T measurement) {
		MediaStreamAggregate mediaStreamAggregate = this.getMediaStreamSample(key);
		aggregator.apply(key, measurement, mediaStreamAggregate);
		this.postCheck(mediaStreamAggregate);
		this.mediaStreamSamples.put(key, mediaStreamAggregate);
	}

	private MediaStreamAggregate getMediaStreamSample(MediaStreamKey key) {
		MediaStreamAggregate result = this.mediaStreamSamples.get(key);
		if (result == null) {
			result = new MediaStreamAggregate();
		}
		return result;
	}

	private void postCheck(MediaStreamAggregate mediaStreamAggregate) {
		if (mediaStreamAggregate.last == null) {
			logger.warn("There was no last timestamp for the mediaStreamSample. It would crash the app, so we wet manually, but its " +
					"inaccurate");
			mediaStreamAggregate.last = LocalDateTime.now();
		}
	}

	private MediaStreamSampleReport makeMediaStreamSampleReport(MediaStreamKey mediaStreamKey, MediaStreamAggregate mediaStreamAggregate) {
		final MediaStreamSampleRecordReport RTT = makeMediaStreamSampleRecord(mediaStreamAggregate.RTTInMs);
		final MediaStreamSampleRecordReport bytesReceived = makeMediaStreamSampleRecord(mediaStreamAggregate.bytesReceived);
		final MediaStreamSampleRecordReport bytesSent = makeMediaStreamSampleRecord(mediaStreamAggregate.bytesSent);
		final MediaStreamSampleRecordReport packetsReceived = makeMediaStreamSampleRecord(mediaStreamAggregate.packetsReceived);
		final MediaStreamSampleRecordReport packetsSent = makeMediaStreamSampleRecord(mediaStreamAggregate.packetsSent);
		final MediaStreamSampleRecordReport packetsLost = makeMediaStreamSampleRecord(mediaStreamAggregate.packetsLost);

		return MediaStreamSampleReport.of(mediaStreamKey.observerUUID,
				mediaStreamKey.peerConnectionUUID,
				mediaStreamKey.SSRC,
				mediaStreamAggregate.first,
				mediaStreamAggregate.last,
				RTT,
				bytesReceived,
				bytesSent,
				packetsLost,
				packetsReceived,
				packetsSent
		);
	}

	private MediaStreamSampleRecordReport makeMediaStreamSampleRecord(MediaStreamAggregateRecord record) {
		final Long minimum;
		if (!Objects.isNull(record.min)) {
			minimum = record.min.longValue();
		} else {

			minimum = null;
		}
		final Long maximum;
		if (!Objects.isNull(record.max)) {
			maximum = record.max.longValue();
		} else {
			maximum = null;
		}
		final Long sum = Long.valueOf(record.sum);
		final Long presented = Long.valueOf(record.presented);
		final Long empty = Long.valueOf(record.empty);
		return MediaStreamSampleRecordReport.of(minimum, maximum, presented, empty, sum);
	}
}
