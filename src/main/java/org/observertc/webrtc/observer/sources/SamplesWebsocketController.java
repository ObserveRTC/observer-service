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
import io.reactivex.rxjava3.functions.Consumer;
import org.observertc.webrtc.observer.configs.ObserverConfig;
import org.observertc.webrtc.observer.micrometer.ExposedMetrics;
import org.observertc.webrtc.observer.micrometer.FlawMonitor;
import org.observertc.webrtc.observer.micrometer.MonitorProvider;
import org.observertc.webrtc.observer.repositories.HazelcastMaps;
import org.observertc.webrtc.observer.repositories.RepositoryEvents;
import org.observertc.webrtc.observer.samples.*;
import org.observertc.webrtc.observer.security.WebsocketAccessTokenValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import java.io.IOException;
import java.io.InvalidObjectException;
import java.time.Instant;
import java.util.LinkedList;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Service should be UUId, because currently mysql stores it as
 * binary and with that type the search is fast for activestreams. thats why.
 */
@Secured(SecurityRule.IS_ANONYMOUS)
@ServerWebSocket("/samples/{serviceId}/{mediaUnitId}")
public class SamplesWebsocketController {

	private static final Logger logger = LoggerFactory.getLogger(SamplesWebsocketController.class);
	private final ObjectReader objectReader;
	private final FlawMonitor flawMonitor;
	private Map<String, Instant> expirations;
//	private Map<String, UUID> sessionToClients;
//	private Map<UUID, WebSocketSession> clientSessions;

	@Inject
	ExposedMetrics exposedMetrics;

	@Inject
    WebsocketCustomCloseReasons customCloseReasons;

	@Inject
	WebsocketAccessTokenValidator websocketAccessTokenValidator;

	@Inject
	ObserverConfig.SourcesConfig.WebsocketsConfig config;

	@Inject
	RepositoryEvents repositoryEvents;

	@Inject
	HazelcastMaps hazelcastMaps;

    @Inject
    ClientSamplesCollector clientSamplesCollector;

    @Inject
    SfuSamplesCollector sfuSamplesCollector;

//	@Inject
//	ObservedClientSampleProcessingPipeline observedClientSampleProcessingPipeline;

	public SamplesWebsocketController(
			ObjectMapper objectMapper,
			MonitorProvider monitorProvider
	) {
		this.objectReader = objectMapper.reader();
		this.flawMonitor = monitorProvider.makeFlawMonitorFor(this.getClass()).withDefaultLogger(logger).withDefaultLogLevel(Level.WARN);
		this.expirations = new ConcurrentHashMap<>();
//		this.clientSessions = new ConcurrentHashMap<>();
//		this.sessionToClients = new ConcurrentHashMap<>();
	}

	@PostConstruct
	void setup() {
//		this.repositoryEvents.updatedClientMessageEvents()
//				.flatMap(Observable::fromIterable)
//				.subscribe(event -> {
//					var eventMessage = event.getNewValue();
//					if (Objects.isNull(eventMessage)) {
//						return;
//					}
//					UUID clientId = eventMessage.getClientId();
//					GeneralEntryDTO value = eventMessage.getValue();
//					if (Objects.isNull(clientId) || Objects.isNull(value)) {
//						return;
//					}
//					WebSocketSession session = this.clientSessions.get(clientId);
//					if (Objects.isNull(session)) {
//						logger.warn("A message received for client {} have not registered in observer. Message: {}", clientId, value);
//						return;
//					}
//					session.send(value.value);
//				});
	}

	@PreDestroy
	void teardown() {
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
//			var requestParameters = session.getRequestParameters();
//			String clientIdParam = requestParameters.get("clientId");
//			if (Objects.nonNull(clientIdParam)) {
//				UUID clientId = UUID.fromString(clientIdParam);
//				this.hazelcastMaps.getClientMessages().put(clientId, null);
//				this.clientSessions.put(clientId, session);
//				this.sessionToClients.put(session.getId(), clientId);
//			}
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
			this.exposedMetrics.incrementClientSamplesOpenedWebsockets(serviceId, mediaUnitId);

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
			this.exposedMetrics.incrementClientSamplesClosedWebsockets(serviceId, mediaUnitId);
//			UUID clientId = this.sessionToClients.get(session.getId());
//			if (Objects.nonNull(clientId)) {
//				this.clientSessions.remove(clientId, session);
//				this.hazelcastMaps.getClientMessages().remove(clientId);
//			}
		} catch (Throwable t) {
			logger.warn("MeterRegistry just caused an error by counting samples", t);
		}
	}

	//	@OnMessage(maxPayloadLength = 1000000) // 1MB
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
			this.exposedMetrics.incrementClientSamplesReceived(serviceId, mediaUnitId);
		} catch (Throwable t) {
			logger.warn("MeterRegistry just caused an error by counting samples", t);
		}
		Samples message;
		try {
            message = this.objectReader.readValue(messageBytes, Samples.class);
		} catch (IOException e) {
			this.flawMonitor.makeLogEntry()
					.withMessage("Exception while parsing {}", ClientSample.class.getSimpleName())
					.withException(e)
					.complete();
			session.close(this.customCloseReasons.getInvalidInput(e.getMessage()));
			return;
		}
        if (Objects.isNull(message)) {
            return;
        }
        if (Objects.nonNull(message.clientSamples)) {
            if (this.config.maxClientSamplesBatch < message.clientSamples.length) {
                logger.warn("Client sample batch is too large");
                return;
            }
            if (!this.acceptClientSamples(session, serviceId, mediaUnitId, message.clientSamples)) {
                return;
            }
        }
        if (Objects.nonNull(message.sfuSamples)) {
            if (this.config.maxSfuSamplesBatch < message.sfuSamples.length) {
                logger.warn("Sfu sample batch is too large");
                return;
            }
            if (!this.acceptSfuSamples(session, serviceId, mediaUnitId, message.sfuSamples)) {
                return;
            }
        }
    }

    private boolean acceptSfuSamples(WebSocketSession session, String serviceId, String mediaUnitId, SfuSample[] samples) {
        var observedSfuSamples = new LinkedList<ObservedSfuSample>();
        Consumer<SfuSample> processor = sample -> {
            var observedSfuSample = ObservedSfuSampleBuilder.from(sample)
                    .withServiceId(serviceId)
                    .withMediaUnitId(mediaUnitId)
                    .build();
            observedSfuSamples.add(observedSfuSample);
        };
        if (!this.accept(session, samples, processor) || observedSfuSamples.size() < 1) {
            return false;
        }
        try {
            this.sfuSamplesCollector.addAll(observedSfuSamples);
        } catch (Exception ex) {
            this.flawMonitor.makeLogEntry()
                    .withException(ex)
                    .withMessage("Error occurred while collecting samples")
                    .complete();
            return false;
        }
        return true;
    }

    private boolean acceptClientSamples(WebSocketSession session, String serviceId, String mediaUnitId, ClientSample[] samples) {
        var observedClientSamples = new LinkedList<ObservedClientSample>();
        Consumer<ClientSample> processor = sample -> {
            var observedClientSample = ObservedClientSampleBuilder.from(sample)
                    .withServiceId(serviceId)
                    .withMediaUnitId(mediaUnitId)
                    .build();
            observedClientSamples.add(observedClientSample);
        };
        if (!this.accept(session, samples, processor) || observedClientSamples.size() < 1) {
            return false;
        }
        try {
            this.clientSamplesCollector.addAll(observedClientSamples);
        } catch (Exception ex) {
            this.flawMonitor.makeLogEntry()
                    .withException(ex)
                    .withMessage("Error occurred while collecting samples")
                    .complete();
            return false;
        }
        return true;
    }



	private<T> boolean accept(WebSocketSession session, T[] samples, Consumer<T> processor) {
        try {
            for (var sample : samples) {
                processor.accept(sample);
            }
        } catch (InvalidObjectException invalidEx) {
            final String message = invalidEx.getMessage();
            session.close(
                    this.customCloseReasons.getInvalidInput(message)
            );
            return false;
        } catch (Throwable ex) {
            this.flawMonitor.makeLogEntry()
                    .withException(ex)
                    .withMessage("Error occured processing samples")
                    .complete();
            session.close(
                    this.customCloseReasons.getInternalServerError(ex.getMessage())
            );
            return false;
        }
        return true;
    }
}
