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
import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import javax.inject.Inject;
import org.junit.jupiter.api.Test;
import org.observertc.webrtc.schemas.reports.Report;

//@Property(name = "hikari.jdbURL", value = "jdbc:h2:~/WebRTCObserver;MODE=MYSQL")
@MicronautTest
public class ActivePCsEvaluatorTest {

	@Inject
	ActivePCsEvaluator evaluator;

	@Test
	public void when_newMediaStreamAppears_then_joinedPCIsReported() {

		// Given
		AtomicReference<UUID> UUIDHolder = new AtomicReference<>(null);
		AtomicReference<Report> reportHolder = new AtomicReference<>(null);
		PCStateGenerator generator = PCStateGenerator.builder().build();
		PCState PCState = generator.getNext();
//		evaluator.getNewPeerConnectionsSubject().subscribe(t -> {
//			UUIDHolder.set(t.v1);
//			reportHolder.set(t.v2);
//		});

		// When
//		Observable
//				.just(Arrays.asList(PCState))
//				.subscribe(evaluator);

		// Then
//		Assertions.assertEquals(PCState.peerConnectionUUID, UUIDHolder.get());
//		Assertions.assertEquals(PCState.marker, reportHolder.get().getMarker());
//		Assertions.assertEquals(PCState.serviceName, reportHolder.get().getServiceName());
//		Assertions.assertEquals(PCState.serviceUUID.toString(), reportHolder.get().getServiceUUID());
//		Assertions.assertEquals(PCState.created, reportHolder.get().getTimestamp());
//		Assertions.assertEquals(ReportType.JOINED_PEER_CONNECTION, reportHolder.get().getType());
	}

	@Test
	public void when_newMediaStreamAppears_then_initiatedCallIsReported() {

		// Given
//		AtomicReference<ReportDraft> reportDraftHolder = new AtomicReference<>(null);
		PCStateGenerator generator = PCStateGenerator.builder().build();
		PCState PCState = generator.getNext();
//		evaluator.getInitiatedCallSubject().subscribe(reportDraftHolder::set);

		// When
//		Observable
//				.just(Arrays.asList(PCState))
//				.subscribe(evaluator);

		// Then
//		Assertions.assertEquals(ReportDraftType.INITIATED_CALL, reportDraftHolder.get().type);
//		InitiatedCallReportDraft initiatedCallReportDraft = (InitiatedCallReportDraft) reportDraftHolder.get();
//		Assertions.assertEquals(PCState.marker, initiatedCallReportDraft.marker);
//		Assertions.assertEquals(PCState.serviceUUID, initiatedCallReportDraft.serviceUUID);
//		Assertions.assertEquals(PCState.created, initiatedCallReportDraft.initiated);
//		Assertions.assertNotNull(initiatedCallReportDraft.callUUID);
	}


	@Test
	public void when_mediaUpdateFromSamePC_then_onlyOneJoinedPC_oneCallIsInitiated() {
		// Given
		AtomicInteger reportedJoinedPCNumHolder = new AtomicInteger(0);
		AtomicInteger createdInitiatedCallsHolder = new AtomicInteger(0);
		PCStateGenerator generator = PCStateGenerator.builder()
				.withPeerConnections(Arrays.asList(UUID.randomUUID()))
				.build();
//		evaluator.getJoinedPeerConnectionSubject().subscribe(i -> reportedJoinedPCNumHolder.getAndIncrement());
//		evaluator.getInitiatedCallSubject().subscribe(i -> createdInitiatedCallsHolder.getAndIncrement());

		// When

//		Observable
//				.fromCallable(generator::getNext)
//				.buffer(1)
//				.repeat(3)
//				.subscribe(evaluator);

		// Then
//		Assertions.assertEquals(1, reportedJoinedPCNumHolder.get());
//		Assertions.assertEquals(1, createdInitiatedCallsHolder.get());
	}
}