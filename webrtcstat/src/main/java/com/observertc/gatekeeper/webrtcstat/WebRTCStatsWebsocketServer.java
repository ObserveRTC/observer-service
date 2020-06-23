package com.observertc.gatekeeper.webrtcstat;

import com.observertc.gatekeeper.webrtcstat.dto.ObserverDTO;
import com.observertc.gatekeeper.webrtcstat.dto.webextrapp.Converter;
import com.observertc.gatekeeper.webrtcstat.dto.webextrapp.ObserveRTCCIceStats;
import com.observertc.gatekeeper.webrtcstat.dto.webextrapp.PeerConnectionSample;
import com.observertc.gatekeeper.webrtcstat.dto.webextrapp.RTCStats;
import com.observertc.gatekeeper.webrtcstat.dto.webextrapp.RTCStatsType;
import com.observertc.gatekeeper.webrtcstat.model.SSRCMapEntry;
import com.observertc.gatekeeper.webrtcstat.repositories.ObserverRepository;
import com.observertc.gatekeeper.webrtcstat.samples.ObserveRTCCIceStatsSample;
import com.observertc.gatekeeper.webrtcstat.samples.ObserveRTCMediaStreamStatsSample;
import io.micronaut.websocket.WebSocketSession;
import io.micronaut.websocket.annotation.OnClose;
import io.micronaut.websocket.annotation.OnMessage;
import io.micronaut.websocket.annotation.OnOpen;
import io.micronaut.websocket.annotation.ServerWebSocket;
import java.io.IOException;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ServerWebSocket("/ws/{observerUUID}")
public class WebRTCStatsWebsocketServer {
	private static final Logger logger = LoggerFactory.getLogger(WebRTCStatsWebsocketServer.class);
	private final ObserverRepository observerRepository;
	private final PeerConnectionSampleKafkaSinks kafkaSinks;
//	private final IDSLContextProvider contextProvider;

//	public DemoWebsocketServer(ObserverRepository observerRepository, DemoSink sink) {
//		this.observerRepository = observerRepository;
//		this.sink = sink;
//	}

	public WebRTCStatsWebsocketServer(
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
		if (peerConnectionUUIDHolder.isEmpty()) {
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
			RTCStatsType type = rtcStats.getType();
			switch (type) {
				case INBOUND_RTP:
				case OUTBOUND_RTP:
					SSRCMapEntry ssrcMapEntry = new SSRCMapEntry();
					Long SSRC = rtcStats.getSsrc().longValue();
					ssrcMapEntry.SSRC = SSRC;
					ssrcMapEntry.observerUUID = observerUUID;
					ssrcMapEntry.peerConnectionUUID = peerConnectionUUID;
					kafkaSinks.sendSSRCMapEntries(peerConnectionUUID, ssrcMapEntry);
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
