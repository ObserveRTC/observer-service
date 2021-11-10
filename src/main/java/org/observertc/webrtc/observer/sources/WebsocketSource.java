///*
// * Copyright  2020 Balazs Kreith
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// *     http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//
//package org.observertc.webrtc.observer.sources;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.fasterxml.jackson.databind.ObjectReader;
//import io.micronaut.scheduling.annotation.Scheduled;
//import io.micronaut.security.annotation.Secured;
//import io.micronaut.security.rules.SecurityRule;
//import io.micronaut.websocket.WebSocketSession;
//import io.micronaut.websocket.annotation.OnClose;
//import io.micronaut.websocket.annotation.OnMessage;
//import io.micronaut.websocket.annotation.OnOpen;
//import io.micronaut.websocket.annotation.ServerWebSocket;
//import io.reactivex.rxjava3.core.Observable;
//import org.observertc.webrtc.observer.configs.ObserverConfig;
//import org.observertc.webrtc.observer.dto.GeneralEntryDTO;
//import org.observertc.webrtc.observer.evaluators.ObservedClientSampleProcessingPipeline;
//import org.observertc.webrtc.observer.micrometer.ExposedMetrics;
//import org.observertc.webrtc.observer.micrometer.FlawMonitor;
//import org.observertc.webrtc.observer.micrometer.MonitorProvider;
//import org.observertc.webrtc.observer.repositories.HazelcastMaps;
//import org.observertc.webrtc.observer.repositories.RepositoryEvents;
//import org.observertc.webrtc.observer.security.WebsocketAccessTokenValidator;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.slf4j.event.Level;
//
//import javax.annotation.PostConstruct;
//import javax.inject.Inject;
//import java.time.Instant;
//import java.util.*;
//import java.util.concurrent.ConcurrentHashMap;
//import java.util.concurrent.atomic.AtomicReference;
//import java.util.concurrent.locks.ReadWriteLock;
//import java.util.concurrent.locks.ReentrantReadWriteLock;
//
///**
// * Service should be UUId, because currently mysql stores it as
// * binary and with that type the search is fast for activestreams. thats why.
// */
//@Secured(SecurityRule.IS_ANONYMOUS)
//@ServerWebSocket("/websockets/{serviceId}/{mediaUnitId}")
//public class WebsocketSource {
//
//	private static final Logger logger = LoggerFactory.getLogger(WebsocketSource.class);
//	private final ObjectReader objectReader;
//	private final FlawMonitor flawMonitor;
//	private ReadWriteLock readWriteLock;
//	private Map<String, WebSocketSession> sessions;
//	private Map<UUID, String> clientSessionIds;
//	private Map<String, Instant> expirations;
//
//	private boolean enabled = false;
//	private boolean restrictedAccess = true;
//
//	@Inject
//	ExposedMetrics exposedMetrics;
//
//	@Inject
//	WebsocketAccessTokenValidator websocketAccessTokenValidator;
//
//	@Inject
//	WebsocketCustomCloseReasons customCloseReasons;
//
//	@Inject
//	ObserverConfig.SourcesConfig.ClientSamplesConfig config;
//
//	@Inject
//	RepositoryEvents repositoryEvents;
//
//	@Inject
//	HazelcastMaps hazelcastMaps;
//
//	@Inject
//	ObservedClientSampleProcessingPipeline observedClientSampleProcessingPipeline;
//
//
//	public WebsocketSource(
//			ObjectMapper objectMapper,
//			MonitorProvider monitorProvider
//	) {
//		this.objectReader = objectMapper.reader();
//		this.flawMonitor = monitorProvider.makeFlawMonitorFor(this.getClass()).withDefaultLogger(logger).withDefaultLogLevel(Level.WARN);
//		this.readWriteLock = new ReentrantReadWriteLock();
//		this.sessions = new HashMap<>();
//		this.clientSessionIds = new HashMap<>();
//		this.expirations = new ConcurrentHashMap<>();
//	}
//
//	@Scheduled(initialDelay = "10m", fixedDelay = "5m")
//	void checkExpirations() {
//		if (this.expirations.size() < 1) {
//			return;
//		}
//		List<String> expiring = new LinkedList<>();
//		List<String> expired = new LinkedList<>();
//		Long now = Instant.now().toEpochMilli();
//		Long nearNow = now - (5 * 60 * 1000);
//		for (Map.Entry<String, Instant> entry : this.expirations.entrySet()) {
//			var sessionId = entry.getKey();
//			var expires = entry.getValue().toEpochMilli();
//			if (nearNow < expires) {
//				continue;
//			}
//			if (now < expires) {
//				expiring.add(sessionId);
//			} else {
//				expired.add(sessionId);
//			}
//		}
//		if (expired.size() < 1 && expiring.size() < 1) {
//			return;
//		}
//		try (var lock = this.getAutoClosableReadLock()) {
//			expiring.forEach(sessionId -> {
//				WebSocketSession session = this.sessions.get(sessionId);
//				if (Objects.isNull(session)) {
//					logger.warn("Session does not exists");
//					return;
//				}
//				// send a request to revalidate access token
////				session.send()
//			});
//			expired.forEach(sessionId -> {
//				this.expirations.remove(sessionId);
//				WebSocketSession session = this.sessions.get(sessionId);
//				if (Objects.isNull(session)) {
//					logger.warn("Session does not exists");
//					return;
//				}
//				session.close(customCloseReasons.getAccessTokenExpired());
//			});
//		} catch (Exception ex) {
//
//		}
//	}
//
//	@PostConstruct
//	void setup() {
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
//					String sessionId = this.clientSessionIds.get(clientId);
//					if (Objects.isNull(sessionId)) {
//						return;
//					}
//					WebSocketSession session = this.sessions.get(sessionId);
//					if (Objects.isNull(session)) {
//						logger.warn("A message received for client {} have not registered in observer. Message: {}", clientId, value);
//						return;
//					}
//					session.send(value.value);
//				});
//	}
//
//	@OnOpen
//	public void onOpen(
//			String serviceId,
//			String mediaUnitId,
//			WebSocketSession session) {
//		if (!this.enabled) {
//			session.close(customCloseReasons.getWebsocketIsDisabled());
//			return;
//		}
//		var params = session.getRequestParameters();
//		final String accessToken = params.get("access_token");
//		final String clientId = params.get("clientId");
//		final boolean accessTokenIsNull = Objects.isNull(accessToken);
//		if (accessTokenIsNull && this.restrictedAccess) {
//			session.close(customCloseReasons.getInvalidAccessToken());
//			return;
//		}
//		if (accessTokenIsNull && Objects.isNull(clientId)) {
//			this.exposedMetrics.incrementClientSamplesOpenedWebsockets(serviceId, mediaUnitId);
//			return;
//		}
//		try (var lock = this.getAutoClosableWriteLock()) {
//			if (!accessTokenIsNull) {
//				var expiration = new AtomicReference<Instant>(null);
//				boolean isValid = websocketAccessTokenValidator.isValid(accessToken, expiration);
//				if (!isValid) {
//					session.close(customCloseReasons.getInvalidAccessToken());
//					return;
//				}
//			}
//			if (websocketAccessTokenValidator.isEnabled()) {
////				this.expirations.put(session.getId(), expiration.get());
//			}
//			if (Objects.nonNull(clientId)) {
//				//
//			}
//		} catch (Exception ex) {
//
//		}
//	}
//
//	@OnClose
//	public void onClose(
//			String serviceId,
//			String mediaUnitId,
//			WebSocketSession session) {
//		try {
//
//			this.expirations.remove(session.getId());
//			this.exposedMetrics.incrementClientSamplesClosedWebsockets(serviceId, mediaUnitId);
////			UUID clientId = this.sessionToClients.get(session.getId());
////			if (Objects.nonNull(clientId)) {
////				this.clientSessions.remove(clientId, session);
////				this.hazelcastMaps.getClientMessages().remove(clientId);
////			}
//		} catch (Throwable t) {
//			logger.warn("MeterRegistry just caused an error by counting samples", t);
//		}
//	}
//
//	//	@OnMessage(maxPayloadLength = 1000000) // 1MB
//	@OnMessage
//	public void onMessage(
//			String serviceId,
//			String mediaUnitId,
//			byte[] messageBytes,
//			WebSocketSession session) {
//
//	}
//
//	private AutoCloseable getAutoClosableReadLock() {
//		var readLock = this.readWriteLock.readLock();
//		return new AutoCloseable() {
//			@Override
//			public void close() throws Exception {
//				readLock.unlock();
//			}
//		};
//	}
//
//	private AutoCloseable getAutoClosableWriteLock() {
//		var writeLock = this.readWriteLock.writeLock();
//		return new AutoCloseable() {
//			@Override
//			public void close() throws Exception {
//				writeLock.unlock();
//			}
//		};
//	}
//}
