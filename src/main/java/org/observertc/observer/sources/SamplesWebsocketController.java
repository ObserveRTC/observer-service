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
import org.bson.internal.Base64;
import org.observertc.observer.common.Utils;
import org.observertc.observer.configs.ObserverConfig;
import org.observertc.observer.configs.TransportFormatType;
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
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service should be UUId, because currently mysql stores it as
 * binary and with that type the search is fast for activestreams. thats why.
 */
@Secured(SecurityRule.IS_ANONYMOUS)
@ServerWebSocket("/samples/{serviceId}/{mediaUnitId}")
public class SamplesWebsocketController {

	private static final Logger logger = LoggerFactory.getLogger(SamplesWebsocketController.class);
	private final FlawMonitor flawMonitor;
	private Map<String, Input> inputs = new ConcurrentHashMap<>();

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

	public SamplesWebsocketController(
	        ObserverConfig observerConfig,
            MonitorProvider monitorProvider
    ) {
		this.config = observerConfig.sources.websocket;
		this.flawMonitor = monitorProvider.makeFlawMonitorFor(this.getClass()).withDefaultLogger(logger).withDefaultLogLevel(Level.WARN);
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
			var requestParameters = session.getRequestParameters();
			String providedSchemaVersion = requestParameters.get("schemaVersion");
			String providedFormat = requestParameters.get("format");
			try {
				var format = TransportFormatType.getValueOrDefault(providedFormat, TransportFormatType.JSON);
				var acceptor = Acceptor.create(
						logger,
						mediaUnitId,
						serviceId,
						Utils.firstNotNull(providedSchemaVersion, Samples.VERSION),
						format,
						samplesCollector::accept
				).onError(ex -> {
					if (session.isOpen()) {
						var reason = this.customCloseReasons.getInvalidInput(ex.getMessage());
						session.close(reason);
						this.inputs.remove(session.getId());
					}
				});
				var input = new Input(acceptor, session);
				this.inputs.put(session.getId(), input);
			} catch (Exception ex) {
				var closeReason = this.customCloseReasons.getInvalidInput(ex.getMessage());
				session.close(closeReason);
				this.inputs.remove(session.getId());
				return;
			}
			this.exposedMetrics.incrementSamplesReceived(serviceId, mediaUnitId);
			logger.info("Session {} is opened", session.getId());
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
			this.exposedMetrics.incrementSamplesClosedWebsockets(serviceId, mediaUnitId);
		} catch (Throwable t) {
			logger.warn("MeterRegistry just caused an error by counting samples", t);
		}
		var input = this.inputs.get(session.getId());
		if (input == null) {
			var closeReason = this.customCloseReasons.getInternalServerError("The input does not exists");
			session.close(closeReason);
			return;
		}
		logger.info("\n\n\n {}", Base64.encode(messageBytes));
		try {
			input.acceptor.accept(messageBytes);
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
		final Acceptor acceptor;
		final WebSocketSession session;

		private Input(Acceptor acceptor, WebSocketSession session) {
			this.acceptor = acceptor;
			this.session = session;
		}
	}
}
