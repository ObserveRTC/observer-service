package org.observertc.webrtc.service.mediastreams;

import io.micronaut.context.annotation.Prototype;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.apache.kafka.streams.processor.Punctuator;
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
	private Map<MediaStreamKey, MediaStreamAggregate> reports;
	private ProcessorContext context;

	public MediaStreamsReporter() {
		this.reports = new HashMap<>();
	}

	public void init(ProcessorContext context, MediaStreamEvaluatorConfiguration configuration) {
		this.context = context;
	}

	@Override
	public void punctuate(long timestamp) {

	}

	public void add(MediaStreamKey key, OutboundStreamMeasurement measurement) {
		MediaStreamAggregate report = this.reports.get(key);
		this.aggregate(report, measurement);
	}

	private void aggregate(MediaStreamAggregate result, OutboundStreamMeasurement measurement) {
		if (result.first == null) {
			result.first = measurement.sampled;
		}
		result.last = measurement.sampled;
		this.updateBytesSent(result.bytesSent, measurement.bytesSent);
		this.updatePacketsSent(result.packetsSent, measurement.packetsSent);
	}

	private void updatePacketsSent(MediaStreamAggregateRecord packetsSentRecord, Integer packetsSent) {

	}

	private void updateBytesSent(MediaStreamAggregateRecord bytesSentRecord, Integer bytesSent) {

	}

	private void update(MediaStreamAggregateRecord record, Integer value) {
		++record.count;
		if (value == null) {
			return;
		}
		++record.presented;
		if (record.last == null) {
			record.last = value;
			return;
		}
		Integer dValue = value - record.last;
		if (record.min == null || dValue < record.min) {
			record.min = dValue;
		}

		if (record.max == null || record.max < dValue) {
			record.max = dValue;
		}
		record.sum += value.longValue();
		record.last = value;
	}
}
