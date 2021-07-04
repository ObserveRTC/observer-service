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

package org.observertc.webrtc.observer.sources;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import io.micronaut.websocket.WebSocketSession;
import io.micronaut.websocket.annotation.OnClose;
import io.micronaut.websocket.annotation.OnMessage;
import io.micronaut.websocket.annotation.OnOpen;
import io.micronaut.websocket.annotation.ServerWebSocket;
import io.reactivex.rxjava3.core.Observable;
import org.observertc.webrtc.observer.common.UUIDAdapter;
import org.observertc.webrtc.observer.dto.pcsamples.v20200114.PeerConnectionSample;
import org.observertc.webrtc.observer.evaluators.ProcessingPipeline;
import org.observertc.webrtc.observer.micrometer.FlawMonitor;
import org.observertc.webrtc.observer.micrometer.MonitorProvider;
import org.observertc.webrtc.observer.samples.ClientSample;
import org.observertc.webrtc.observer.samples.ObservedClientSampleBuilder;
import org.observertc.webrtc.observer.security.WebsocketAccessTokenValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Service should be UUId, because currently mysql stores it as
 * binary and with that type the search is fast for activestreams. thats why.
 */
@Secured(SecurityRule.IS_ANONYMOUS)
@ServerWebSocket("/pcsamples/{serviceUUIDStr}/{mediaUnitID}")
public class WebsocketPCSamples {

	private static final Logger logger = LoggerFactory.getLogger(WebsocketPCSamples.class);
	private final ObjectReader objectReader;
	private final FlawMonitor flawMonitor;
	private Map<String, Instant> expirations;

	@Inject
	PCSampleToClientSampleConverter pcSampleConverter;

	@Inject
	ProcessingPipeline processingPipeline;

	@Inject
	MeterRegistry meterRegistry;

	@Inject
	WebsocketCustomCloseReasons securityCustomCloseReasons;

	@Inject
	WebsocketAccessTokenValidator websocketAccessTokenValidator;

	public WebsocketPCSamples(
			ObjectMapper objectMapper,
			MonitorProvider monitorProvider
	) {
		this.objectReader = objectMapper.reader();
		this.flawMonitor = monitorProvider.makeFlawMonitorFor(this.getClass());
		this.expirations = new ConcurrentHashMap<>();
	}

	@PostConstruct
	void setup() {

	}

	@OnOpen
	public void onOpen(
			String serviceUUIDStr,
			String mediaUnitID,
			WebSocketSession session) {
		try {
			// validated access token from websocket
			if (websocketAccessTokenValidator.isEnabled()) {
				var expiration = new AtomicReference<Instant>(null);
				String accessToken = WebsocketAccessTokenValidator.getAccessToken(session);
				boolean isValid = websocketAccessTokenValidator.isValid(accessToken, expiration);
				if (!isValid) {
					session.close(securityCustomCloseReasons.getInvalidAccessToken());
					return;
				}
				this.expirations.put(session.getId(), expiration.get());
			}
			this.meterRegistry.counter(
					"observertc_pcsamples_opened_websockets"
			).increment();
			return ;
		} catch (Throwable t) {
			logger.warn("MeterRegistry just caused an error by counting samples", t);
		}
	}

	@OnClose
	public void onClose(
			String serviceUUIDStr,
			String mediaUnitID,
			WebSocketSession session) {
		try {
			this.expirations.remove(session.getId());
			this.meterRegistry.counter(
					"observertc_pcsamples_closed_websockets"
			).increment();

		} catch (Throwable t) {
			logger.warn("MeterRegistry just caused an error by counting samples", t);
		}
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

		// check expiration of validated tokens
		if (this.websocketAccessTokenValidator.isEnabled()) {
			Instant now = Instant.now();
			Instant expires = this.expirations.get(session.getId());
			if (expires.compareTo(now) < 0) {
				session.close(securityCustomCloseReasons.getAccessTokenExpired());
				return;
			}
		}
		try {
			this.meterRegistry.counter(
					"observertc_pcsamples",
					List.of(
							Tag.of("mediaUnit", mediaUnitID)
					)
			).increment();
		} catch (Throwable t) {
			logger.warn("MeterRegistry just caused an error by counting samples", t);
		}
		PeerConnectionSample sample;
		try {
			sample = this.objectReader.readValue(messageBytes, PeerConnectionSample.class);
		} catch (Throwable t) {
			this.flawMonitor.makeLogEntry()
					.withException(t)
					.withLogger(logger)
					.withLogLevel(Level.WARN)
					.withMessage("There was an exception happened during interpretation")
					.complete();
			return;
		}


		try {
			ClientSample clientSample = pcSampleConverter.apply(sample);
			var observedClientSample = ObservedClientSampleBuilder.from(clientSample)
					.withServiceId(serviceUUID.toString())
					.withMediaUnitId(mediaUnitID)
					.build();
			Observable.just(observedClientSample)
					.subscribe(this.processingPipeline);
		} catch (Exception ex) {
			this.flawMonitor.makeLogEntry()
					.withException(ex)
					.withMessage("Error occured processing message by {} ", this.getClass().getSimpleName())
					.complete();
		}
	}
}
