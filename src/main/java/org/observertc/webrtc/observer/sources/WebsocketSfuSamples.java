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
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import io.micronaut.websocket.WebSocketSession;
import io.micronaut.websocket.annotation.OnClose;
import io.micronaut.websocket.annotation.OnMessage;
import io.micronaut.websocket.annotation.OnOpen;
import io.micronaut.websocket.annotation.ServerWebSocket;
import io.reactivex.rxjava3.core.Observable;
import org.observertc.webrtc.observer.configs.ObserverConfig;
import org.observertc.webrtc.observer.evaluators.ObservedSfuSampleProcessingPipeline;
import org.observertc.webrtc.observer.micrometer.ExposedMetrics;
import org.observertc.webrtc.observer.micrometer.FlawMonitor;
import org.observertc.webrtc.observer.micrometer.MonitorProvider;
import org.observertc.webrtc.observer.samples.ObservedSfuSampleBuilder;
import org.observertc.webrtc.observer.samples.SfuSample;
import org.observertc.webrtc.observer.security.WebsocketAccessTokenValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Service should be UUId, because currently mysql stores it as
 * binary and with that type the search is fast for activestreams. thats why.
 */
@Secured(SecurityRule.IS_ANONYMOUS)
@ServerWebSocket("/sfusamples/{serviceId}/{mediaUnitId}")
public class WebsocketSfuSamples {

	private static final Logger logger = LoggerFactory.getLogger(WebsocketSfuSamples.class);
	private final ObjectReader objectReader;
	private final FlawMonitor flawMonitor;
	private Map<String, Instant> expirations;

	@Inject
    ExposedMetrics exposedMetrics;

	@Inject
	WebsocketCustomCloseReasons customCloseReasons;

	@Inject
	WebsocketAccessTokenValidator websocketAccessTokenValidator;

	@Inject
	ObserverConfig.SourcesConfig.SfuSamplesConfig config;

	@Inject
	ObservedSfuSampleProcessingPipeline observedSfuSampleProcessingPipeline;

	public WebsocketSfuSamples(
			ObjectMapper objectMapper,
			MonitorProvider monitorProvider
	) {
		this.objectReader = objectMapper.reader();
		this.flawMonitor = monitorProvider.makeFlawMonitorFor(this.getClass()).withDefaultLogger(logger).withDefaultLogLevel(Level.WARN);
		this.expirations = new ConcurrentHashMap<>();
	}

	@PostConstruct
	void setup() {

	}

	@OnOpen
	public void onOpen(
			String serviceId,
			String mediaUnitId,
			WebSocketSession session) {
		try {
			if (!this.config.enabled) {
				session.close(customCloseReasons.getWebsocketIsDisabled());
				return;
			}
			// validated access token from websocket
			if (websocketAccessTokenValidator.isEnabled()) {
				var expiration = new AtomicReference<Instant>(null);
				String accessToken = WebsocketAccessTokenValidator.getAccessToken(session);
				boolean isValid = websocketAccessTokenValidator.isValid(accessToken, expiration);
				if (!isValid) {
					session.close(customCloseReasons.getInvalidAccessToken());
					return;
				}
				this.expirations.put(session.getId(), expiration.get());
			}
			this.exposedMetrics.incrementSfuSamplesOpenedWebsockets(serviceId, mediaUnitId);

		} catch (Throwable t) {
			logger.warn("MeterRegistry just caused an error by counting samples", t);
		}
	}

	@OnClose
	public void onClose(
			String serviceId,
			String mediaUnitId,
			WebSocketSession session) {
		try {
			this.expirations.remove(session.getId());
			this.exposedMetrics.incrementSfuSamplesClosedWebsockets(serviceId, mediaUnitId);

		} catch (Throwable t) {
			logger.warn("MeterRegistry just caused an error by counting samples", t);
		}
	}

	@OnMessage
	public void onMessage(
			String serviceId,
			String mediaUnitId,
			byte[] messageBytes,
			WebSocketSession session) {

		// check expiration of validated tokens
		if (this.websocketAccessTokenValidator.isEnabled()) {
			Instant now = Instant.now();
			Instant expires = this.expirations.get(session.getId());
			if (expires.compareTo(now) < 0) {
				session.close(customCloseReasons.getAccessTokenExpired());
				return;
			}
		}
		try {
			this.exposedMetrics.incrementSfuSamplesReceived(serviceId, mediaUnitId);
		} catch (Throwable t) {
			logger.warn("MeterRegistry just caused an error by counting samples", t);
		}
		SfuSample sample;
		try {
			sample = this.objectReader.readValue(messageBytes, SfuSample.class);
		} catch (IOException e) {
			this.flawMonitor.makeLogEntry()
					.withMessage("Exception while parsing {}", SfuSample.class.getSimpleName())
					.withException(e)
					.complete();
			return;
		}


		try {
			var observedSfuSample = ObservedSfuSampleBuilder.from(sample)
					.withServiceId(serviceId)
					.withMediaUnitId(mediaUnitId)
					.build();
			Observable.just(observedSfuSample)
					.subscribe(this.observedSfuSampleProcessingPipeline);
		} catch (Exception ex) {
			this.flawMonitor.makeLogEntry()
					.withException(ex)
					.withMessage("Error occured processing sample {} ", sample)
					.complete();
		}

	}
}
