package org.observertc.webrtc.service.processors;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import javax.inject.Singleton;
import org.apache.kafka.streams.kstream.Aggregator;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.apache.kafka.streams.processor.Punctuator;
import org.observertc.webrtc.common.reports.MediaStreamSample;
import org.observertc.webrtc.common.reports.MediaStreamSampleRecord;
import org.observertc.webrtc.common.reportsink.MediaStreamReports;
import org.observertc.webrtc.common.reportsink.ReportService;
import org.observertc.webrtc.service.micrometer.WebRTCStatsReporter;
import org.observertc.webrtc.service.reportsink.ReportServiceProvider;
import org.observertc.webrtc.service.samples.InboundStreamMeasurement;
import org.observertc.webrtc.service.samples.MediaStreamAggregate;
import org.observertc.webrtc.service.samples.MediaStreamAggregateRecord;
import org.observertc.webrtc.service.samples.MediaStreamKey;
import org.observertc.webrtc.service.samples.OutboundStreamMeasurement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class MediaStreamSampleReportPunctuator implements Punctuator {

	private static final Logger logger = LoggerFactory.getLogger(MediaStreamSampleReportPunctuator.class);

	private LocalDateTime reported = null;
	private Map<MediaStreamKey, MediaStreamAggregate> mediaStreamSamples;
	private final Aggregator<MediaStreamKey, InboundStreamMeasurement, MediaStreamAggregate> inboundStreamMeasurementAggregator;
	private final Aggregator<MediaStreamKey, OutboundStreamMeasurement, MediaStreamAggregate> outboundStreamMeasurementAggregator;
	private final WebRTCStatsReporter webRTCStatsReporter;
	private final ReportService reportService;

	public MediaStreamSampleReportPunctuator(ReportServiceProvider reportServiceProvider, WebRTCStatsReporter webRTCStatsReporter) {
		this.mediaStreamSamples = new HashMap<>();
		this.reportService = reportServiceProvider.getReportService();
		this.inboundStreamMeasurementAggregator = new InboundStreamMeasurementAggregator();
		this.outboundStreamMeasurementAggregator = new OutboundStreamMeasurementAggregator();
		this.webRTCStatsReporter = webRTCStatsReporter;
	}

	@Override
	public void punctuate(long timestamp) {
		MediaStreamReports mediaStreamReports = this.reportService.getMediaStreamReports();
		if (mediaStreamReports == null) {
			logger.warn("There is no MediaStreamReporter");
			return;
		}
		Iterator<Map.Entry<MediaStreamKey, MediaStreamAggregate>> it = this.mediaStreamSamples.entrySet().iterator();
		for (; it.hasNext(); ) {
			Map.Entry<MediaStreamKey, MediaStreamAggregate> entry = it.next();
			MediaStreamKey mediaStreamKey = entry.getKey();
			MediaStreamAggregate mediaStreamAggregate = entry.getValue();
			if (this.reported != null && mediaStreamAggregate.last.compareTo(this.reported) < 0) {
				it.remove();
				continue;
			}
			MediaStreamSample mediaStreamSample = this.makeMediaStreamSample(mediaStreamKey, mediaStreamAggregate);
			mediaStreamReports.sample(mediaStreamSample);
//			this.webRTCStatsReporter.reportMediaStreamSample(mediaStreamKey, mediaStreamSampleImpl);
			this.cleanMediaStream(mediaStreamAggregate);
		}
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

	public void init(ProcessorContext context) {

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

	private MediaStreamSample makeMediaStreamSample(MediaStreamKey mediaStreamKey, MediaStreamAggregate mediaStreamAggregate) {
		final UUID observerUUID = mediaStreamKey.observerUUID;
		final UUID peerConnectionUUID = mediaStreamKey.peerConnectionUUID;
		final Long SSRC = mediaStreamKey.SSRC;
		final MediaStreamSampleRecord RTT = makeMediaStreamSampleRecord(mediaStreamAggregate.RTTInMs);
		final MediaStreamSampleRecord bytesReceived = makeMediaStreamSampleRecord(mediaStreamAggregate.bytesReceived);
		final MediaStreamSampleRecord bytesSent = makeMediaStreamSampleRecord(mediaStreamAggregate.bytesSent);
		final MediaStreamSampleRecord packetsReceived = makeMediaStreamSampleRecord(mediaStreamAggregate.packetsReceived);
		final MediaStreamSampleRecord packetsSent = makeMediaStreamSampleRecord(mediaStreamAggregate.packetsSent);
		final MediaStreamSampleRecord packetsLost = makeMediaStreamSampleRecord(mediaStreamAggregate.packetsLost);
		final LocalDateTime firstSample = mediaStreamAggregate.first;
		final LocalDateTime lastSample = mediaStreamAggregate.last;
		return new MediaStreamSample() {
			@Override
			public UUID getPeerConnectionUUID() {
				return peerConnectionUUID;
			}

			@Override
			public UUID getObserverUUID() {
				return observerUUID;
			}

			@Override
			public Long getSSRC() {
				return SSRC;
			}

			@Override
			public MediaStreamSampleRecord getRTTRecord() {
				return RTT;
			}

			@Override
			public MediaStreamSampleRecord getBytesReceivedRecord() {
				return bytesReceived;
			}

			@Override
			public MediaStreamSampleRecord getBytesSentRecord() {
				return bytesSent;
			}

			@Override
			public MediaStreamSampleRecord getPacketsSentRecord() {
				return packetsSent;
			}

			@Override
			public MediaStreamSampleRecord getPacketsReceivedRecord() {
				return packetsReceived;
			}

			@Override
			public MediaStreamSampleRecord getPacketsLostRecord() {
				return packetsLost;
			}

			@Override
			public LocalDateTime getFirstSampleTimestamp() {
				return firstSample;
			}

			@Override
			public LocalDateTime getLastSampleTimestamp() {
				return lastSample;
			}
		};
	}

	private MediaStreamSampleRecord makeMediaStreamSampleRecord(MediaStreamAggregateRecord record) {
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
		return new MediaStreamSampleRecord() {


			@Override
			public Long getMinimum() {
				return minimum;
			}

			@Override
			public Long getMaximum() {
				return maximum;
			}

			@Override
			public Long getPresented() {
				return presented;
			}

			@Override
			public Long getEmpty() {
				return empty;
			}

			@Override
			public Long getSum() {
				return sum;
			}
		};
	}

}
