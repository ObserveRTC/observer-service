package com.observertc.gatekeeper.webrtcstat.bigquery;

import com.observertc.gatekeeper.webrtcstat.samples.MediaStreamKey;
import com.observertc.gatekeeper.webrtcstat.samples.MediaStreamSample;
import com.observertc.gatekeeper.webrtcstat.samples.SampleDescription;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class StreamSample {

	private static Logger logger = LoggerFactory.getLogger(StreamSample.class);

	public static StreamSample from(MediaStreamKey mediaStreamKey, MediaStreamSample mediaStreamSample) {
		StreamSample result = new StreamSample();
		result.observerUUID = mediaStreamKey.observerUUID;
		if (result.observerUUID == null) {
			logger.warn("The observerUUID for PeerConnection {} is null", mediaStreamKey.peerConnectionUUID);
		}
		result.SSRC = mediaStreamKey.SSRC;
		result.peerConnectionUUID = mediaStreamKey.peerConnectionUUID;
		result.bytesReceived = Summary.from(mediaStreamSample.bytesReceived);
		result.bytesSent = Summary.from(mediaStreamSample.bytesSent);
		result.packetsSent = Summary.from(mediaStreamSample.packetsSent);
		result.packetsReceived = Summary.from(mediaStreamSample.packetsReceived);
		result.packetsLost = Summary.from(mediaStreamSample.packetsLost);
//		result.firstSample = mediaStreamSample.first;
//		result.lastSample = mediaStreamSample.last;
		return result;
	}

	public UUID observerUUID;
	public UUID peerConnectionUUID;
	public Long SSRC;
	public LocalDateTime firstSample;
	public LocalDateTime lastSample;
	public Summary RTT = new Summary();
	public Summary bytesReceived = new Summary();
	public Summary bytesSent = new Summary();
	public Summary packetsSent = new Summary();
	public Summary packetsReceived = new Summary();
	public Summary packetsLost = new Summary();

	public Map<String, Object> toMap() {
		Map<String, Object> result = new HashMap();
//		String observerUUIDStr;
		if (this.observerUUID != null) {
			result.put("ObserverUUID", this.observerUUID.toString());
//			observerUUIDStr = this.observerUUID.toString();
		} else {
//			observerUUIDStr = "NOT_AVAILABLE";
		}

//		result.put("ObserverUUID", observerUUIDStr);
		result.put("peerConnectionUUID", this.peerConnectionUUID.toString());
		result.put("SSRC", this.SSRC);
		result.put("RTT", this.RTT.toMap());
		result.put("bytesReceived", this.bytesReceived.toMap());
		result.put("bytesSent", this.bytesSent.toMap());
		result.put("packetsSent", this.packetsSent.toMap());
		result.put("packetsReceived", this.packetsReceived.toMap());
		result.put("packetsLost", this.packetsLost.toMap());

		ZoneId zoneId = ZoneId.systemDefault();
		if (this.firstSample != null) {
			long epoch = this.firstSample.atZone(zoneId).toEpochSecond();
			result.put("firstSample", epoch);
		}
		if (this.lastSample != null) {
			long epoch = this.lastSample.atZone(zoneId).toEpochSecond();
			result.put("lastSample", epoch);
		}
		return result;
	}

	public static class Summary {
		public static Summary from(SampleDescription source) {
			Summary result = new Summary();
			if (source == null) {
				return result;
			}

			result.empty = Long.valueOf(source.empty);

			if (source.min != null) {
				result.minimum = Long.valueOf(source.min);
			}
			if (source.max != null) {
				result.maximum = Long.valueOf(source.max);
			}

			result.presented = Long.valueOf(source.presented);
			result.sum = Long.valueOf(source.sum);
			return result;
		}

		public Long minimum;
		public Long maximum;
		public Long sum;
		public Long presented;
		public Long empty;

		public Map<String, Object> toMap() {
			Map<String, Object> result = new HashMap();
			result.put("minimum", this.minimum);
			result.put("maximum", this.maximum);
			result.put("sum", this.sum);
			result.put("presented", this.presented);
			result.put("empty", this.empty);
			return result;
		}
	}
}