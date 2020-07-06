package org.observertc.webrtc.observer.service;

import io.micronaut.websocket.WebSocketSession;
import io.micronaut.websocket.annotation.OnClose;
import io.micronaut.websocket.annotation.OnMessage;
import io.micronaut.websocket.annotation.OnOpen;
import io.micronaut.websocket.annotation.ServerWebSocket;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import org.observertc.webrtc.observer.service.dto.ObserverDTO;
import org.observertc.webrtc.observer.service.dto.webextrapp.Converter;
import org.observertc.webrtc.observer.service.dto.webextrapp.ObserveRTCCIceStats;
import org.observertc.webrtc.observer.service.dto.webextrapp.PeerConnectionSample;
import org.observertc.webrtc.observer.service.dto.webextrapp.RTCStats;
import org.observertc.webrtc.observer.service.dto.webextrapp.RTCStatsType;
import org.observertc.webrtc.observer.service.repositories.ObserverRepository;
import org.observertc.webrtc.observer.service.samples.ObserveRTCCIceStatsSample;
import org.observertc.webrtc.observer.service.samples.ObserveRTCMediaStreamStatsSample;
import org.observertc.webrtc.observer.service.samples.ObserverSSRCPeerConnectionSample;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ServerWebSocket("/ws/{observerUUID}")
public class WebExtrAppWebsocketServer {
	private static final Logger logger = LoggerFactory.getLogger(WebExtrAppWebsocketServer.class);
	private final ObserverRepository observerRepository;
	private final PeerConnectionSampleKafkaSinks kafkaSinks;
//	private final IDSLContextProvider contextProvider;

//	public DemoWebsocketServer(ObserverRepository observerRepository, DemoSink sink) {
//		this.observerRepository = observerRepository;
//		this.sink = sink;
//	}

	public WebExtrAppWebsocketServer(
			ObserverRepository observerRepository,
			PeerConnectionSampleKafkaSinks kafkaSinks) {
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
		} catch (IOException e) {
			e.printStackTrace();
			logger.error("The provided message cannot be parsed for " + observerUUID, e);
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
		for (int i = 0; i < mediaStreamStats.length; ++i) {
			RTCStats rtcStats = mediaStreamStats[i];
			// TODO: ObjectPool?
			ObserveRTCMediaStreamStatsSample sample = new ObserveRTCMediaStreamStatsSample();
			sample.observerUUID = observerUUID;
			if (rtcStats == null) {
				logger.warn("Null RTCStats is provided for {}, pcUUID: {}", observerUUID, peerConnectionUUID);
				continue;
			}
			sample.rtcStats = rtcStats;
			sample.sampled = LocalDateTime.now();
			RTCStatsType type = rtcStats.getType();
			switch (type) {
				case INBOUND_RTP:
				case OUTBOUND_RTP:
					ObserverSSRCPeerConnectionSample observerSSRCPeerConnectionSample = new ObserverSSRCPeerConnectionSample();
					Long SSRC = rtcStats.getSsrc().longValue();
					observerSSRCPeerConnectionSample.SSRC = SSRC;
					observerSSRCPeerConnectionSample.observerUUID = observerUUID;
					observerSSRCPeerConnectionSample.peerConnectionUUID = peerConnectionUUID;
					observerSSRCPeerConnectionSample.timestamp = LocalDateTime.now();
					kafkaSinks.sendObserverSSRCPeerConnectionSamples(peerConnectionUUID, observerSSRCPeerConnectionSample);
					break;
			}
			this.kafkaSinks.sendObserveRTCMediaStreamStatsSamples(peerConnectionUUID, sample);
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
