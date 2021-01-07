package org.observertc.webrtc.observer.evaluators;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.observertc.webrtc.observer.ReportRecord;
import org.observertc.webrtc.observer.models.CallEntity;
import org.observertc.webrtc.observer.models.PeerConnectionEntity;
import org.observertc.webrtc.observer.models.SynchronizationSourceEntity;
import org.observertc.webrtc.observer.repositories.hazelcast.*;
import org.observertc.webrtc.schemas.reports.ReportType;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@MicronautTest
class NewPCsEvaluatorTest {

    static TestInputsGenerator generator = new TestInputsGenerator();

    @Inject
    Provider<NewPCEvaluator> subject;

    @Inject
    PeerConnectionsRepository peerConnectionsRepository;

    @Inject
    SynchronizationSourcesRepository synchronizationSourcesRepository;

    @Inject
    CallPeerConnectionsRepository callPeerConnectionsRepository;

    @Inject
    CallEntitiesRepository callEntitiesRepository;

    @Inject
    CallSynchronizationSourcesRepository callSynchronizationSourcesRepository;

    @Test
    public void shouldAddPeerConnection() {
        // Given
        NewPCEvaluator evaluator = subject.get();
        SynchronizationSourceEntity ssrcEntity = generator.makeSynchronizationSourceEntity();
        PeerConnectionEntity alice = generator.makePeerConnectionEntityFor(ssrcEntity);
        PeerConnectionEntity bob = generator.makePeerConnectionEntityFor(ssrcEntity);
        CallEntity callEntity = generator.makeCallEntityFor(alice, bob);
        PCState pcState = generator.makePCStateFor(alice, ssrcEntity);
        this.peerConnectionsRepository.save(alice.peerConnectionUUID, alice);
        String ssrcKey = SynchronizationSourcesRepository.getKey(ssrcEntity.serviceUUID, ssrcEntity.SSRC);
        this.synchronizationSourcesRepository.save(ssrcKey, ssrcEntity);
        this.callPeerConnectionsRepository.addAll(ssrcEntity.callUUID, List.of(alice.peerConnectionUUID, bob.peerConnectionUUID));
        this.callEntitiesRepository.add(callEntity.callUUID, callEntity);
        List<ReportRecord> reports = new LinkedList<>();
        evaluator.getReports().subscribe(reports::add);

        // When
        evaluator.onNext(Map.of(pcState.peerConnectionUUID, pcState));

        // Then
        Assertions.assertEquals(1, reports.stream().filter(r -> r.value.getType().equals(ReportType.JOINED_PEER_CONNECTION)).count());
        Assertions.assertEquals(0, reports.stream().filter(r -> r.value.getType().equals(ReportType.INITIATED_CALL)).count());
    }
}