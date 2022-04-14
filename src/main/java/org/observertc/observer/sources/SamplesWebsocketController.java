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

package org.observertc.observer.sources;

import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import io.micronaut.websocket.WebSocketSession;
import io.micronaut.websocket.annotation.OnClose;
import io.micronaut.websocket.annotation.OnMessage;
import io.micronaut.websocket.annotation.OnOpen;
import io.micronaut.websocket.annotation.ServerWebSocket;
import jakarta.inject.Inject;
import org.observertc.observer.configs.ObserverConfig;
import org.observertc.observer.mappings.Decoder;
import org.observertc.observer.micrometer.ExposedMetrics;
import org.observertc.observer.micrometer.FlawMonitor;
import org.observertc.observer.micrometer.MonitorProvider;
import org.observertc.observer.repositories.HazelcastMaps;
import org.observertc.observer.repositories.RepositoryEvents;
import org.observertc.schemas.samples.Samples;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * Service should be UUId, because currently mysql stores it as
 * binary and with that type the search is fast for activestreams. thats why.
 */
@Secured(SecurityRule.IS_ANONYMOUS)
@ServerWebSocket("/samples/{serviceId}/{mediaUnitId}")
public class SamplesWebsocketController {

	private static final Logger logger = LoggerFactory.getLogger(SamplesWebsocketController.class);
	private final FlawMonitor flawMonitor;
//	private Map<String, UUID> sessionToClients;
	private Map<String, WebSocketSession> webSocketSessions = new ConcurrentHashMap<>();

	@Inject
	ExposedMetrics exposedMetrics;

	@Inject
    WebsocketCustomCloseReasons customCloseReasons;

	@Inject
	RepositoryEvents repositoryEvents;

	@Inject
	HazelcastMaps hazelcastMaps;

	@Inject
    SamplesCollector samplesCollector;

    private final ObserverConfig.SourcesConfig.WebsocketsConfig config;
	private Consumer<ReceivedMessage> acceptor;

	public SamplesWebsocketController(
	        ObserverConfig observerConfig,
            MonitorProvider monitorProvider
    ) {
		this.config = observerConfig.sources.websocket;
		this.flawMonitor = monitorProvider.makeFlawMonitorFor(this.getClass()).withDefaultLogger(logger).withDefaultLogLevel(Level.WARN);
//		this.clientSessions = new ConcurrentHashMap<>();
//		this.sessionToClients = new ConcurrentHashMap<>();
	}

	@PostConstruct
	void setup() {
		var decoder = SamplesDecoder.builder(logger)
				.withCodecType(this.config.format)
				.build();
		var messageProcessor = this.createMessageProcessor(decoder);
		// TODO: here create an assembler for assembling the chunks.
		this.acceptor = messageProcessor;
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
			this.webSocketSessions.put(session.getId(), session);
//			var requestParameters = session.getRequestParameters();
//			String clientIdParam = requestParameters.get("clientId");
//			if (Objects.nonNull(clientIdParam)) {
//				UUID clientId = UUID.fromString(clientIdParam);
//				this.hazelcastMaps.getClientMessages().put(clientId, null);
//				this.clientSessions.put(clientId, session);
//				this.sessionToClients.put(session.getId(), clientId);
//			}
			this.exposedMetrics.incrementSamplesReceived(serviceId, mediaUnitId);
			logger.info("Session is opened");
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

			this.exposedMetrics.incrementSamplesOpenedWebsockets(serviceId, mediaUnitId);
//			UUID clientId = this.sessionToClients.get(session.getId());
//			if (Objects.nonNull(clientId)) {
//				this.clientSessions.remove(clientId, session);
//				this.hazelcastMaps.getClientMessages().remove(clientId);
//			}
			this.webSocketSessions.remove(session.getId());
			logger.info("Session is closed");
		} catch (Throwable t) {
			logger.warn("MeterRegistry just caused an error by counting samples", t);
		}
	}

	@OnMessage(maxPayloadLength = 1000000) // 1MB
	public void onMessage(
			String serviceId,
			String mediaUnitId,
			byte[] messageBytes,
			WebSocketSession session) {

		try {
			this.exposedMetrics.incrementSamplesClosedWebsockets(serviceId, mediaUnitId);
		} catch (Throwable t) {
			logger.warn("MeterRegistry just caused an error by counting samples", t);
		}
		var receivedMessage = ReceivedMessage.of(
				session.getId(),
				serviceId,
				mediaUnitId,
				messageBytes
		);
		this.acceptor.accept(receivedMessage);
	}

//	@OnMessage(maxPayloadLength = 1000000) // 1MB
//	public void onMessage(
//			String serviceId,
//			String mediaUnitId,
//			String messageString,
//			WebSocketSession session) {
//
//		try {
//			this.exposedMetrics.incrementSamplesReceived(serviceId, mediaUnitId);
//		} catch (Throwable t) {
//			logger.warn("MeterRegistry just caused an error by counting samples", t);
//		}
//		var messageBytes = Base64.decode(messageString);
//		var receivedMessage = ReceivedMessage.of(
//				session.getId(),
//				serviceId,
//				mediaUnitId,
//				messageBytes
//		);
//		this.acceptor.accept(receivedMessage);
//	}


	private Consumer<ReceivedMessage> createMessageProcessor(Decoder<byte[], Samples> decoder) {
		return receivedMessage -> {
			try {
				var samples = decoder.decode(receivedMessage.message);
				var receivedSamples = ReceivedSamples.of(
						receivedMessage.serviceId,
						receivedMessage.mediaUnitId,
						samples
				);
				this.samplesCollector.accept(receivedSamples);
			} catch (Throwable ex) {
				this.flawMonitor.makeLogEntry()
						.withException(ex)
						.withMessage("Error occured processing samples")
						.complete();
				if (Objects.isNull(receivedMessage.sessionId)) {
					return;
				}
				var websocketSession = this.webSocketSessions.get(receivedMessage.sessionId);
				if (Objects.isNull(websocketSession)) {
					return;
				}
				websocketSession.close(
						this.customCloseReasons.getInternalServerError(ex.getMessage())
				);
			}
		};
	}


}
