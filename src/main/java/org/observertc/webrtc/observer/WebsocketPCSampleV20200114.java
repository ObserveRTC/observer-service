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
import io.micrometer.core.annotation.Counted;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import io.micronaut.websocket.WebSocketSession;
import io.micronaut.websocket.annotation.OnClose;
import io.micronaut.websocket.annotation.OnMessage;
import io.micronaut.websocket.annotation.OnOpen;
import io.micronaut.websocket.annotation.ServerWebSocket;
import org.observertc.webrtc.observer.common.UUIDAdapter;
import org.observertc.webrtc.observer.dto.v20200114.PeerConnectionSample;
import org.observertc.webrtc.observer.evaluators.Pipeline;
import org.observertc.webrtc.observer.monitors.FlawMonitor;
import org.observertc.webrtc.observer.monitors.MonitorProvider;
import org.observertc.webrtc.observer.repositories.ServicesRepository;
import org.observertc.webrtc.observer.samples.ObservedPCS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;

import javax.inject.Inject;
import java.io.IOException;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

/**
 * Service should be UUId, because currently mysql stores it as
 * binary and with that type the search is fast for activestreams. thats why.
 */
@Secured(SecurityRule.IS_ANONYMOUS)
@ServerWebSocket("/{serviceUUIDStr}/{mediaUnitID}/v20200114/json")
public class WebsocketPCSampleV20200114 {
	private static final String PC_SAMPLE_VERSION = "20200114";

	private static final Logger logger = LoggerFactory.getLogger(WebsocketPCSampleV20200114.class);
	private final ObjectReader objectReader;
	private final FlawMonitor flawMonitor;
	private final ServicesRepository servicesRepository;

	@Inject
	Pipeline pipeline;

	public WebsocketPCSampleV20200114(
			ObjectMapper objectMapper,
			MonitorProvider monitorProvider,
			ServicesRepository servicesRepository
	) {
		this.objectReader = objectMapper.reader();
		this.servicesRepository = servicesRepository;
		this.flawMonitor = monitorProvider.makeFlawMonitorFor(this.getClass());
	}

	@Counted(
			value = "observer_pcsample_opened_websocket",
			description = "Counter to track the number of opened websocket for pcsamples per instance"
	)
	@OnOpen
	public void onOpen(String serviceUUIDStr, String mediaUnitID, WebSocketSession session) {

	}

	@Counted(
			value = "observer_pcsample_closed_websocket",
			description = "Counter to track the number of closed websocket for pcsamples per instance"
	)
	@OnClose
	public void onClose(
			String serviceUUIDStr,
			String mediaUnitID,
			WebSocketSession session) {
	}

	//	@OnMessage(maxPayloadLength = 1000000) // 1MB
	@OnMessage
	public void onMessage(
			String serviceUUIDStr,
			String mediaUnitID,
			byte[] messageBytes,
			WebSocketSession session) {
		Optional<UUID> serviceUUIDHolder = UUIDAdapter.tryParse(serviceUUIDStr);
		if (!serviceUUIDHolder.isPresent()) {
			this.flawMonitor.makeLogEntry()
					.withLogger(logger)
					.withLogLevel(Level.WARN)
					.withMessage("Invalid service uuid {}, from {}", serviceUUIDStr, mediaUnitID)
					.complete();
			return;
		}
		UUID serviceUUID = serviceUUIDHolder.get();
		String serviceName = this.servicesRepository.getServiceName(serviceUUID);
		PeerConnectionSample sample;
		try {
			sample = this.objectReader.readValue(messageBytes, PeerConnectionSample.class);
		} catch (IOException e) {
			String message = new String(messageBytes);
			this.flawMonitor.makeLogEntry()
					.withException(e)
					.withLogger(logger)
					.withLogLevel(Level.WARN)
					.withMessage("Invalid message ", message)
					.complete();
			int maxLogSize = 1000;
			for(int i = 0; i <= message.length() / maxLogSize; i++) {
				int start = i * maxLogSize;
				int end = (i+1) * maxLogSize;
				end = end > message.length() ? message.length() : end;
				logger.warn("Invalid message part {}, message snippet####{}####", message.substring(start, end));
			}
			return;
		} catch (Throwable t) {
			this.flawMonitor.makeLogEntry()
					.withException(t)
					.withLogger(logger)
					.withLogLevel(Level.WARN)
					.withMessage("There was an exception happened during interpretation")
					.complete();
			return;
		}

		String timeZoneID = this.getSampleTimeZoneID(serviceName, serviceUUID, mediaUnitID, sample);
		Long timestamp = this.getTimestamp(serviceName, serviceUUID, mediaUnitID, sample);
		if (sample.peerConnectionId == null) {
			if (sample.userMediaErrors != null) {
				ObservedPCS observedPCS = ObservedPCS.of(
						serviceUUIDHolder.get(),
						mediaUnitID,
						null,
						sample,
						timeZoneID,
						serviceName,
						null,
						timestamp
				);
				this.pipeline.input(observedPCS);
				return;
			}
			this.flawMonitor.makeLogEntry()
					.withLogger(logger)
					.withLogLevel(Level.WARN)
					.withMessage("pc uuid is null for ", sample)
					.complete();
			return;
		}
		Optional<UUID> peerConnectionUUIDHolder = UUIDAdapter.tryParse(sample.peerConnectionId);

		if (!peerConnectionUUIDHolder.isPresent()) {
			this.flawMonitor.makeLogEntry()
					.withLogger(logger)
					.withLogLevel(Level.WARN)
					.withMessage("invalid peer connection uuid for sample {} ", sample)
					.complete();
			return;
		}
		UUID peerConnectionUUID = peerConnectionUUIDHolder.get();

		ObservedPCS observedPCS = ObservedPCS.of(
				serviceUUIDHolder.get(),
				mediaUnitID,
				peerConnectionUUID,
				sample,
				timeZoneID,
				serviceName,
				null,
				timestamp
		);

		try {
			this.pipeline.input(observedPCS);
		} catch (Exception ex) {
			this.flawMonitor.makeLogEntry()
					.withLogger(logger)
					.withException(ex)
					.withLogLevel(Level.WARN)
					.withMessage("Error occured processing message by {} ", this.pipeline.getClass().getSimpleName())
					.complete();
		}


	}

	private Long getTimestamp(String serviceName, UUID serviceUUID,
							  String mediaUnitID, PeerConnectionSample sample) {
		if (sample.timestamp != null) {
			return sample.timestamp;
		}
		this.flawMonitor.makeLogEntry()
				.withLogger(logger)
				.withLogLevel(Level.WARN)
				.withMessage("Missing timestamp at {} ", sample.timeZoneOffsetInMinute)
				.complete();
		Long result = Instant.now().toEpochMilli();
		return result;
	}


	private String getSampleTimeZoneID(String serviceName,
									   UUID serviceUUID,
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
			this.flawMonitor.makeLogEntry()
					.withLogger(logger)
					.withException(ex)
					.withLogLevel(Level.WARN)
					.withMessage("Cannot parse timeZoneOffsetInMinute {} ", sample.timeZoneOffsetInMinute)
					.complete();
			return ZoneOffset.UTC.getId();
		}

		if (hours == 0) {
			return ZoneOffset.UTC.getId();
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

		ZoneOffset zoneOffset;
		try {
			zoneOffset = ZoneOffset.of(offsetID);
		} catch (Exception ex) {
			this.flawMonitor.makeLogEntry()
					.withLogger(logger)
					.withException(ex)
					.withLogLevel(Level.WARN)
					.withMessage("Cannot parse zoneoffset {} ", offsetID)
					.complete();
			return null;
		}

		if (zoneOffset == null) {
			return null;
		}
		return zoneOffset.getId();
	}
}
