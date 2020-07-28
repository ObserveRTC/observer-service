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
import org.observertc.webrtc.common.UUIDAdapter;
import org.observertc.webrtc.service.dto.MediaStreamSampleTransformer;
import org.observertc.webrtc.service.dto.ObserverDTO;
import org.observertc.webrtc.service.dto.webextrapp.Converter;
import org.observertc.webrtc.service.dto.webextrapp.ObserveRTCCIceStats;
import org.observertc.webrtc.service.dto.webextrapp.PeerConnectionSample;
import org.observertc.webrtc.service.dto.webextrapp.RTCStats;
import org.observertc.webrtc.service.repositories.ObserverRepository;
import org.observertc.webrtc.service.samples.MediaStreamSample;
import org.observertc.webrtc.service.samples.ObserveRTCCIceStatsSample;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ServerWebSocket("/ws/{observerUUID}")
public class WebExtrAppWebsocketServer {
	private static final Logger logger = LoggerFactory.getLogger(WebExtrAppWebsocketServer.class);
	private final ObserverRepository observerRepository;
	private final WebRTCKafkaSinks kafkaSinks;
	private final MediaStreamSampleTransformer<Void> mediaStreamSampleSender;

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
		this.mediaStreamSampleSender = this.makeMediaSampleSender();
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
			this.sendRTCStats(observerUUID, peerConnectionUUID, sample.getReceiverStats());
		}

		if (sample.getSenderStats() != null) {
			this.sendRTCStats(observerUUID, peerConnectionUUID, sample.getSenderStats());
		}

		if (sample.getIceStats() != null) {
			this.sendObserveRTCCIceStats(observerUUID, peerConnectionUUID, sample.getIceStats());
		}
	}

	private void sendRTCStats(UUID observerUUID, UUID peerConnectionUUID, RTCStats[] mediaStreamStats) {
		// TODO: here consider the timestamp extraction from server or not
//		LocalDateTime timestamp = LocalDateTime.now(ZoneOffset.UTC);
		LocalDateTime timestamp = LocalDateTime.now(applicationTimeZoneId.getZoneId());
		for (int i = 0; i < mediaStreamStats.length; ++i) {
			RTCStats rtcStats = mediaStreamStats[i];
			MediaStreamSample sample = MediaStreamSample.of(observerUUID, peerConnectionUUID, rtcStats, timestamp);
			this.mediaStreamSampleSender.transform(sample);
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

	private MediaStreamSampleTransformer<Void> makeMediaSampleSender() {
		return new MediaStreamSampleTransformer() {
			@Override
			public Void processInboundRTP(MediaStreamSample sample) {
				kafkaSinks.sendObserveRTCMediaStreamStatsSamples(sample.peerConnectionUUID, sample);
				return null;
			}

			@Override
			public Void processOutboundRTP(MediaStreamSample sample) {
				kafkaSinks.sendObserveRTCMediaStreamStatsSamples(sample.peerConnectionUUID, sample);
				return null;
			}

			@Override
			public Void processRemoteInboundRTP(MediaStreamSample sample) {
				kafkaSinks.sendObserveRTCMediaStreamStatsSamples(sample.peerConnectionUUID, sample);
				return null;
			}

			@Override
			public Object processTrack(MediaStreamSample sample) {
				return null;
			}

			@Override
			public Object processMediaSource(MediaStreamSample sample) {
				return null;
			}

			@Override
			public Object processCandidatePair(MediaStreamSample sample) {
				return null;
			}

			@Override
			public Void unprocessable(MediaStreamSample sample) {
				UUID observerUUID = sample != null ? sample.observerUUID : null;
				UUID peerConnectionUUID = sample != null ? sample.peerConnectionUUID : null;
				logger.warn("Cannot Process MediaStreamSample for observerUUID{}, pcUUID: {}", observerUUID,
						peerConnectionUUID);
				return null;
			}
		};
	}

}
