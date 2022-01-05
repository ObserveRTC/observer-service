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

import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import io.micronaut.websocket.WebSocketSession;
import io.micronaut.websocket.annotation.OnClose;
import io.micronaut.websocket.annotation.OnMessage;
import io.micronaut.websocket.annotation.OnOpen;
import io.micronaut.websocket.annotation.ServerWebSocket;
import org.observertc.webrtc.observer.configs.ObserverConfig;
import org.observertc.webrtc.observer.micrometer.ExposedMetrics;
import org.observertc.webrtc.observer.micrometer.FlawMonitor;
import org.observertc.webrtc.observer.micrometer.MonitorProvider;
import org.observertc.webrtc.observer.repositories.HazelcastMaps;
import org.observertc.webrtc.observer.repositories.RepositoryEvents;
import org.observertc.webrtc.observer.samples.Samples;
import org.observertc.webrtc.observer.security.WebsocketAccessTokenValidator;
import org.observertc.webrtc.observer.sources.inboundSamples.InboundSamplesAcceptor;
import org.observertc.webrtc.observer.sources.inboundSamples.InboundSamplesAcceptorFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import java.io.IOException;
import java.util.function.Function;

/**
 * Service should be UUId, because currently mysql stores it as
 * binary and with that type the search is fast for activestreams. thats why.
 */
@Secured(SecurityRule.IS_ANONYMOUS)
@ServerWebSocket("/samples/{serviceId}/{mediaUnitId}")
public class SamplesWebsocketController {

	private static final Logger logger = LoggerFactory.getLogger(SamplesWebsocketController.class);
	private final FlawMonitor flawMonitor;
	private Function<byte[], Samples> converter;
//	private Map<String, UUID> sessionToClients;
//	private Map<UUID, WebSocketSession> clientSessions;

	@Inject
	ExposedMetrics exposedMetrics;

	@Inject
    WebsocketCustomCloseReasons customCloseReasons;

	@Inject
	WebsocketAccessTokenValidator websocketAccessTokenValidator;

	@Inject
	RepositoryEvents repositoryEvents;

	@Inject
	HazelcastMaps hazelcastMaps;

	@Inject
    ClientSamplesCollector clientSamplesCollector;

    @Inject
    SfuSamplesCollector sfuSamplesCollector;

    private final ObserverConfig.SourcesConfig.WebsocketsConfig config;
    private final InboundSamplesAcceptor inboundSamplesAcceptor;

//	@Inject
//	ObservedClientSampleProcessingPipeline observedClientSampleProcessingPipeline;

	public SamplesWebsocketController(
	        ObserverConfig observerConfig,
            InboundSamplesAcceptorFactory acceptorFactory,
            MonitorProvider monitorProvider
    ) {
		this.config = observerConfig.sources.websockets;
		this.inboundSamplesAcceptor = acceptorFactory.makeAcceptor(this.config);
		this.flawMonitor = monitorProvider.makeFlawMonitorFor(this.getClass()).withDefaultLogger(logger).withDefaultLogLevel(Level.WARN);
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
		Function<byte[], byte[]> decrypt = Function.identity();
		Function<byte[], byte[]> unzip = Function.identity();;
		Function<byte[], Samples> parse = input -> new Samples();
		this.converter = decrypt.andThen(unzip.andThen(parse));
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

	@OnMessage(maxPayloadLength = 1000000) // 1MB
	public void onMessage(
			String serviceId,
			String mediaUnitId,
			byte[] messageBytes,
			WebSocketSession session) {

		try {
			this.exposedMetrics.incrementClientSamplesReceived(serviceId, mediaUnitId);
		} catch (Throwable t) {
			logger.warn("MeterRegistry just caused an error by counting samples", t);
		}
		try {
            this.inboundSamplesAcceptor.accept(serviceId, mediaUnitId, messageBytes);
		} catch (IOException e) {
			this.flawMonitor.makeLogEntry()
					.withMessage("Exception while accepting sample")
					.withException(e)
					.complete();
			session.close(this.customCloseReasons.getInvalidInput(e.getMessage()));
			return;
        } catch (Throwable ex) {
            this.flawMonitor.makeLogEntry()
                    .withException(ex)
                    .withMessage("Error occured processing samples")
                    .complete();
            session.close(
                    this.customCloseReasons.getInternalServerError(ex.getMessage())
            );
        }
    }
}
