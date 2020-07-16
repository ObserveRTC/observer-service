package org.observertc.webrtc.service;

import io.micronaut.websocket.WebSocketSession;
import io.micronaut.websocket.annotation.OnClose;
import io.micronaut.websocket.annotation.OnMessage;
import io.micronaut.websocket.annotation.OnOpen;
import io.micronaut.websocket.annotation.ServerWebSocket;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import javax.inject.Inject;
import org.observertc.webrtc.service.dto.ObserverDTO;
import org.observertc.webrtc.service.dto.webextrapp.Converter;
import org.observertc.webrtc.service.dto.webextrapp.ObserveRTCCIceStats;
import org.observertc.webrtc.service.dto.webextrapp.PeerConnectionSample;
import org.observertc.webrtc.service.dto.webextrapp.RTCStats;
import org.observertc.webrtc.service.dto.webextrapp.RTCStatsType;
import org.observertc.webrtc.service.repositories.ObserverRepository;
import org.observertc.webrtc.service.samples.ObserveRTCCIceStatsSample;
import org.observertc.webrtc.service.samples.ObserveRTCMediaStreamStatsSample;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ServerWebSocket("/ws/{observerUUID}")
public class WebExtrAppWebsocketServer {
	private static final Logger logger = LoggerFactory.getLogger(WebExtrAppWebsocketServer.class);
	private final ObserverRepository observerRepository;
	private final WebRTCKafkaSinks kafkaSinks;

	@Inject
	ApplicationTimeZoneId applicationTimeZoneId;
//	private final IDSLContextProvider contextProvider;

//	public DemoWebsocketServer(ObserverRepository observerRepository, DemoSink sink) {
//		this.observerRepository = observerRepository;
//		this.sink = sink;
//	}

	public WebExtrAppWebsocketServer(
			ObserverRepository observerRepository,
			WebRTCKafkaSinks kafkaSinks) {
		this.observerRepository = observerRepository;
		this.kafkaSinks = kafkaSinks;
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

	private Random random = new Random();
	//String array
	String[] strings = {"First", "Second", "Third", "Forth", "Fifth", "Sixth", "Seventh", "Eight", "Ninth", "Tenth"};

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
		Optional<UUID> peerConnectionUUIDHolder = UUIDAdapter.tryParse(sample.getPeerConnectionID());
		if (!peerConnectionUUIDHolder.isPresent()) {
			logger.error("The provided peer connection id {} from {} cannot be parsed as UUID", sample.getPeerConnectionID(), observerUUID);
			return;
		}
		UUID peerConnectionUUID = peerConnectionUUIDHolder.get();
		if (sample.getReceiverStats() != null) {
			this.sendObserveRTCMediaStreamStats(observerUUID, peerConnectionUUID, sample.getReceiverStats());
		}

		if (sample.getSenderStats() != null) {
			this.sendObserveRTCMediaStreamStats(observerUUID, peerConnectionUUID, sample.getSenderStats());
		}

		if (sample.getIceStats() != null) {
			this.sendObserveRTCCIceStats(observerUUID, peerConnectionUUID, sample.getIceStats());
		}
	}

	private void sendObserveRTCMediaStreamStats(UUID observerUUID, UUID peerConnectionUUID, RTCStats[] mediaStreamStats) {
		// TODO: here consider the timestamp extraction from server or not
//		LocalDateTime timestamp = LocalDateTime.now(ZoneOffset.UTC);
		LocalDateTime timestamp = LocalDateTime.now(applicationTimeZoneId.getZoneId());
		for (int i = 0; i < mediaStreamStats.length; ++i) {
			RTCStats rtcStats = mediaStreamStats[i];
			ObserveRTCMediaStreamStatsSample sample = new ObserveRTCMediaStreamStatsSample();
			sample.observerUUID = observerUUID;
			if (rtcStats == null) {
				logger.warn("Null RTCStats is provided for {}, pcUUID: {}", observerUUID, peerConnectionUUID);
				continue;
			}
			sample.rtcStats = rtcStats;
			sample.sampled = timestamp;
			RTCStatsType type = rtcStats.getType();
			switch (type) {
				case INBOUND_RTP:
				case OUTBOUND_RTP:
				case REMOTE_INBOUND_RTP:
					this.kafkaSinks.sendObserveRTCMediaStreamStatsSamples(peerConnectionUUID, sample);
					break;
			}
		}
	}

	private void sendObserveRTCCIceStats(UUID observerUUID, UUID peerConnectionUUID, ObserveRTCCIceStats observeRTCCIceStats) {
		ObserveRTCCIceStatsSample sample = new ObserveRTCCIceStatsSample();
		sample.observerUUID = observerUUID;
		sample.iceStats = observeRTCCIceStats;
		this.kafkaSinks.sendObserveRTCICEStatsSamples(peerConnectionUUID, sample);
	}

	@OnClose
	public void onClose(
			UUID observerUUID,
			WebSocketSession session) {
	}


}
