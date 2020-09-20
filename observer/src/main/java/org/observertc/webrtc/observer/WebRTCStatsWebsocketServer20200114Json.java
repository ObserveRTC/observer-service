package org.observertc.webrtc.observer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import io.micronaut.websocket.WebSocketSession;
import io.micronaut.websocket.annotation.OnClose;
import io.micronaut.websocket.annotation.OnMessage;
import io.micronaut.websocket.annotation.OnOpen;
import io.micronaut.websocket.annotation.ServerWebSocket;
import java.io.IOException;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;
import org.observertc.webrtc.common.UUIDAdapter;
import org.observertc.webrtc.observer.dto.v20200114.PeerConnectionSample;
import org.observertc.webrtc.observer.samples.ObservedPCS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service should be UUId, because currently mysql stores it as
 * binary and with that type the search is fast for activestreams. thats why.
 */
@ServerWebSocket("/{serviceUUID}/{mediaUnitID}/v20200114/json")
public class WebRTCStatsWebsocketServer20200114Json {

	private static final Logger logger = LoggerFactory.getLogger(WebRTCStatsWebsocketServer20200114Json.class);
	private final ObservedPCSSink observedPCSSink;
	private final ObserverConfig config;
	private final ObjectReader objectReader;
//	private final IDSLContextProvider contextProvider;

//	public DemoWebsocketServer(ObserverRepository observerRepository, DemoSink sink) {
//		this.observerRepository = observerRepository;
//		this.sink = sink;
//	}

	public WebRTCStatsWebsocketServer20200114Json(
			ObserverConfig config,
			ObjectMapper objectMapper,
			ObservedPCSSink observedPCSSink) {
		this.observedPCSSink = observedPCSSink;
		this.config = config;
		this.objectReader = objectMapper.reader();
	}


	@OnOpen
	public void onOpen(UUID serviceUUID, String mediaUnitID, WebSocketSession session) {

	}

	@OnClose
	public void onClose(
			String serviceUUID,
			String mediaUnitID,
			WebSocketSession session) {
	}

	@OnMessage
	public void onMessage(
			String serviceUUID,
			String mediaUnitID,
			String message,
			WebSocketSession session) {
		PeerConnectionSample sample;
		try {
			sample = this.objectReader.readValue(message, PeerConnectionSample.class);
		} catch (IOException e) {
			logger.warn("Parse error", e);
			return;
		}
		Optional<UUID> serviceUUIDHolder = UUIDAdapter.tryParse(serviceUUID);
		if (!serviceUUIDHolder.isPresent()) {
			logger.error("Invalid Service UUID {}", serviceUUID);
			return;
		}

		if (sample.peerConnectionId == null) {
			logger.warn("Sample is dropped due to null peerconnection uuid");
			return;
		}
		Optional<UUID> peerConnectionUUIDHolder = UUIDAdapter.tryParse(sample.peerConnectionId);

		if (!peerConnectionUUIDHolder.isPresent()) {
			logger.error("PC UUID is not parsable", sample.peerConnectionId);
			return;
		}
		UUID peerConnectionUUID = peerConnectionUUIDHolder.get();
		String timeZoneID = this.getSampleTimeZoneID(sample);
		Long timestamp = this.getTimestamp(sample);

		ObservedPCS observedPCS = ObservedPCS.of(
				serviceUUIDHolder.get(),
				mediaUnitID,
				peerConnectionUUID,
				sample,
				timeZoneID,
				"serviceName",
				timestamp
		);

		try {
			// TODO: avro!
			this.observedPCSSink.sendObservedPCS(peerConnectionUUID, observedPCS);
		} catch (Exception ex) {
			logger.error("Error happened for kafka push ", ex);
		}
	}

	private Long getTimestamp(PeerConnectionSample sample) {
		if (sample.timestamp != null) {
			return sample.timestamp;
		}
		Long result = Instant.now().toEpochMilli();
		return result;
	}


	private String getSampleTimeZoneID(PeerConnectionSample sample) {
		if (sample.timeZoneOffsetInMinute == null) {
			return null;
		}
		Integer hours;
		try {
			Long timeZoneInHours = sample.timeZoneOffsetInMinute / 60;
			hours = timeZoneInHours.intValue();
		} catch (Exception ex) {
			logger.warn("Cannot parse timeZoneOffsetInMinute {}", sample.timeZoneOffsetInMinute);
			return ZoneOffset.of("GMT").getId();
		}

		if (hours == 0) {
			return ZoneOffset.of("GMT").getId();
		}
//		char sign = 0 < hours ? '+' : '';
		String offsetID;
		if (0 < hours) {
			if (9 < hours) {
				offsetID = String.format("+%d:00", hours);
			} else {
				offsetID = String.format("+%02d:00", hours);
			}
		} else {
			hours *= -1;
			if (9 < hours) {
				offsetID = String.format("-%d:00", hours);
			} else {
				offsetID = String.format("-%02d:00", hours);
			}
		}

//		logger.info("Something 3: {}", offsetID);
		ZoneOffset zoneOffset;
		try {
			zoneOffset = ZoneOffset.of(offsetID);
		} catch (Exception ex) {
			logger.warn("Exception occured by converting " + sample.timeZoneOffsetInMinute + " with offset string " + offsetID + " to" +
							" ZoneOffset",
					ex);
			return null;
		}

		if (zoneOffset == null) {
			return null;
		}
		return zoneOffset.getId();
	}

}
