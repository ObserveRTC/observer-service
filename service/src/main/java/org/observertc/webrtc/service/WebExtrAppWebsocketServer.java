package org.observertc.webrtc.service;

import io.micronaut.websocket.WebSocketSession;
import io.micronaut.websocket.annotation.OnClose;
import io.micronaut.websocket.annotation.OnMessage;
import io.micronaut.websocket.annotation.OnOpen;
import io.micronaut.websocket.annotation.ServerWebSocket;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;
import javax.inject.Inject;
import org.observertc.webrtc.common.UUIDAdapter;
import org.observertc.webrtc.service.dto.ObserverDTO;
import org.observertc.webrtc.service.dto.webextrapp.Converter;
import org.observertc.webrtc.service.dto.webextrapp.PeerConnectionSample;
import org.observertc.webrtc.service.repositories.ObserverRepository;
import org.observertc.webrtc.service.samples.WebExtrAppSample;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ServerWebSocket("/ws/{observerUUID}")
public class WebExtrAppWebsocketServer {
	private static final Logger logger = LoggerFactory.getLogger(WebExtrAppWebsocketServer.class);
	private final ObserverRepository observerRepository;
	private final ObserverKafkaSinks kafkaSinks;
	private final ObserverConfig config;

	@Inject
	ObserverTimeZoneId observerTimeZoneId;
//	private final IDSLContextProvider contextProvider;

//	public DemoWebsocketServer(ObserverRepository observerRepository, DemoSink sink) {
//		this.observerRepository = observerRepository;
//		this.sink = sink;
//	}

	public WebExtrAppWebsocketServer(
			ObserverConfig config,
			ObserverRepository observerRepository,
			ObserverKafkaSinks kafkaSinks) {
		this.observerRepository = observerRepository;
		this.kafkaSinks = kafkaSinks;
		this.config = config;
	}

	@OnOpen
	public void onOpen(UUID observerUUID, WebSocketSession session) {
		Optional<ObserverDTO> observerDTO = observerRepository.findById(observerUUID);
		if (!observerDTO.isPresent()) {
			System.out.println("observer has not been found");
			session.close();
			return;
		}
	}

	@OnClose
	public void onClose(
			UUID observerUUID,
			WebSocketSession session) {
	}

	@OnMessage
	public void onMessage(
			UUID observerUUID,
			String message,
//			PeerConnectionSample sample,
			WebSocketSession session) {
//		System.out.println(content);
		PeerConnectionSample sample;
		try {
			sample = Converter.PeerConnectionSampleFromJsonString(message);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("The provided message cannot be parsed for " + observerUUID, e);
			return;
		}

		if (sample.getPeerConnectionID() == null) {
			logger.warn("Sample is dropped due to null peerconnectionid");
			return;
		}
		LocalDateTime timestamp = LocalDateTime.now(observerTimeZoneId.getZoneId());
		String sampleTimeZoneID = this.getSampleTimeZoneID(sample);
		Optional<UUID> peerConnectionUUIDHolder = UUIDAdapter.tryParse(sample.getPeerConnectionID());
		if (!peerConnectionUUIDHolder.isPresent()) {
			logger.error("The provided peer connection id {} from {} cannot be parsed as UUID", sample.getPeerConnectionID(), observerUUID);
			return;
		}
		if (sample.getBrowserId() == null) {
			String generatedBrowserId = new String(UUIDAdapter.toBytes(peerConnectionUUIDHolder.get()), StandardCharsets.UTF_8);
			sample.setBrowserId(generatedBrowserId);
		}

		UUID peerConnectionUUID = peerConnectionUUIDHolder.get();

		WebExtrAppSample webExtrAppSample = WebExtrAppSample.of(
				observerUUID,
				peerConnectionUUID, sample,
				sampleTimeZoneID,
				timestamp);

		this.kafkaSinks.sendWebExtrAppSample(peerConnectionUUID, webExtrAppSample);

	}


	private String getSampleTimeZoneID(PeerConnectionSample sample) {
		if (sample.getTimeZoneOffsetInMinute() == null) {
			return null;
		}
		Integer hours;
		try {
			Double something = sample.getTimeZoneOffsetInMinute() / 60;
			logger.info("Something 1: {}", something);
			hours = something.intValue();
			logger.info("Something 2: {}", hours);
		} catch (Exception ex) {
			logger.warn("Cannot parse timeZoneOffsetInMinute {}", sample.getTimeZoneOffsetInMinute());
			return ZoneOffset.of("GMT").getId();
		}

		if (hours == 0) {
			return ZoneOffset.of("GMT").getId();
		}
		char sign = 0 < hours ? '+' : '-';
		String offsetID = String.format("%c%02d:00", sign, hours);
		logger.info("Something 3: {}", offsetID);
		ZoneOffset zoneOffset;
		try {
			zoneOffset = ZoneOffset.of(offsetID);
		} catch (Exception ex) {
			logger.warn("Exception occured by converting " + sample.getTimeZoneOffsetInMinute() + " with offset string " + offsetID + " to" +
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
