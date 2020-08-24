package org.observertc.webrtc.observer.evaluators.mediastreams;

import io.micronaut.context.annotation.Prototype;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Supplier;
import org.observertc.webrtc.observer.EvaluatorsConfig;
import org.observertc.webrtc.observer.ObserverDateTime;
import org.observertc.webrtc.observer.dto.webextrapp.RTCStats;
import org.observertc.webrtc.observer.evaluators.IteratorProvider;
import org.observertc.webrtc.observer.samples.WebExtrAppSample;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Prototype
public class MediaStreamUpdatesProvider implements Consumer<WebExtrAppSample>, Supplier<Map<UUID, MediaStreamUpdate>> {
	private static final Logger logger = LoggerFactory.getLogger(MediaStreamUpdatesProvider.class);
	private final Map<UUID, MediaStreamUpdate> missingBrowserIDs;
	private final Map<UUID, String> surrogatedBrowserIDs;
	private final ObserverDateTime observerDateTime;
	private Map<UUID, MediaStreamUpdate> updates;
	private final EvaluatorsConfig.ActiveStreamsConfig config;

	public MediaStreamUpdatesProvider(
			ObserverDateTime observerDateTime,
			EvaluatorsConfig.ActiveStreamsConfig config) {
		this.config = config;
		this.observerDateTime = observerDateTime;
		this.missingBrowserIDs = new LinkedHashMap<UUID, MediaStreamUpdate>(config.missingBrowserIDMapSize, 0.75f, true) {
			@Override
			protected boolean removeEldestEntry(Map.Entry<UUID, MediaStreamUpdate> eldest) {
				// When to remove the eldest entry.
				return size() > config.missingBrowserIDMapSize; // Size exceeded the max allowed.
			}
		};

		this.surrogatedBrowserIDs = new LinkedHashMap<UUID, String>(config.missingBrowserIDMapSize, 0.75f, true) {
			@Override
			protected boolean removeEldestEntry(Map.Entry<UUID, String> eldest) {
				// When to remove the eldest entry.
				return size() > config.missingBrowserIDMapSize; // Size exceeded the max allowed.
			}
		};
		this.updates = new HashMap<>();
	}

	@Override
	public void accept(WebExtrAppSample sample) {
		Iterator<RTCStats> it = IteratorProvider.makeRTCStatsIt(sample);
		Set<Long> SSRCs = new HashSet<>();
		for (; it.hasNext(); ) {
			RTCStats rtcStats = it.next();
			switch (rtcStats.getType()) {
				case INBOUND_RTP:
				case OUTBOUND_RTP:
				case REMOTE_INBOUND_RTP:
					if (rtcStats.getSsrc() != null) {
						SSRCs.add(rtcStats.getSsrc().longValue());
					}
					break;
			}
		}
		if (SSRCs.size() < 1) {
			return;
		}
		MediaStreamUpdate mediaStreamUpdate = null;
		String browserID = sample.peerConnectionSample.getBrowserId();
		if (browserID == null) {
			browserID = this.surrogatedBrowserIDs.get(sample.peerConnectionUUID);
		} else {
			mediaStreamUpdate = this.missingBrowserIDs.get(sample.peerConnectionUUID);
			if (mediaStreamUpdate != null) {
				mediaStreamUpdate.browserID = browserID;
				this.missingBrowserIDs.remove(sample.peerConnectionUUID);
			}
		}

		if (browserID == null) {
			mediaStreamUpdate = this.missingBrowserIDs.get(sample.peerConnectionUUID);
			if (mediaStreamUpdate == null) {
				this.missingBrowserIDs.put(sample.peerConnectionUUID, this.makeMediaStreamUpdate(sample, browserID));
				return;
			}
			long elapsed = ChronoUnit.SECONDS.between(mediaStreamUpdate.created, this.observerDateTime.now());
			if (this.config.maxTimeBrowserIdCanMiss < elapsed) {
				logger.info("Sample for PC {} does not have a browserID after {}s, so the peer connection is used as browserID",
						mediaStreamUpdate.peerConnectionUUID.toString(), this.config.maxTimeBrowserIdCanMiss);
				this.missingBrowserIDs.remove(sample.peerConnectionUUID);
				mediaStreamUpdate.browserID = browserID = sample.peerConnectionUUID.toString();
				this.surrogatedBrowserIDs.put(sample.peerConnectionUUID, browserID);
			} else {
				mediaStreamUpdate.updated = sample.timestamp;
				mediaStreamUpdate.SSRCs.addAll(SSRCs);
				return;
			}
		}

		if (mediaStreamUpdate == null) {
			mediaStreamUpdate = this.updates.getOrDefault(sample.peerConnectionUUID, this.makeMediaStreamUpdate(sample, browserID));
		}

		mediaStreamUpdate.updated = sample.timestamp;
		mediaStreamUpdate.SSRCs.addAll(SSRCs);
		this.updates.put(sample.peerConnectionUUID, mediaStreamUpdate);
	}

	@Override
	public Map<UUID, MediaStreamUpdate> get() {
		Map<UUID, MediaStreamUpdate> result = this.updates;
		this.updates = new HashMap<>();
		return result;
	}

	private MediaStreamUpdate makeMediaStreamUpdate(WebExtrAppSample sample, String browserID) {
		return MediaStreamUpdate.of(
				sample.observerUUID,
				sample.peerConnectionUUID,
				sample.timestamp,
				browserID,
				sample.sampleTimeZoneID
		);
	}
}
