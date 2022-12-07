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
import org.observertc.observer.HamokService;
import org.observertc.observer.common.Utils;
import org.observertc.observer.configs.ObserverConfig;
import org.observertc.observer.configs.TransportFormatType;
import org.observertc.observer.metrics.SourceMetrics;
import org.observertc.observer.repositories.RepositoryEvents;
import org.observertc.schemas.samples.Samples;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Secured(SecurityRule.IS_ANONYMOUS)
@ServerWebSocket("/samples/{serviceId}/{mediaUnitId}")
public class SamplesWebsocketController {

	private static final Logger logger = LoggerFactory.getLogger(SamplesWebsocketController.class);
	private Map<String, Input> inputs = new ConcurrentHashMap<>();

	@Inject
	SourceMetrics exposedMetrics;

	@Inject
    WebsocketCustomCloseReasons customCloseReasons;

	@Inject
	RepositoryEvents repositoryEvents;

	@Inject
    SamplesCollector samplesCollector;

	@Inject
	HamokService hamokService;

    private final ObserverConfig.SourcesConfig.WebsocketsConfig config;

	public SamplesWebsocketController(
	        ObserverConfig observerConfig
    ) {
		this.config = observerConfig.sources.websocket;
	}

	@PostConstruct
	void setup() {

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
//			if (!this.hamokService.areRemotePeersReady()) {
//				logger.warn("Dropping Websocket connection {}, because remote peers not ready");
//				session.close(customCloseReasons.getObserverRemotePeersNotReady());
//				return;
//			}
			
			var requestParameters = session.getRequestParameters();
			String providedSchemaVersion = requestParameters.get("schemaVersion");
			String providedFormat = requestParameters.get("format");
			var version = Utils.firstNotNull(providedSchemaVersion, Samples.VERSION);
			try {
				var format = TransportFormatType.getValueOrDefault(providedFormat, TransportFormatType.JSON);
				var acceptor = Acceptor.create(
						logger,
						mediaUnitId,
						serviceId,
						version,
						format,
						samplesCollector::accept
				).onError(ex -> {
					if (session.isOpen()) {
						var reason = this.customCloseReasons.getInvalidInput(ex.getMessage());
						session.close(reason);
						this.inputs.remove(session.getId());
					}
				});
				var input = new Input(acceptor, session, version);
				this.inputs.put(session.getId(), input);
			} catch (Exception ex) {
				var closeReason = this.customCloseReasons.getInvalidInput(ex.getMessage());
				session.close(closeReason);
				this.inputs.remove(session.getId());
				return;
			}
			this.exposedMetrics.incrementOpenedWebsockets(serviceId, mediaUnitId);
			logger.info("Session {} is opened, providedSchemaVersion: {}, providedFormat: {}", session.getId(), providedSchemaVersion, providedFormat);
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
			this.exposedMetrics.incrementClosedWebsockets(serviceId, mediaUnitId);
			this.inputs.remove(session.getId());
			logger.info("Session {} is closed", session.getId());
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
			this.exposedMetrics.incrementWebsocketReceivedSamples(serviceId, mediaUnitId);
		} catch (Throwable t) {
			logger.warn("MeterRegistry just caused an error by counting samples", t);
		}
		var input = this.inputs.get(session.getId());
		if (input == null) {
			var closeReason = this.customCloseReasons.getInternalServerError("The input does not exists");
			session.close(closeReason);
			return;
		}
//		logger.info("{}\n {}\n", input.version, Base64.encode(messageBytes));
		try {
			input.acceptor.accept(messageBytes);
			Long now = Instant.now().toEpochMilli();
			if (input.last.get() < now - 60000) {
				session.send("message");
				input.last.set(now);
			}
		} catch (Exception ex) {
			logger.warn("Exception happened while accepting message");
			if (input.session != null && input.session.isOpen()) {
				var reason = this.customCloseReasons.getInternalServerError(ex.getMessage());
				input.session.close(reason);
			}
			this.inputs.remove(session.getId());
		}
	}

	private class Input {
		final String version;
		final Acceptor acceptor;
		final WebSocketSession session;
		final AtomicLong last = new AtomicLong(Instant.now().toEpochMilli());

		private Input(Acceptor acceptor, WebSocketSession session, String version) {
			this.version = version;
			this.acceptor = acceptor;
			this.session = session;
		}
	}
}
