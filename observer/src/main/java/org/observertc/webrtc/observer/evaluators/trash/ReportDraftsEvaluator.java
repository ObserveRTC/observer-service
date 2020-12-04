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

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.subjects.PublishSubject;
import java.util.Iterator;
import java.util.UUID;
import javax.inject.Singleton;
import org.jooq.lambda.tuple.Tuple2;
import org.observertc.webrtc.common.UUIDAdapter;
import org.observertc.webrtc.observer.evaluators.trash.reportdrafts.AbstractReportDraftProcessor;
import org.observertc.webrtc.observer.evaluators.trash.reportdrafts.FinishedCallReportDraft;
import org.observertc.webrtc.observer.evaluators.trash.reportdrafts.InitiatedCallReportDraft;
import org.observertc.webrtc.observer.evaluators.trash.reportdrafts.ReportDraft;
import org.observertc.webrtc.observer.jooq.tables.records.PeerconnectionsRecord;
import org.observertc.webrtc.observer.repositories.mysql.PeerConnectionsRepository;
import org.observertc.webrtc.schemas.reports.FinishedCall;
import org.observertc.webrtc.schemas.reports.InitiatedCall;
import org.observertc.webrtc.schemas.reports.Report;
import org.observertc.webrtc.schemas.reports.ReportType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Deprecated
@Singleton
public class ReportDraftsEvaluator implements Observer<ReportDraft> {

	private static final Logger logger = LoggerFactory.getLogger(ReportDraftsEvaluator.class);
	private final PeerConnectionsRepository peerConnectionsRepository;
	private final AbstractReportDraftProcessor processor;
	private Disposable subscription;
	private final PublishSubject<Tuple2<UUID, Report>> reportSubject = PublishSubject.create();

	public ReportDraftsEvaluator(PeerConnectionsRepository peerConnectionsRepository) {
		this.peerConnectionsRepository = peerConnectionsRepository;
		this.processor = this.makeProcessor();
	}

	@Override
	public void onSubscribe(@NonNull Disposable d) {
		this.subscription = d;
	}

	@Override
	public void onNext(@NonNull ReportDraft reportDraft) {
		if (this.subscription != null && this.subscription.isDisposed()) {
			logger.warn("Report {} arrived after dispose.", reportDraft);
		}
		this.processor.accept(reportDraft);
	}

	@Override
	public void onError(@NonNull Throwable e) {
		logger.error("Error occured during process", e);
		this.subscription.dispose();
	}

	@Override
	public void onComplete() {
		logger.error("Process has been terminated");
		this.subscription.dispose();
	}

	private void evaluateInitiatedCallReport(InitiatedCallReportDraft initiatedCallReport) {
		PeerconnectionsRecord firstJoinedPC = null;
		Iterator<PeerconnectionsRecord> it = this.peerConnectionsRepository.findByCallUUID(initiatedCallReport.callUUID).iterator();
		for (; it.hasNext(); ) {
			PeerconnectionsRecord record = it.next();
			if (record.getJoined() == null) {
				logger.warn("The joined timestamp for the PC {} is null. This should have not been happened, there is no way we can use " +
						"now the joined timestamp anywhere.");
				continue;
			}
			if (firstJoinedPC == null) {
				firstJoinedPC = record;
			} else if (record.getJoined() < firstJoinedPC.getJoined()) {
				firstJoinedPC = record;
			}

		}
		if (firstJoinedPC == null) {
			logger.warn("No first joined PC is found. ReportDraft is dropped. {}", initiatedCallReport.toString());
			return;
		}
		//  || record.getJoined().compareTo(firstJoinedPC) < 0
		String callUUIDStr;
		if (firstJoinedPC.getCalluuid() != null) {
			callUUIDStr = UUIDAdapter.toUUID(firstJoinedPC.getCalluuid()).toString();
		} else {
			callUUIDStr = "NOT VALID UUID";
		}

		Object payload = InitiatedCall.newBuilder()
				.setCallUUID(callUUIDStr)
				.setCallName(firstJoinedPC.getProvidedcallid())
				.build();
		UUID serviceUUID = UUIDAdapter.toUUIDOrDefault(firstJoinedPC.getServiceuuid(), null);
		Report report = Report.newBuilder()
				.setServiceUUID(serviceUUID.toString())
				.setServiceName(firstJoinedPC.getServicename())
				.setMarker(initiatedCallReport.marker)
				.setType(ReportType.FINISHED_CALL)
				.setTimestamp(firstJoinedPC.getUpdated())
				.setPayload(payload)
				.build();
		Tuple2<UUID, Report> tuple = new Tuple2<>(serviceUUID, report);
		this.reportSubject.onNext(tuple);

	}

	private void evaluateFinishedCallReport(FinishedCallReportDraft finishedCallReport) {
		PeerconnectionsRecord lastDetachedPC = null;
		Iterator<PeerconnectionsRecord> it = this.peerConnectionsRepository.findByCallUUID(finishedCallReport.callUUID).iterator();
		for (; it.hasNext(); ) {
			PeerconnectionsRecord record = it.next();
			if (record.getDetached() == null) {
				continue;
			}
			if (lastDetachedPC == null) {
				lastDetachedPC = record;
			} else if (lastDetachedPC.getDetached() < record.getDetached()) {
				lastDetachedPC = record;
			}
		}
		if (lastDetachedPC == null) {
			logger.warn("No last detached PC is found. ReportDraft is dropped. {}", finishedCallReport.toString());
			return;
		}

		String callUUIDStr;
		if (lastDetachedPC.getCalluuid() != null) {
			callUUIDStr = UUIDAdapter.toUUID(lastDetachedPC.getCalluuid()).toString();
		} else {
			callUUIDStr = "NOT VALID UUID";
		}
		Object payload = FinishedCall.newBuilder()
				.setCallUUID(callUUIDStr)
				.setCallName(lastDetachedPC.getProvidedcallid())
				.build();

		UUID serviceUUID = UUIDAdapter.toUUIDOrDefault(lastDetachedPC.getServiceuuid(), null);
		Report report = Report.newBuilder()
				.setServiceUUID(serviceUUID.toString())
				.setServiceName(lastDetachedPC.getServicename())
				.setMarker(finishedCallReport.marker)
				.setType(ReportType.FINISHED_CALL)
				.setTimestamp(lastDetachedPC.getUpdated())
				.setPayload(payload)
				.build();
		Tuple2<UUID, Report> tuple = new Tuple2<>(serviceUUID, report);
		this.reportSubject.onNext(tuple);

	}

	private AbstractReportDraftProcessor makeProcessor() {
		return new AbstractReportDraftProcessor() {
			@Override
			public void processInitiatedCallReport(InitiatedCallReportDraft report) {
				evaluateInitiatedCallReport(report);
			}

			@Override
			public void processFinishedCallReport(FinishedCallReportDraft report) {
				evaluateFinishedCallReport(report);
			}
		};
	}


}
