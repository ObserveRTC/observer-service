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
import io.micronaut.websocket.CloseReason;
import io.micronaut.websocket.WebSocketSession;
import io.micronaut.websocket.annotation.OnClose;
import io.micronaut.websocket.annotation.OnMessage;
import io.micronaut.websocket.annotation.OnOpen;
import io.micronaut.websocket.annotation.ServerWebSocket;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Observer;
import org.observertc.webrtc.observer.common.UUIDAdapter;
import org.observertc.webrtc.observer.configs.ObserverConfigDispatcher;
import org.observertc.webrtc.observer.dto.pcsamples.v20200114.PeerConnectionSample;
import org.observertc.webrtc.observer.monitors.FlawMonitor;
import org.observertc.webrtc.observer.monitors.MonitorProvider;
import org.observertc.webrtc.observer.samples.SourceSample;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;

import javax.inject.Inject;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Service should be UUId, because currently mysql stores it as
 * binary and with that type the search is fast for activestreams. thats why.
 */
@Secured(SecurityRule.IS_ANONYMOUS)
@ServerWebSocket("/pcsamples/{serviceUUIDStr}/{mediaUnitID}/")
public class WebsocketPCSamples extends Observable<SourceSample> {

	private static final Logger logger = LoggerFactory.getLogger(WebsocketPCSamples.class);
	private final ObjectReader objectReader;
	private final FlawMonitor flawMonitor;

	@Inject
	MeterRegistry meterRegistry;

	private Observer<? super SourceSample> observer = null;

	public WebsocketPCSamples(
			ObjectMapper objectMapper,
			MonitorProvider monitorProvider
	) {
		this.objectReader = objectMapper.reader();
		this.flawMonitor = monitorProvider.makeFlawMonitorFor(this.getClass());

	}

	@Override
	protected void subscribeActual(@NonNull Observer<? super SourceSample> observer) {
		this.observer = observer;
	}

	@OnOpen
	public void onOpen(String serviceUUIDStr, String mediaUnitID, WebSocketSession session) {
		try {
			if (Objects.isNull(this.observer)) {
				// This observer is not ready
				session.close(CloseReason.TRY_AGAIN_LATER);
				return;
			}
			this.meterRegistry.counter(
					"observertc_pcsamples_opened_websockets"
			).increment();

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
		SourceSample.Builder sourceSampleBuilder = new SourceSample.Builder()
				.withServiceUUID(serviceUUID)
				.withMediaUnitId(mediaUnitID)
				.withSample(sample);

		if (Objects.isNull(sample.peerConnectionId)) {
			if (Objects.nonNull(sample.userMediaErrors)) {
				this.observer.onNext(
						sourceSampleBuilder.build()
				);
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
		sourceSampleBuilder.withPeerConnectionUUID(peerConnectionUUIDHolder.get());

		try {
			this.observer.onNext(
					sourceSampleBuilder.build()
			);
		} catch (Exception ex) {
			this.flawMonitor.makeLogEntry()
					.withLogger(logger)
					.withException(ex)
					.withLogLevel(Level.WARN)
					.withMessage("Error occured processing message by {} ", this.getClass().getSimpleName())
					.complete();
		}
	}
}
