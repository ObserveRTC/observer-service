package org.observertc.webrtc.service.reportsink.bigquery;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Deprecated
public abstract class MediaStreamReportEntry<T extends MediaStreamReportEntry> implements BigQueryEntry {

	private static Logger logger = LoggerFactory.getLogger(MediaStreamReportEntry.class);
	public static final String OBSERVER_UUID_FIELD_NAME = "observerUUID";
	public static final String SSRC_FIELD_NAME = "SSRC";
	public static final String FIRST_SAMPLE_TIMESTAMP_FIELD_NAME = "firstSample";
	public static final String LAST_SAMPLE_TIMESTAMP_FIELD_NAME = "lastSample";
	public static final String PEER_CONNECTION_UUID_FIELD_NAME = "peerConnectionUUID";

	protected final Map<String, Object> values = new HashMap<>();

	public T withObserverUUID(UUID value) {
		this.values.put(OBSERVER_UUID_FIELD_NAME, value.toString());
		return (T) this;
	}


	public T withPeerConnectionUUID(UUID value) {
		this.values.put(PEER_CONNECTION_UUID_FIELD_NAME, value.toString());
		return (T) this;
	}

	public T withSSRC(Long value) {
		this.values.put(SSRC_FIELD_NAME, value);
		return (T) this;
	}

	public T withFirstSampledTimestamp(LocalDateTime firstSample) {
		if (firstSample == null) {
			logger.warn("No First sample");
			return (T) this;
		}
		ZoneId zoneId = ZoneId.systemDefault();
		Long epoch = firstSample.atZone(zoneId).toEpochSecond();
		this.values.put(FIRST_SAMPLE_TIMESTAMP_FIELD_NAME, epoch);
		return (T) this;
	}

	public T withLastSampledTimestamp(LocalDateTime lastSample) {
		if (lastSample == null) {
			logger.warn("No last sample");
			return (T) this;
		}
		ZoneId zoneId = ZoneId.systemDefault();
		Long epoch = lastSample.atZone(zoneId).toEpochSecond();
		this.values.put(LAST_SAMPLE_TIMESTAMP_FIELD_NAME, epoch);
		return (T) this;
	}

	public Map<String, Object> toMap() {
		return this.values;
	}
}