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

import io.micronaut.test.annotation.MicronautTest;
import io.reactivex.rxjava3.core.Observable;
import java.time.Instant;
import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import javax.inject.Inject;
import org.jooq.lambda.tuple.Tuple2;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.observertc.webrtc.common.UUIDAdapter;
import org.observertc.webrtc.observer.evaluators.reportdrafts.FinishedCallReportDraft;
import org.observertc.webrtc.observer.evaluators.reportdrafts.ReportDraft;
import org.observertc.webrtc.observer.evaluators.reportdrafts.ReportDraftType;
import org.observertc.webrtc.observer.jooq.Tables;
import org.observertc.webrtc.observer.jooq.tables.records.PeerconnectionsRecord;
import org.observertc.webrtc.observer.repositories.DSLContextProvider;
import org.observertc.webrtc.schemas.reports.Report;
import org.observertc.webrtc.schemas.reports.ReportType;

//@Property(name = "hikari.jdbURL", value = "jdbc:h2:~/WebRTCObserver;MODE=MYSQL")
@MicronautTest
public class ExpiredPCsEvaluatorTest {

	@Inject
	ExpiredPCsEvaluator evaluator;

	@Inject
	DSLContextProvider contextProvider;

	@Test
	public void should_reportDetachedPC() {

		// Given
		AtomicReference<Tuple2<UUID, Report>> reportHolder = new AtomicReference<>();

		PeerconnectionsRecord expiredPC = new PeerconnectionsRecord(
				UUIDAdapter.toBytes(UUID.randomUUID()),
				UUIDAdapter.toBytes(UUID.randomUUID()),
				UUIDAdapter.toBytes(UUID.randomUUID()),
				Instant.EPOCH.toEpochMilli(),
				Instant.EPOCH.toEpochMilli(),
				null,
				"mediaUnitId",
				"browserId",
				"providedUserId",
				"providedCallId",
				"EET",
				"serviceName"
		);
		contextProvider.get().insertInto(Tables.PEERCONNECTIONS).set(expiredPC).execute();
		evaluator.getDetachedPeerConnections().subscribe(reportHolder::set);

		// When
		Observable
				.just(Arrays.asList(UUIDAdapter.toUUID(expiredPC.getPeerconnectionuuid())))
				.subscribe(evaluator);

		// Then
		Assertions.assertNotNull(reportHolder.get());
		Assertions.assertEquals(expiredPC.getPeerconnectionuuid(), UUIDAdapter.toBytes(reportHolder.get().v1));
		Report report = reportHolder.get().v2;
		Assertions.assertEquals(expiredPC.getServiceuuid().toString(), report.getServiceUUID());
		Assertions.assertEquals(expiredPC.getServicename(), report.getServiceName());
		Assertions.assertEquals(expiredPC.getUpdated(), report.getTimestamp());
		Assertions.assertEquals(null, report.getMarker());
		Assertions.assertEquals(ReportType.DETACHED_PEER_CONNECTION, report.getType());
	}

	@Test
	public void should_createFinishedCall() {

		// Given
		AtomicReference<ReportDraft> draftHolder = new AtomicReference<>();

		PeerconnectionsRecord expiredPC = new PeerconnectionsRecord(
				UUIDAdapter.toBytes(UUID.randomUUID()),
				UUIDAdapter.toBytes(UUID.randomUUID()),
				UUIDAdapter.toBytes(UUID.randomUUID()),
				Instant.EPOCH.toEpochMilli(),
				Instant.EPOCH.toEpochMilli() + 1,
				null,
				"mediaUnitId",
				"browserId",
				"providedUserId",
				"providedCallId",
				"EET",
				"serviceName"
		);
		contextProvider.get().insertInto(Tables.PEERCONNECTIONS).set(expiredPC).execute();
		evaluator.getFinishedCallSubject().subscribe(draftHolder::set);

		// When
		Observable
				.just(Arrays.asList(UUIDAdapter.toUUID(expiredPC.getPeerconnectionuuid())))
				.subscribe(evaluator);

		// Then
		Assertions.assertNotNull(draftHolder.get());
		ReportDraft reportDraft = draftHolder.get();
		Assertions.assertEquals(reportDraft.type, ReportDraftType.FINISHED_CALL);
		FinishedCallReportDraft finishedCallReportDraft = (FinishedCallReportDraft) reportDraft;
		Assertions.assertEquals(UUIDAdapter.toUUID(expiredPC.getCalluuid()), finishedCallReportDraft.callUUID);
		Assertions.assertEquals(UUIDAdapter.toUUID(expiredPC.getServiceuuid()), finishedCallReportDraft.serviceUUID);
		Assertions.assertEquals(expiredPC.getUpdated(), finishedCallReportDraft.finished);
		Assertions.assertEquals(null, finishedCallReportDraft.marker);
	}
}