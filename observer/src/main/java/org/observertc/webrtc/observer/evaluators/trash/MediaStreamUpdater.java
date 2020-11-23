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
//package org.observertc.webrtc.observer.evaluators;
//
//import io.micronaut.context.annotation.Prototype;
//import io.micronaut.scheduling.TaskExecutors;
//import io.reactivex.Observable;
//import io.reactivex.Scheduler;
//import io.reactivex.schedulers.Schedulers;
//import java.time.Instant;
//import java.util.HashMap;
//import java.util.Iterator;
//import java.util.LinkedList;
//import java.util.List;
//import java.util.Map;
//import java.util.Observable;
//import java.util.Random;
//import java.util.UUID;
//import java.util.concurrent.ExecutorService;
//import java.util.concurrent.TimeUnit;
//import java.util.function.Supplier;
//import javax.inject.Named;
//import org.observertc.webrtc.common.UUIDAdapter;
//import org.observertc.webrtc.observer.EvaluatorsConfig;
//import org.observertc.webrtc.observer.dto.AbstractPeerConnectionSampleVisitor;
//import org.observertc.webrtc.observer.dto.PeerConnectionSampleVisitor;
//import org.observertc.webrtc.observer.dto.v20200114.PeerConnectionSample;
//import org.observertc.webrtc.observer.micrometer.MetricsReporter;
//import org.observertc.webrtc.observer.samples.ObservedPCS;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
///**
// * Peer connection handled by this instance of the observer
// */
//@Prototype
//public final class MediaStreamUpdater {
//	private static final Logger logger = LoggerFactory.getLogger(MediaStreamUpdater.class);
//
//	private final Map<UUID, MediaStreamUpdate> activePeerConnections = new HashMap<>();
//	private final Map<UUID, MediaStreamUpdate> passivePeerConnections = new HashMap<>();
//	private final PeerConnectionSampleVisitor<ObservedPCS> SSRCExtractor;
//	private final EvaluatorsConfig.CallCleanerConfig callCleanerConfig;
//	private final MetricsReporter metricsReporter;
//	private String name = "UNDEFINED";
//
//	public MediaStreamUpdater(
//			@Named(TaskExecutors.IO) ExecutorService executorService,
//			MetricsReporter metricsReporter,
//			ActiveStreamsEvaluator activeStreamsEvaluator,
//			ExpiredPCsConsumer expiredPCsConsumer,
//			EvaluatorsConfig.CallCleanerConfig callCleanerConfig
//	) {
//		Scheduler scheduler = Schedulers.from(executorService);
//		this.SSRCExtractor = this.makeSSRCExtractor();
//		this.callCleanerConfig = callCleanerConfig;
//		this.metricsReporter = metricsReporter;
//
//		Random random = new Random();
//		Supplier<Map<UUID, MediaStreamUpdate>> updateProvider = () -> this.evaluateActiveStreamUpdates();
//		Observable.just(updateProvider)
////				.delay(Single.defer(() -> 1000), TimeUnit.SECONDS)
//				.delay(random.nextInt(5000) + 10000, TimeUnit.MILLISECONDS)
//				.repeat()
//				.doOnError(throwable -> {
//					logger.error("Error occured", throwable);
//				})
//				.subscribeOn(scheduler)
//				.subscribe(updateP -> {
//					Map<UUID, MediaStreamUpdate> updates = updateP.get();
//					activeStreamsEvaluator.update(updates);
//				});
//
//		Supplier<List<byte[]>> detachedPCsProvider = () -> this.evaluatePassiveStreamUpdates();
//		Observable.just(detachedPCsProvider)
//				.delay(random.nextInt(10000) + 50000, TimeUnit.MILLISECONDS)
//				.repeat()
//				.doOnError(throwable -> {
//					logger.error("Error occured", throwable);
//				})
//				.subscribeOn(scheduler)
//				.subscribe(updateP -> {
//					List<byte[]> expiredPCs = updateP.get();
//					expiredPCsConsumer.processExpiredPCs(expiredPCs);
//				});
//	}
//
//	public void setName(String value) {
//		this.name = value;
//	}
//
//	public void add(ObservedPCS observedPCS) {
//		UUID peerConnectionUUID = observedPCS.peerConnectionUUID;
//		PeerConnectionSample pcSample = observedPCS.peerConnectionSample;
//		if (pcSample == null) {
//			logger.warn("Peer connection sample is null");
//			return;
//		}
//		synchronized (this) {
//			MediaStreamUpdate mediaStreamUpdate = this.activePeerConnections.get(peerConnectionUUID);
//			if (mediaStreamUpdate != null) {
//				mediaStreamUpdate.updated = observedPCS.timestamp;
//				return;
//			}
//			mediaStreamUpdate = this.passivePeerConnections.get(peerConnectionUUID);
//			if (mediaStreamUpdate != null) {
//				mediaStreamUpdate.updated = observedPCS.timestamp;
//				this.activePeerConnections.put(peerConnectionUUID, mediaStreamUpdate);
//				this.passivePeerConnections.remove(peerConnectionUUID);
//				return;
//			}
//			mediaStreamUpdate = MediaStreamUpdate.of(
//					observedPCS.serviceUUID,
//					observedPCS.peerConnectionUUID,
//					observedPCS.timestamp,
//					pcSample.browserId,
//					pcSample.callId,
//					observedPCS.timeZoneID,
//					pcSample.userId,
//					observedPCS.mediaUnitId,
//					observedPCS.serviceName,
//					observedPCS.marker
//			);
//			this.activePeerConnections.put(peerConnectionUUID, mediaStreamUpdate);
//			this.SSRCExtractor.accept(observedPCS, pcSample);
//		}
//	}
//
//
//	//	@Scheduled(initialDelay = "2m", fixedRate = "30s")
//	private Map<UUID, MediaStreamUpdate> evaluateActiveStreamUpdates() {
//		Map<UUID, MediaStreamUpdate> result = new HashMap<>();
//		synchronized (this) {
//			Iterator<Map.Entry<UUID, MediaStreamUpdate>> it = this.activePeerConnections.entrySet().iterator();
//			for (; it.hasNext(); ) {
//				Map.Entry<UUID, MediaStreamUpdate> entry = it.next();
//				result.put(entry.getKey(), entry.getValue());
//				this.passivePeerConnections.put(entry.getKey(), entry.getValue());
//				it.remove();
//			}
//		}
//		this.metricsReporter.gaugeActiveMediaStreams(this.name, result.size());
//		return result;
//	}
//
//	private List<byte[]> evaluatePassiveStreamUpdates() {
//		List<byte[]> result = new LinkedList<>();
//		long threshold = Instant.now().toEpochMilli() - TimeUnit.SECONDS.toMillis(callCleanerConfig.streamMaxIdleTimeInS);
//		synchronized (this) {
//			Iterator<Map.Entry<UUID, MediaStreamUpdate>> it = this.passivePeerConnections.entrySet().iterator();
//			for (; it.hasNext(); ) {
//				Map.Entry<UUID, MediaStreamUpdate> entry = it.next();
//				UUID peerConnectionUUID = entry.getKey();
//				MediaStreamUpdate mediaStreamUpdate = entry.getValue();
//				if (mediaStreamUpdate == null) {
//					logger.warn("Null mediastreamupdate");
//					it.remove();
//					continue;
//				}
//				if (threshold < mediaStreamUpdate.updated) {
//					continue;
//				}
//				byte[] pcUUIDHolder = UUIDAdapter.toBytesOrDefault(peerConnectionUUID, null);
//				if (pcUUIDHolder == null) {
//					logger.warn("NULL at converter");
//					continue;
//				}
//				result.add(pcUUIDHolder);
//				it.remove();
//			}
//		}
//		return result;
//	}
//
//	private PeerConnectionSampleVisitor<ObservedPCS> makeSSRCExtractor() {
//		return new AbstractPeerConnectionSampleVisitor<ObservedPCS>() {
//			@Override
//			public void visitRemoteInboundRTP(ObservedPCS obj, PeerConnectionSample sample, PeerConnectionSample.RemoteInboundRTPStreamStats subject) {
//				MediaStreamUpdate mediaStreamUpdate = activePeerConnections.get(obj.peerConnectionUUID);
//				if (mediaStreamUpdate != null) {
//					mediaStreamUpdate.SSRCs.add(subject.ssrc);
//				}
//			}
//
//			@Override
//			public void visitInboundRTP(ObservedPCS obj, PeerConnectionSample sample, PeerConnectionSample.InboundRTPStreamStats subject) {
//				MediaStreamUpdate mediaStreamUpdate = activePeerConnections.get(obj.peerConnectionUUID);
//				if (mediaStreamUpdate != null) {
//					mediaStreamUpdate.SSRCs.add(subject.ssrc);
//				}
//			}
//
//			@Override
//			public void visitOutboundRTP(ObservedPCS obj, PeerConnectionSample sample, PeerConnectionSample.OutboundRTPStreamStats subject) {
//				MediaStreamUpdate mediaStreamUpdate = activePeerConnections.get(obj.peerConnectionUUID);
//				if (mediaStreamUpdate != null) {
//					mediaStreamUpdate.SSRCs.add(subject.ssrc);
//				}
//			}
//		};
//	}
//
//
//}