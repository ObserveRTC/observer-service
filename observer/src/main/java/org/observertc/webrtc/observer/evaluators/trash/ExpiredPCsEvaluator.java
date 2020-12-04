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

package org.observertc.webrtc.observer.evaluators.trash;

import io.micronaut.context.annotation.Prototype;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.subjects.PublishSubject;
import io.reactivex.rxjava3.subjects.Subject;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import org.jooq.lambda.tuple.Tuple2;
import org.observertc.webrtc.common.UUIDAdapter;
import org.observertc.webrtc.observer.evaluators.trash.reportdrafts.FinishedCallReportDraft;
import org.observertc.webrtc.observer.evaluators.trash.reportdrafts.ReportDraft;
import org.observertc.webrtc.observer.jooq.tables.records.PeerconnectionsRecord;
import org.observertc.webrtc.observer.micrometer.MetricsReporter;
import org.observertc.webrtc.observer.repositories.mysql.ActiveStreamsRepository;
import org.observertc.webrtc.observer.repositories.mysql.PeerConnectionsRepository;
import org.observertc.webrtc.schemas.reports.DetachedPeerConnection;
import org.observertc.webrtc.schemas.reports.Report;
import org.observertc.webrtc.schemas.reports.ReportType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Prototype
@Deprecated
public class ExpiredPCsEvaluator implements Observer<List<UUID>> {

	private static final Logger logger = LoggerFactory.getLogger(ExpiredPCsEvaluator.class);
	private final PublishSubject<Tuple2<UUID, Report>> detachedPeerConnections = PublishSubject.create();
	private final PublishSubject<ReportDraft> finishedCalls = PublishSubject.create();

	private final ActiveStreamsRepository activeStreamsRepository;
	private final PeerConnectionsRepository peerConnectionsRepository;
	private final MetricsReporter metricsReporter;

	public ExpiredPCsEvaluator(
			ActiveStreamsRepository activeStreamsRepository,
			PeerConnectionsRepository peerConnectionsRepository,
			MetricsReporter metricsReporter) {
		this.activeStreamsRepository = activeStreamsRepository;
		this.peerConnectionsRepository = peerConnectionsRepository;
		this.metricsReporter = metricsReporter;
	}

	public Subject<ReportDraft> getFinishedCallSubject() {
		return this.finishedCalls;
	}

	public Subject<Tuple2<UUID, Report>> getDetachedPeerConnections() {
		return this.detachedPeerConnections;
	}

	@Override
	public void onSubscribe(@NonNull Disposable d) {

	}

	@Override
	public void onNext(@NonNull List<UUID> uuids) {
		if (uuids.size() < 1) {
			return;
		}
		List<byte[]> uuidsInBytes = uuids.stream()
				.filter(Objects::nonNull)
				.map(UUIDAdapter::toBytes)
				.collect(Collectors.toList());

		try {
			this.processExpiredPCs(uuidsInBytes);
		} catch (Exception ex) {
			logger.error("Error occurred", ex);
		}
	}

	@Override
	public void onError(@NonNull Throwable e) {

	}

	@Override
	public void onComplete() {

	}

	private void processExpiredPCs(List<byte[]> peerConnectionUUIDs) {
		if (peerConnectionUUIDs.size() < 1) {
			return;
		}
		Iterator<PeerconnectionsRecord> it = this.peerConnectionsRepository.findAll(peerConnectionUUIDs).iterator();
		for (; it.hasNext(); ) {
			PeerconnectionsRecord record = it.next();
			record.setDetached(record.getUpdated());
			record.setUpdated(record.getUpdated());
			record.store();

			UUID serviceUUID = UUIDAdapter.toUUID(record.getServiceuuid());
			String serviceName = record.getServicename();
			UUID callUUID = UUIDAdapter.toUUID(record.getCalluuid());
			UUID peerConnectionUUID = UUIDAdapter.toUUID(record.getPeerconnectionuuid());

			Object payload = DetachedPeerConnection.newBuilder()
					.setMediaUnitId(record.getMediaunitid())
					.setCallName(record.getProvidedcallid())
					.setCallUUID(callUUID.toString())
					.setUserId(record.getProvideduserid())
					.setBrowserId(record.getBrowserid())
					.setPeerConnectionUUID(peerConnectionUUID.toString())
					.build();

			Report report = Report.newBuilder()
					.setServiceUUID(serviceUUID.toString())
					.setServiceName(serviceName)
					.setMarker(null)
					.setType(ReportType.DETACHED_PEER_CONNECTION)
					.setTimestamp(record.getUpdated())
					.setPayload(payload)
					.build();
			Tuple2<UUID, Report> tuple = new Tuple2<>(peerConnectionUUID, report);

			detachedPeerConnections.onNext(tuple);

			Optional<PeerconnectionsRecord> joinedPCHolder =
					this.peerConnectionsRepository.findJoinedPCsByCallUUIDBytes(record.getCalluuid()).findFirst();

			if (joinedPCHolder.isPresent()) {
				continue;
			}

			//finished call
			FinishedCallReportDraft reportDraft = FinishedCallReportDraft.of(serviceUUID, callUUID, null, record.getUpdated());
			this.finishedCalls.onNext(reportDraft);
			this.activeStreamsRepository.deleteByCallUUIDBytes(record.getCalluuid());
		}
	}


}
