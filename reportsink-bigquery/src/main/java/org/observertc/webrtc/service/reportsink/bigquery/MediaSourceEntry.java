package org.observertc.webrtc.service.reportsink.bigquery;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.observertc.webrtc.common.reports.MediaSourceReport;
import org.observertc.webrtc.common.reports.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MediaSourceEntry implements BigQueryEntry {

	private static Logger logger = LoggerFactory.getLogger(MediaSourceEntry.class);

	public static final String OBSERVER_UUID_FIELD_NAME = "observerUUID";
	public static final String PEER_CONNECTION_UUID_FIELD_NAME = "peerConnectionUUID";
	public static final String TIMESTAMP_FIELD_NAME = "timestamp";
	public static final String MEDIA_SOURCE_ID_FIELD_NAME = "mediaSourceID";
	public static final String FRAMES_PER_SECOND_FIELD_NAME = "framesPerSecond";
	public static final String HEIGHT_FIELD_NAME = "height";
	public static final String WIDTH_FIELD_NAME = "width";
	public static final String AUDIO_LEVEL_FIELD_NAME = "audioLevel";
	public static final String MEDIA_TYPE_FIELD_NAME = "mediaType";
	public static final String TOTAL_AUDIO_ENERGY_FIELD_NAME = "totalAudioEnergy";
	public static final String TOTAL_SAMPLES_DURATION_FIELD_NAME = "totalSamplesDuration";


	public static MediaSourceEntry from(MediaSourceReport mediaSourceReport) {
		if (mediaSourceReport == null) {
			return null;
		}
		return new MediaSourceEntry()
				.withObserverUUID(mediaSourceReport.observerUUID)
				.withPeerConnectionUUID(mediaSourceReport.peerConnectionUUID)
				.withTimestamp(mediaSourceReport.timestamp)
				.withMediaSourceID(mediaSourceReport.mediaSourceID)
				.withAudioLevel(mediaSourceReport.audioLevel)
				.withFramesPerSecond(mediaSourceReport.framesPerSecond)
				.withHeight(mediaSourceReport.height)
				.withWidth(mediaSourceReport.width)
				.withAudioLevel(mediaSourceReport.audioLevel)
				.withMediaType(mediaSourceReport.mediaType)
				.withTotalAudioEnergy(mediaSourceReport.totalAudioEnergy)
				.withTotalSamplesDuration(mediaSourceReport.totalSamplesDuration)
				;
	}

	private MediaSourceEntry withMediaSourceID(String value) {
		this.values.put(MEDIA_SOURCE_ID_FIELD_NAME, value);
		return this;
	}

	private MediaSourceEntry withFramesPerSecond(Double value) {
		this.values.put(FRAMES_PER_SECOND_FIELD_NAME, value);
		return this;
	}

	private MediaSourceEntry withMediaType(MediaType value) {
		String mediaType = null;
		if (value != null) {
			mediaType = value.name();
		}
		this.values.put(MEDIA_TYPE_FIELD_NAME, mediaType);
		return this;
	}

	private MediaSourceEntry withAudioLevel(Double value) {
		this.values.put(AUDIO_LEVEL_FIELD_NAME, value);
		return this;
	}

	private MediaSourceEntry withTotalAudioEnergy(Double value) {
		this.values.put(TOTAL_AUDIO_ENERGY_FIELD_NAME, value);
		return this;
	}

	private MediaSourceEntry withTotalSamplesDuration(Double value) {
		this.values.put(TOTAL_SAMPLES_DURATION_FIELD_NAME, value);
		return this;
	}


	private MediaSourceEntry withHeight(Integer value) {
		this.values.put(HEIGHT_FIELD_NAME, value);
		return this;
	}

	private MediaSourceEntry withWidth(Integer value) {
		this.values.put(WIDTH_FIELD_NAME, value);
		return this;
	}

	public MediaSourceEntry withTimestamp(LocalDateTime value) {
		if (value == null) {
			logger.warn("No valid sample timestamp");
			return this;
		}
		Long epoch = BigQueryServiceTimeConverter.getInstance().toEpoch(value);
		this.values.put(TIMESTAMP_FIELD_NAME, epoch);
		return this;
	}

	private final Map<String, Object> values;

	public MediaSourceEntry() {
		this.values = new HashMap<>();
	}

	public MediaSourceEntry withObserverUUID(UUID value) {
		this.values.put(OBSERVER_UUID_FIELD_NAME, value.toString());
		return this;
	}

	public MediaSourceEntry withPeerConnectionUUID(UUID value) {
		this.values.put(PEER_CONNECTION_UUID_FIELD_NAME, value.toString());
		return this;
	}

	public UUID getObserverUUID() {
		String value = (String) this.values.get(OBSERVER_UUID_FIELD_NAME);
		if (value == null) {
			return null;
		}
		return UUID.fromString(value);
	}

	public UUID getPeerConnectionUUID() {
		String value = (String) this.values.get(PEER_CONNECTION_UUID_FIELD_NAME);
		if (value == null) {
			return null;
		}
		return UUID.fromString(value);
	}

	public Map<String, Object> toMap() {
		return this.values;
	}
}
