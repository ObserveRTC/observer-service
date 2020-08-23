package org.observertc.webrtc.service.reportsink.bigquery;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.observertc.webrtc.common.reports.MediaType;
import org.observertc.webrtc.common.reports.TrackReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TrackReportEntry implements BigQueryEntry {
	public static final String OBSERVER_UUID_FIELD_NAME = "observerUUID";
	public static final String PEER_CONNECTION_UUID_FIELD_NAME = "peerConnectionUUID";
	public static final String TIMESTAMP_FIELD_NAME = "timestamp";
	public static final String TRACK_ID_FIELD_NAME = "trackID";

	public static final String AUDIO_LEVEL_FIELD_NAME = "audioLevel";
	public static final String CONCEALED_SAMPLES_FIELD_NAME = "concealedSamples";
	public static final String CONCEALMENT_EVENTS_FIELD_NAME = "concealmentEvents";
	public static final String DETACHED_FIELD_NAME = "detached";
	public static final String ENDED_FIELD_NAME = "ended";
	public static final String FRAMES_HEIGHT_FIELD_NAME = "frameHeight";
	public static final String FRAMES_DECODED_FIELD_NAME = "framesDecoded";
	public static final String FRAMES_DROPPED_FIELD_NAME = "framesDropped";
	public static final String FRAMES_RECEIVED_FIELD_NAME = "framesReceived";
	public static final String FRAMES_SENT_FIELD_NAME = "framesSent";
	public static final String FRAMES_WIDTH_FIELD_NAME = "frameWidth";
	public static final String HUGE_FRAMES_SENT_FIELD_NAME = "hugeFramesSent";
	public static final String INSERTED_SAMPLES_FOR_DECELERATION_FIELD_NAME = "insertedSamplesForDeceleration";
	public static final String JITTER_BUFFER_DELAY_FIELD_NAME = "jitterBufferDelay";
	public static final String JITTER_BUFFER_EMITTED_COUNT_FIELD_NAME = "jitterBufferEmittedCount";
	public static final String MEDIA_TYPE_FIELD_NAME = "mediaType";
	public static final String REMOTE_SOURCE_FIELD_NAME = "remoteSource";
	public static final String REMOVED_SAMPLES_FOR_ACCELERATION_FIELD_NAME = "removedSamplesForAcceleration";
	public static final String SILENT_CONCEALED_SAMPLES_FIELD_NAME = "silentConcealedSamples";
	public static final String TOTAL_AUDIO_ENERGY_FIELD_NAME = "totalAudioEnergy";
	public static final String TOTAL_SAMPLES_DURATION_FIELD_NAME = "totalSamplesDuration";
	public static final String TOTAL_SAMPLES_RECEIVED_FIELD_NAME = "totalSamplesReceived";
	public static final String MEDIA_SOURCE_ID_FIELD_NAME = "mediaSourceID";


	private static Logger logger = LoggerFactory.getLogger(TrackReportEntry.class);

	public static TrackReportEntry from(TrackReport report) {
		TrackReportEntry result = new TrackReportEntry()
				.withObserverUUID(report.observerUUID)
				.withPeerConnectionUUID(report.peerConnectionUUID)
				.withTimestamp(report.timestamp)
				.withTrackID(report.trackID)
				.withMediaType(report.mediaType)
				.withAudioLevel(report.audioLevel)
				.withConcealmentEvents(report.concealmentEvents)
				.withDetached(report.detached)
				.withEnded(report.ended)
				.withFramesHeight(report.frameHeight)
				.withConcealedSamples(report.concealedSamples)
				.withFramesDecoded(report.framesDecoded)
				.withFramesDropped(report.framesDropped)
				.withFramesReceived(report.framesReceived)
				.withFramesSent(report.framesSent)
				.withFramesWidth(report.frameWidth)
				.withHugeFramesSent(report.framesSent)
				.withInsertedSamplesForDeceleration(report.insertedSamplesForDeceleration)
				.withJitterBufferDelay(report.jitterBufferDelay)
				.withJitterBufferEmittedCount(report.jitterBufferEmittedCount)
				.withRemoteSource(report.remoteSource)
				.withRemovedSamplesForAcceleration(report.removedSamplesForAcceleration)
				.withSilentConcealedSamples(report.silentConcealedSamples)
				.withTotalAudioEnergy(report.totalAudioEnergy)
				.withTotalSamplesDuration(report.totalSamplesDuration)
				.withTotalSamplesReceived(report.totalSamplesReceived)
				.withMediaSourceID(report.mediaSourceID);
		return result;
	}

	private final Map<String, Object> values;

	public TrackReportEntry() {
		this.values = new HashMap<>();
	}

	public TrackReportEntry withObserverUUID(UUID value) {
		this.values.put(OBSERVER_UUID_FIELD_NAME, value.toString());
		return this;
	}

	public TrackReportEntry withTimestamp(LocalDateTime value) {
		if (value == null) {
			logger.warn("No valid sample timestamp");
			return this;
		}
		Long epoch = BigQueryServiceTimeConverter.getInstance().toEpoch(value);
		this.values.put(TIMESTAMP_FIELD_NAME, epoch);
		return this;
	}

	public TrackReportEntry withPeerConnectionUUID(UUID value) {
		this.values.put(PEER_CONNECTION_UUID_FIELD_NAME, value.toString());
		return this;
	}

	public TrackReportEntry withTrackID(String value) {
		this.values.put(TRACK_ID_FIELD_NAME, value);
		return this;
	}

	public TrackReportEntry withMediaType(MediaType value) {
		String mediaType = null;
		if (value != null) {
			mediaType = value.name();
		}
		this.values.put(MEDIA_TYPE_FIELD_NAME, mediaType);
		return this;
	}

	public TrackReportEntry withAudioLevel(Double value) {
		this.values.put(AUDIO_LEVEL_FIELD_NAME, value);
		return this;
	}

	public TrackReportEntry withConcealmentEvents(Integer value) {
		this.values.put(CONCEALMENT_EVENTS_FIELD_NAME, value);
		return this;
	}


	public TrackReportEntry withDetached(Boolean value) {
		this.values.put(DETACHED_FIELD_NAME, value);
		return this;
	}

	public TrackReportEntry withEnded(Boolean value) {
		this.values.put(ENDED_FIELD_NAME, value);
		return this;
	}

	public TrackReportEntry withFramesHeight(Integer value) {
		this.values.put(FRAMES_HEIGHT_FIELD_NAME, value);
		return this;
	}

	public TrackReportEntry withConcealedSamples(Integer value) {
		this.values.put(CONCEALED_SAMPLES_FIELD_NAME, value);
		return this;
	}

	public TrackReportEntry withFramesDecoded(Integer value) {
		this.values.put(FRAMES_DECODED_FIELD_NAME, value);
		return this;
	}

	public TrackReportEntry withFramesDropped(Integer value) {
		this.values.put(FRAMES_DROPPED_FIELD_NAME, value);
		return this;
	}

	public TrackReportEntry withFramesReceived(Integer value) {
		this.values.put(FRAMES_RECEIVED_FIELD_NAME, value);
		return this;
	}

	public TrackReportEntry withFramesSent(Integer value) {
		this.values.put(FRAMES_SENT_FIELD_NAME, value);
		return this;
	}

	public TrackReportEntry withFramesWidth(Integer value) {
		this.values.put(FRAMES_WIDTH_FIELD_NAME, value);
		return this;
	}

	public TrackReportEntry withHugeFramesSent(Integer value) {
		this.values.put(HUGE_FRAMES_SENT_FIELD_NAME, value);
		return this;
	}

	public TrackReportEntry withInsertedSamplesForDeceleration(Integer value) {
		this.values.put(INSERTED_SAMPLES_FOR_DECELERATION_FIELD_NAME, value);
		return this;
	}

	public TrackReportEntry withJitterBufferDelay(Double value) {
		this.values.put(JITTER_BUFFER_DELAY_FIELD_NAME, value);
		return this;
	}


	public TrackReportEntry withJitterBufferEmittedCount(Integer value) {
		this.values.put(JITTER_BUFFER_EMITTED_COUNT_FIELD_NAME, value);
		return this;
	}

	public TrackReportEntry withRemoteSource(Boolean value) {
		this.values.put(REMOTE_SOURCE_FIELD_NAME, value);
		return this;
	}


	public TrackReportEntry withRemovedSamplesForAcceleration(Integer value) {
		this.values.put(REMOVED_SAMPLES_FOR_ACCELERATION_FIELD_NAME, value);
		return this;
	}

	public TrackReportEntry withSilentConcealedSamples(Integer value) {
		this.values.put(SILENT_CONCEALED_SAMPLES_FIELD_NAME, value);
		return this;
	}


	public TrackReportEntry withTotalAudioEnergy(Double value) {
		this.values.put(TOTAL_AUDIO_ENERGY_FIELD_NAME, value);
		return this;
	}

	public TrackReportEntry withTotalSamplesDuration(Double value) {
		this.values.put(TOTAL_SAMPLES_DURATION_FIELD_NAME, value);
		return this;
	}

	public TrackReportEntry withTotalSamplesReceived(Integer value) {
		this.values.put(TOTAL_SAMPLES_RECEIVED_FIELD_NAME, value);
		return this;
	}

	public TrackReportEntry withMediaSourceID(String value) {
		this.values.put(MEDIA_SOURCE_ID_FIELD_NAME, value);
		return this;
	}

	@Override
	public Map<String, Object> toMap() {
		return this.values;
	}
}
