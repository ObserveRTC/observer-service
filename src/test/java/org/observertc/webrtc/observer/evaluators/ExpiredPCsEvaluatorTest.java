package org.observertc.webrtc.observer.evaluators;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.observertc.webrtc.observer.models.CallEntity;
import org.observertc.webrtc.observer.models.PeerConnectionEntity;
import org.observertc.webrtc.observer.models.SynchronizationSourceEntity;
import org.observertc.webrtc.observer.repositories.hazelcast.*;
import org.observertc.webrtc.schemas.reports.Report;
import org.observertc.webrtc.schemas.reports.ReportType;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.*;

@MicronautTest
class ExpiredPCsEvaluatorTest {

    static TestInputsGenerator generator = TestInputsGenerator.builder().build();

    @Inject
    Provider<ExpiredPCsEvaluator> subject;

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
    public void shouldRemovePeerConnection() {
        // Given
        ExpiredPCsEvaluator evaluator = subject.get();
        SynchronizationSourceEntity ssrcEntity = generator.makeSynchronizationSourceEntity();
        PeerConnectionEntity alice = generator.makePeerConnectionEntityFor(ssrcEntity);
        PeerConnectionEntity bob = generator.makePeerConnectionEntityFor(ssrcEntity);
        CallEntity callEntity = generator.makeCallEntityFor(alice, bob);
        PCState pcState = generator.makePCStateFor(alice, ssrcEntity);
        this.peerConnectionsRepository.save(alice.peerConnectionUUID, alice);
        this.peerConnectionsRepository.save(bob.peerConnectionUUID, bob);
        String ssrcKey = SynchronizationSourcesRepository.getKey(ssrcEntity.serviceUUID, ssrcEntity.SSRC);
        this.synchronizationSourcesRepository.save(ssrcKey, ssrcEntity);
        this.callPeerConnectionsRepository.addAll(ssrcEntity.callUUID, List.of(alice.peerConnectionUUID, bob.peerConnectionUUID));
        this.callEntitiesRepository.add(callEntity.callUUID, callEntity);
        List<Report> reports = new LinkedList<>();
        evaluator.observableReports().subscribe(reports::add);

        // When
        evaluator.onNext(Map.of(pcState.peerConnectionUUID, pcState));

        // Then
        Assertions.assertFalse(this.peerConnectionsRepository.exists(alice.peerConnectionUUID));
        Assertions.assertTrue(this.peerConnectionsRepository.exists(bob.peerConnectionUUID));
        Assertions.assertTrue(this.synchronizationSourcesRepository.exists(ssrcKey));
        Assertions.assertEquals(1, reports.stream().filter(r -> r.getType().equals(ReportType.DETACHED_PEER_CONNECTION)).count());
        Assertions.assertEquals(0, reports.stream().filter(r -> r.getType().equals(ReportType.FINISHED_CALL)).count());
        Collection<UUID> remainingPCs = this.callPeerConnectionsRepository.find(ssrcEntity.callUUID);
        Assertions.assertEquals(1, remainingPCs.size());
        Assertions.assertTrue(remainingPCs.contains(bob.peerConnectionUUID));
        Assertions.assertTrue(this.callEntitiesRepository.exists(callEntity.callUUID));
    }

    @Test
    public void shouldRemoveCall() {
        // Given
        ExpiredPCsEvaluator evaluator = subject.get();
        SynchronizationSourceEntity ssrcEntity = generator.makeSynchronizationSourceEntity();
        PeerConnectionEntity alice = generator.makePeerConnectionEntityFor(ssrcEntity);
        CallEntity callEntity = generator.makeCallEntityFor(alice);
        PCState pcState = generator.makePCStateFor(alice, ssrcEntity);
        this.peerConnectionsRepository.save(alice.peerConnectionUUID, alice);
        String ssrcKey = SynchronizationSourcesRepository.getKey(ssrcEntity.serviceUUID, ssrcEntity.SSRC);
        this.synchronizationSourcesRepository.save(ssrcKey, ssrcEntity);
        this.callPeerConnectionsRepository.addAll(ssrcEntity.callUUID, List.of(alice.peerConnectionUUID));
        this.callEntitiesRepository.add(callEntity.callUUID, callEntity);
        this.callSynchronizationSourcesRepository.add(callEntity.callUUID, ssrcKey);
        List<Report> reports = new LinkedList<>();
        evaluator.observableReports().subscribe(reports::add);

        // When
        evaluator.onNext(Map.of(pcState.peerConnectionUUID, pcState));

        // Then
        Assertions.assertFalse(this.peerConnectionsRepository.exists(alice.peerConnectionUUID));
        Assertions.assertFalse(this.synchronizationSourcesRepository.exists(ssrcKey));
        Assertions.assertEquals(1, reports.stream().filter(r -> r.getType().equals(ReportType.DETACHED_PEER_CONNECTION)).count());
        Assertions.assertEquals(1, reports.stream().filter(r -> r.getType().equals(ReportType.FINISHED_CALL)).count());
        Assertions.assertEquals(0, this.callPeerConnectionsRepository.find(alice.callUUID).size());
        Assertions.assertFalse(this.callEntitiesRepository.exists(callEntity.callUUID));
    }
}