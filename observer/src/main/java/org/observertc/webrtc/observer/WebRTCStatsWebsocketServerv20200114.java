/*
 * Copyright  2020 Balazs Kreith
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
import org.observertc.webrtc.observer.evaluators.MediaStreamUpdateRouter;
import org.observertc.webrtc.observer.micrometer.CountedLogMonitor;
import org.observertc.webrtc.observer.micrometer.ObserverMetricsReporter;
import org.observertc.webrtc.observer.samples.ObservedPCS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;

/**
 * Service should be UUId, because currently mysql stores it as
 * binary and with that type the search is fast for activestreams. thats why.
 */
@ServerWebSocket("/{serviceUUID}/{mediaUnitID}/v20200114/json")
public class WebRTCStatsWebsocketServerv20200114 {
	private static final String PC_SAMPLE_VERSION = "20200114";

	private static final Logger logger = LoggerFactory.getLogger(WebRTCStatsWebsocketServerv20200114.class);
	//	private final ObservedPCSSink observedPCSSink;
	private final ObserverConfig config;
	private final ObjectReader objectReader;
	private final ObservedPCSForwarder observedPCSForwarder;
	private final MediaStreamUpdateRouter mediaStreamUpdateRouter;
	private final ObserverMetricsReporter observerMetricsReporter;
	private final CountedLogMonitor countedLogMonitor;


//	private final IDSLContextProvider contextProvider;

//	public DemoWebsocketServer(ObserverRepository observerRepository, DemoSink sink) {
//		this.observerRepository = observerRepository;
//		this.sink = sink;
//	}

	public WebRTCStatsWebsocketServerv20200114(
			ObserverConfig config,
			ObjectMapper objectMapper,
			MediaStreamUpdateRouter mediaStreamUpdateRouter,
			ObservedPCSForwarder observedPCSForwarder,
			ObserverMetricsReporter observerMetricsReporter
//			ObservedPCSSink observedPCSSink
	) {
//		this.observedPCSSink = observedPCSSink;
		this.observedPCSForwarder = observedPCSForwarder;
		this.config = config;
		this.objectReader = objectMapper.reader();
		this.mediaStreamUpdateRouter = mediaStreamUpdateRouter;
		this.observerMetricsReporter = observerMetricsReporter;
		this.countedLogMonitor = observerMetricsReporter.makeCountedLogMonitor(logger)
				.withCommonTags("version", "20200114")
				.withRequiredTags("serviceName", "serviceUUID", "mediaUnitId")
				.withDefaultMetricName("websocket");
	}


	@OnOpen
	public void onOpen(String serviceUUID, String mediaUnitID, WebSocketSession session) {
		this.observerMetricsReporter.incrementOpenedWebsocketConnectionsCounter(serviceUUID, mediaUnitID);
	}

	@OnClose
	public void onClose(
			String serviceUUID,
			String mediaUnitID,
			WebSocketSession session) {
		this.observerMetricsReporter.incrementClosedWebsocketConnectionsCounter(serviceUUID, mediaUnitID);
	}

	@OnMessage(maxPayloadLength = 1000000) // 1MB
	public void onMessage(
			String serviceUUID,
			String mediaUnitID,
			String message,
			WebSocketSession session) {
		PeerConnectionSample sample;
		try {
			sample = this.objectReader.readValue(message, PeerConnectionSample.class);
		} catch (IOException e) {
			this.countedLogMonitor
					.makeEntry("serviceName", serviceUUID, mediaUnitID)
					.withLogLevel(Level.WARN)
					.withCategory("invalid.message")
					.withException(e)
					.log();
			return;
		}
		Optional<UUID> serviceUUIDHolder = UUIDAdapter.tryParse(serviceUUID);
		if (!serviceUUIDHolder.isPresent()) {
			this.countedLogMonitor
					.makeEntry("serviceName", serviceUUID, mediaUnitID)
					.withMessage(String.format("Invalid service UUID %s", serviceUUID))
					.withLogLevel(Level.WARN)
					.withCategory("invalid.serviceUUID")
					.log();
			return;
		}

		if (sample.peerConnectionId == null) {
			this.countedLogMonitor
					.makeEntry("serviceName", serviceUUID, mediaUnitID)
					.withLogLevel(Level.WARN)
					.withCategory("invalid.pcUUID")
					.withMessage("pcUUID is null")
					.log();
			return;
		}
		Optional<UUID> peerConnectionUUIDHolder = UUIDAdapter.tryParse(sample.peerConnectionId);

		if (!peerConnectionUUIDHolder.isPresent()) {
			this.countedLogMonitor
					.makeEntry("serviceName", serviceUUID, mediaUnitID)
					.withLogLevel(Level.WARN)
					.withCategory("invalid.pcUUID")
					.withMessage(String.format("pcUUID is invalid %s", sample.peerConnectionId))
					.log();
			return;
		}
		UUID peerConnectionUUID = peerConnectionUUIDHolder.get();
		String timeZoneID = this.getSampleTimeZoneID("serviceName", serviceUUID, mediaUnitID, sample);
		Long timestamp = this.getTimestamp(sample);

		ObservedPCS observedPCS = ObservedPCS.of(
				serviceUUIDHolder.get(),
				mediaUnitID,
				peerConnectionUUID,
				sample,
				timeZoneID,
				"serviceName",
				"customProvided",
				timestamp
		);

		try {
			this.observedPCSForwarder.forward(observedPCS);
		} catch (Exception ex) {
			this.countedLogMonitor
					.makeEntry("serviceName", serviceUUID, mediaUnitID)
					.withLogLevel(Level.ERROR)
					.withCategory("observedPCs.forwarding")
					.withException(ex)
					.log();
		}

		try {
			this.mediaStreamUpdateRouter.add(observedPCS);
		} catch (Exception ex) {
			this.countedLogMonitor
					.makeEntry("serviceName", serviceUUID, mediaUnitID)
					.withLogLevel(Level.ERROR)
					.withCategory("observedPCs.evaluation")
					.withException(ex)
					.log();
		}


	}

	private Long getTimestamp(PeerConnectionSample sample) {
		if (sample.timestamp != null) {
			return sample.timestamp;
		}
		Long result = Instant.now().toEpochMilli();
		return result;
	}


	private String getSampleTimeZoneID(String serviceName,
									   String serviceUUID,
									   String mediaUnitID,
									   PeerConnectionSample sample) {
		if (sample.timeZoneOffsetInMinute == null) {
			return null;
		}
		Integer hours;
		try {
			Long timeZoneInHours = sample.timeZoneOffsetInMinute / 60;
			hours = timeZoneInHours.intValue();
		} catch (Exception ex) {
			this.countedLogMonitor
					.makeEntry(serviceName, serviceUUID, mediaUnitID)
					.withCategory("parsingTimezoneOffset")
					.withLogLevel(Level.WARN)
					.withMessage(String.format("Cannot parse timeZoneOffsetInMinute %d", sample.timeZoneOffsetInMinute))
					.withException(ex)
					.log();
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
			this.countedLogMonitor
					.makeEntry(serviceName, serviceUUID, mediaUnitID)
					.withCategory("parsingZoneOffset")
					.withLogLevel(Level.WARN)
					.withMessage(String.format("Cannot parse zoneoffset %s", offsetID))
					.withException(ex)
					.log();
			return null;
		}

		if (zoneOffset == null) {
			return null;
		}
		return zoneOffset.getId();
	}
}
