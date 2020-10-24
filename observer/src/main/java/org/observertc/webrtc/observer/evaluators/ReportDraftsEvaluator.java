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

package org.observertc.webrtc.observer.evaluators;

import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import javax.inject.Singleton;
import org.observertc.webrtc.common.UUIDAdapter;
import org.observertc.webrtc.observer.EvaluatorsConfig;
import org.observertc.webrtc.observer.ReportSink;
import org.observertc.webrtc.observer.evaluators.reportdrafts.AbstractReportDraftProcessor;
import org.observertc.webrtc.observer.evaluators.reportdrafts.FinishedCallReportDraft;
import org.observertc.webrtc.observer.evaluators.reportdrafts.InitiatedCallReportDraft;
import org.observertc.webrtc.observer.evaluators.reportdrafts.ReportDraft;
import org.observertc.webrtc.observer.jooq.tables.records.PeerconnectionsRecord;
import org.observertc.webrtc.observer.repositories.PeerConnectionsRepository;
import org.observertc.webrtc.schemas.reports.FinishedCall;
import org.observertc.webrtc.schemas.reports.InitiatedCall;
import org.observertc.webrtc.schemas.reports.ReportType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class ReportDraftsEvaluator {

	private static final Logger logger = LoggerFactory.getLogger(ReportDraftsEvaluator.class);
	private final PeerConnectionsRepository peerConnectionsRepository;
	private final ReportSink reportSink;
	private final EvaluatorsConfig.ReportDraftsConfig config;
	private final AbstractReportDraftProcessor processor;
	private Queue<ReportDraft> reportDrafts = new LinkedList<>();

	public ReportDraftsEvaluator(PeerConnectionsRepository peerConnectionsRepository,
								 EvaluatorsConfig.ReportDraftsConfig config,
								 ReportSink reportSink) {
		this.peerConnectionsRepository = peerConnectionsRepository;
		this.reportSink = reportSink;
		this.config = config;
		this.processor = this.makeProcessor();

		Supplier<Queue<ReportDraft>> reportDraftsSupplier = () -> this.retrieve();
		Observable.just(reportDraftsSupplier)
				.delay(15000, TimeUnit.MILLISECONDS)
				.repeat()
				.doOnError(throwable -> {
					logger.error("Error occured", throwable);
				})
				.subscribeOn(Schedulers.io())
				.subscribe(updateP -> {
					Queue<ReportDraft> reportDrafts = updateP.get();
					process(reportDrafts);
				});
	}

	public void add(ReportDraft reportDraft) {
		synchronized (this) {
			this.reportDrafts.add(reportDraft);
		}
	}

	private Queue<ReportDraft> retrieve() {
		Queue<ReportDraft> result = new LinkedList<>();
		Instant now = Instant.now();
		synchronized (this) {
			for (int c = this.reportDrafts.size(); 0 < c; --c) {
				ReportDraft reportDraft = this.reportDrafts.poll();
				if (reportDraft.created == null) {
					reportDraft.created = now.toEpochMilli();
					logger.warn("ReportDraft was created without timestamp");
					this.reportDrafts.add(reportDraft);
					continue;
				}
				Instant created = Instant.ofEpochMilli(reportDraft.created);
				long elapsedSeconds = ChronoUnit.SECONDS.between(created, now);
				if (this.config.expirationTimeInS < elapsedSeconds) {
					logger.warn("ReportDraft {} is expired", reportDraft.toString());
					continue;
				}
				if (elapsedSeconds < this.config.minEnforcedTimeInS) {
					this.reportDrafts.add(reportDraft);
					continue;
				}
				result.add(reportDraft);
			}
		}
		return result;
	}

	private void process(Queue<ReportDraft> reportDrafts) {
		if (reportDrafts.size() < 1) {
			return;
		}
		while (!reportDrafts.isEmpty()) {
			ReportDraft reportDraft = reportDrafts.poll();
			if (reportDraft == null) {
				logger.warn("Null reportDraft or report occured in evaluation. skipping");
				continue;
			}
			this.processor.accept(reportDraft);
		}
	}

	private void evaluateInitiatedCallReport(InitiatedCallReportDraft initiatedCallReport) {
		PeerconnectionsRecord firstJoinedPC = null;
		Iterator<PeerconnectionsRecord> it = this.peerConnectionsRepository.findByCallUUID(initiatedCallReport.callUUID).iterator();
		for (; it.hasNext(); ) {
			PeerconnectionsRecord record = it.next();
			if (record.getDetached() != null) {
				continue;
			}
			if (record.getJoined() == null) {
				logger.warn("The joined timestamp for the PC {} is null. This should have not been happened, there is no way we can use " +
						"now the joined timestamp anywhere.");
				continue;
			}
			if (firstJoinedPC == null) {
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
		this.reportSink.sendReport(
				serviceUUID,
				serviceUUID,
				firstJoinedPC.getServicename(),
				initiatedCallReport.customProvided,
				ReportType.INITIATED_CALL,
				firstJoinedPC.getJoined(),
				payload
		);
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
		this.reportSink.sendReport(
				serviceUUID,
				serviceUUID,
				lastDetachedPC.getProvidedcallid(),
				lastDetachedPC.getServicename(),
				ReportType.FINISHED_CALL,
				lastDetachedPC.getUpdated(),
				payload
		);
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
