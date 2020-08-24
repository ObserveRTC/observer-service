package org.observertc.webrtc.observer;

import io.micronaut.websocket.WebSocketSession;
import io.micronaut.websocket.annotation.OnClose;
import io.micronaut.websocket.annotation.OnMessage;
import io.micronaut.websocket.annotation.OnOpen;
import io.micronaut.websocket.annotation.ServerWebSocket;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Iterator;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import javax.inject.Inject;
import org.observertc.webrtc.common.UUIDAdapter;
import org.observertc.webrtc.observer.dto.ObserverDTO;
import org.observertc.webrtc.observer.dto.webextrapp.CandidatePair;
import org.observertc.webrtc.observer.dto.webextrapp.Converter;
import org.observertc.webrtc.observer.dto.webextrapp.LocalCandidate;
import org.observertc.webrtc.observer.dto.webextrapp.PeerConnectionSample;
import org.observertc.webrtc.observer.dto.webextrapp.RTCStats;
import org.observertc.webrtc.observer.dto.webextrapp.RemoteCandidate;
import org.observertc.webrtc.observer.dto.webextrapp.Timestamp;
import org.observertc.webrtc.observer.evaluators.IteratorProvider;
import org.observertc.webrtc.observer.repositories.ObserverRepository;
import org.observertc.webrtc.observer.repositories.PeerConnectionsRepository;
import org.observertc.webrtc.observer.samples.WebExtrAppSample;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ServerWebSocket("/ws/{observerUUID}")
public class WebExtrAppWebsocketServer {

	private static final Logger logger = LoggerFactory.getLogger(WebExtrAppWebsocketServer.class);
	private final ObserverRepository observerRepository;
	private final KafkaSinks kafkaSinks;
	private final ObserverConfig config;
	private final PeerConnectionsRepository repository;
	private final Function<PeerConnectionSample, LocalDateTime> timestampExtractor;

	@Inject
	ObserverDateTime observerDateTime;
//	private final IDSLContextProvider contextProvider;

//	public DemoWebsocketServer(ObserverRepository observerRepository, DemoSink sink) {
//		this.observerRepository = observerRepository;
//		this.sink = sink;
//	}

	public WebExtrAppWebsocketServer(
			ObserverConfig config,
			ObserverRepository observerRepository,
			PeerConnectionsRepository repository,
			KafkaSinks kafkaSinks) {
		this.observerRepository = observerRepository;
		this.kafkaSinks = kafkaSinks;
		this.config = config;
		this.repository = repository;
		if (this.config.useClientTimestamps) {
			this.timestampExtractor = sample -> {
				LocalDateTime result = extractTs(sample);
				if (result == null) {
					logger.warn("The provided peer connection id {} does not have a timestamp, thus the server is giving one",
							sample.getPeerConnectionID());
					result = this.observerDateTime.now();
				}
				return result;
			};
		} else {
			this.timestampExtractor = sample -> observerDateTime.now();
		}
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

		String sampleTimeZoneID = this.getSampleTimeZoneID(sample);
		LocalDateTime timestamp = this.timestampExtractor.apply(sample);

		Optional<UUID> peerConnectionUUIDHolder = UUIDAdapter.tryParse(sample.getPeerConnectionID());
		if (!peerConnectionUUIDHolder.isPresent()) {
			logger.error("The provided peer connection id {} from {} cannot be parsed as UUID", sample.getPeerConnectionID(), observerUUID);
			return;
		}
		UUID peerConnectionUUID = peerConnectionUUIDHolder.get();

		WebExtrAppSample webExtrAppSample = WebExtrAppSample.of(
				observerUUID,
				peerConnectionUUID,
				sample,
				sampleTimeZoneID,
				timestamp);

		this.kafkaSinks.sendWebExtrAppSamples(peerConnectionUUID, webExtrAppSample);
	}


	private String getSampleTimeZoneID(PeerConnectionSample sample) {
		if (sample.getTimeZoneOffsetInMinute() == null) {
			return null;
		}
		Integer hours;
		try {
			Double something = sample.getTimeZoneOffsetInMinute() / 60;
//			logger.info("Something 1: {}", something);
			hours = something.intValue();
//			logger.info("Something 2: {}", hours);
		} catch (Exception ex) {
			logger.warn("Cannot parse timeZoneOffsetInMinute {}", sample.getTimeZoneOffsetInMinute());
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

	private static final ZoneId CLIENT_ZONE_ID = ZoneId.of("GMT");

	private LocalDateTime convertGMTEpoch(long epoch) {
		Instant epochInstant = Instant.ofEpochMilli(epoch);
		LocalDateTime date = LocalDateTime.ofInstant(epochInstant, CLIENT_ZONE_ID);
		return date;
	}

	private LocalDateTime extractTs(PeerConnectionSample sample) {
		Timestamp timestamp = null;
		Iterator<RTCStats> it = IteratorProvider.makeRTCStatsIt(sample);
		for (; it.hasNext(); ) {
			RTCStats stat = it.next();
			if (stat.getTimestamp() != null) {
				timestamp = stat.getTimestamp();
				break;
			}
		}
		if (timestamp != null) {
			Double value = null;
			if (0 < timestamp.doubleValue) {
				value = timestamp.doubleValue;
			} else {
				try {
					value = Double.parseDouble(timestamp.stringValue);
				} catch (Exception ex) {
					logger.warn("Cannot parse timestamp {}", timestamp.stringValue);
				}
			}
			if (value != null) {
				long epoch = value.longValue();
				return this.convertGMTEpoch(epoch);
			}
		}
		if (sample.getIceStats() == null) {
			return null;
		}
		LocalCandidate[] localCandidates = sample.getIceStats().getLocalCandidates();
		for (int i = 0; i < localCandidates.length; ++i) {
			LocalCandidate candidate = localCandidates[i];
			if (0.0 < candidate.getTimestamp()) {
				long epoch = (long) candidate.getTimestamp();
				return this.convertGMTEpoch(epoch);
			}
		}

		RemoteCandidate[] remoteCandidates = sample.getIceStats().getRemoteCandidates();
		for (int i = 0; i < remoteCandidates.length; ++i) {
			RemoteCandidate candidate = remoteCandidates[i];
			if (0.0 < candidate.getTimestamp()) {
				long epoch = (long) candidate.getTimestamp();
				return this.convertGMTEpoch(epoch);
			}
		}

		CandidatePair[] candidatePairs = sample.getIceStats().getIceCandidatePair();
		for (int i = 0; i < candidatePairs.length; ++i) {
			CandidatePair candidate = candidatePairs[i];
			if (0.0 < candidate.getTimestamp()) {
				long epoch = (long) candidate.getTimestamp();
				return this.convertGMTEpoch(epoch);
			}
		}
		return null;
	}
}
