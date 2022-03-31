package org.observertc.observer.repositories;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.observertc.observer.dto.*;
import org.observertc.observer.utils.DTOGenerators;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@MicronautTest
class RepositoryEventsTest {

    @Inject
    HazelcastMaps hazelcastMaps;

    @Inject
    RepositoryEvents repositoryEvents;

    DTOGenerators dtoGenerators = new DTOGenerators();

    @Test
    @DisplayName("When callDTO is added and removed Then corresponding events are triggered")
    void test_1() throws ExecutionException, InterruptedException, TimeoutException {
        var subject = dtoGenerators.getCallDTO();
        var added = new CompletableFuture<List<CallDTO>>();
        var removed = new CompletableFuture<List<CallDTO>>();

        repositoryEvents.addedCalls().subscribe(added::complete);
        repositoryEvents.removedCalls().subscribe(removed::complete);

        this.hazelcastMaps.getCalls().put(subject.callId, subject);
        this.hazelcastMaps.getCalls().remove(subject.callId);
        CompletableFuture.allOf(added, removed).get(30, TimeUnit.SECONDS);
    }

    @Test
    @DisplayName("When clientDTO is added and removed Then corresponding events are triggered")
    void test_2() throws ExecutionException, InterruptedException, TimeoutException {
        var subject = dtoGenerators.getClientDTO();
        var added = new CompletableFuture<List<ClientDTO>>();
        var removed = new CompletableFuture<List<ClientDTO>>();

        repositoryEvents.addedClients().subscribe(added::complete);
        repositoryEvents.removedClients().subscribe(removed::complete);

        this.hazelcastMaps.getClients().put(subject.clientId, subject);
        this.hazelcastMaps.getClients().remove(subject.clientId);
        CompletableFuture.allOf(added, removed).get(30, TimeUnit.SECONDS);
    }

    @Test
    @DisplayName("When clientDTO is added and waited to expire Then corresponding events are triggered")
    void test_3() throws ExecutionException, InterruptedException, TimeoutException {
        var subject = dtoGenerators.getClientDTO();
        var expired = new CompletableFuture<List<RepositoryExpiredEvent<ClientDTO>>>();

        repositoryEvents.expiredClients().subscribe(expired::complete);

        this.hazelcastMaps.getClients().put(subject.clientId, subject, 100, TimeUnit.MILLISECONDS);
        CompletableFuture.allOf(expired).get(30, TimeUnit.SECONDS);
    }


    @Test
    @DisplayName("When SfuStreamDTO is added and removed Then corresponding events are triggered")
    void test_4() throws ExecutionException, InterruptedException, TimeoutException {
        var subject = dtoGenerators.getSfuStreamDTO();
        var added = new CompletableFuture<List<SfuStreamDTO>>();
        var removed = new CompletableFuture<List<SfuStreamDTO>>();

        repositoryEvents.addedSfuStreams().subscribe(added::complete);
        repositoryEvents.removedSfuStreams().subscribe(removed::complete);

        this.hazelcastMaps.getSfuStreams().put(subject.sfuStreamId, subject);
        this.hazelcastMaps.getSfuStreams().remove(subject.sfuStreamId);
        CompletableFuture.allOf(added, removed).get(30, TimeUnit.SECONDS);
    }

    @Test
    @DisplayName("When SfuStreamDTO is added and waited to expire Then corresponding events are triggered")
    void test_5() throws ExecutionException, InterruptedException, TimeoutException {
        var subject = dtoGenerators.getSfuStreamDTO();
        var updated = new CompletableFuture<List<RepositoryUpdatedEvent<SfuStreamDTO>>>();

        this.hazelcastMaps.getSfuStreams().put(subject.sfuStreamId, subject);
        repositoryEvents.updatedSfuStreams().subscribe(updated::complete);
        subject.peerConnectionId = UUID.randomUUID();
        this.hazelcastMaps.getSfuStreams().put(subject.sfuStreamId, subject);

        CompletableFuture.allOf(updated).get(30, TimeUnit.SECONDS);
    }

    @Test
    @DisplayName("When SfuSinkDTO is added and removed Then corresponding events are triggered")
    void test_6() throws ExecutionException, InterruptedException, TimeoutException {
        var subject = dtoGenerators.getSfuSinkDTO();
        var added = new CompletableFuture<List<SfuSinkDTO>>();
        var removed = new CompletableFuture<List<SfuSinkDTO>>();

        repositoryEvents.addedSfuSinks().subscribe(added::complete);
        repositoryEvents.removedSfuSinks().subscribe(removed::complete);

        this.hazelcastMaps.getSfuSinks().put(subject.sfuSinkId, subject);
        this.hazelcastMaps.getSfuSinks().remove(subject.sfuSinkId);
        CompletableFuture.allOf(added, removed).get(30, TimeUnit.SECONDS);
    }

    @Test
    @DisplayName("When SfuSinkDTO is added and waited to expire Then corresponding events are triggered")
    void test_7() throws ExecutionException, InterruptedException, TimeoutException {
        var subject = dtoGenerators.getSfuSinkDTO();
        var updated = new CompletableFuture<List<RepositoryUpdatedEvent<SfuSinkDTO>>>();

        this.hazelcastMaps.getSfuSinks().put(subject.sfuStreamId, subject);
        repositoryEvents.updatedSuSinks().subscribe(updated::complete);
        subject.peerConnectionId = UUID.randomUUID();
        this.hazelcastMaps.getSfuSinks().put(subject.sfuStreamId, subject);

        CompletableFuture.allOf(updated).get(30, TimeUnit.SECONDS);
    }


    @Test
    @DisplayName("When PeerConnectionDTO is added and removed Then corresponding events are triggered")
    void test_8() throws ExecutionException, InterruptedException, TimeoutException {
        var subject = dtoGenerators.getPeerConnectionDTO();
        var added = new CompletableFuture<List<PeerConnectionDTO>>();
        var removed = new CompletableFuture<List<PeerConnectionDTO>>();

        repositoryEvents.addedPeerConnection().subscribe(added::complete);
        repositoryEvents.removedPeerConnection().subscribe(removed::complete);

        this.hazelcastMaps.getPeerConnections().put(subject.peerConnectionId, subject);
        this.hazelcastMaps.getPeerConnections().remove(subject.peerConnectionId);
        CompletableFuture.allOf(added, removed).get(30, TimeUnit.SECONDS);
    }

    @Test
    @DisplayName("When PeerConnectionDTO is added and waited to expire Then corresponding events are triggered")
    void test_9() throws ExecutionException, InterruptedException, TimeoutException {
        var subject = dtoGenerators.getPeerConnectionDTO();
        var expired = new CompletableFuture<List<RepositoryExpiredEvent<PeerConnectionDTO>>>();

        repositoryEvents.expiredPeerConnection().subscribe(expired::complete);

        this.hazelcastMaps.getPeerConnections().put(subject.peerConnectionId, subject, 100, TimeUnit.MILLISECONDS);
        CompletableFuture.allOf(expired).get(30, TimeUnit.SECONDS);
    }

    @Test
    @DisplayName("When PeerConnectionDTO is added and removed Then corresponding events are triggered")
    void test_10() throws ExecutionException, InterruptedException, TimeoutException {
        var subject = dtoGenerators.getMediaTrackDTO();
        var added = new CompletableFuture<List<MediaTrackDTO>>();
        var removed = new CompletableFuture<List<MediaTrackDTO>>();

        repositoryEvents.addedMediaTracks().subscribe(added::complete);
        repositoryEvents.removedMediaTracks().subscribe(removed::complete);

        this.hazelcastMaps.getMediaTracks().put(subject.trackId, subject);
        this.hazelcastMaps.getMediaTracks().remove(subject.trackId);
        CompletableFuture.allOf(added, removed).get(30, TimeUnit.SECONDS);
    }

    @Test
    @DisplayName("When PeerConnectionDTO is added and waited to expire Then corresponding events are triggered")
    void test_11() throws ExecutionException, InterruptedException, TimeoutException {
        var subject = dtoGenerators.getMediaTrackDTO();
        var expired = new CompletableFuture<List<RepositoryExpiredEvent<MediaTrackDTO>>>();

        repositoryEvents.expiredMediaTracks().subscribe(expired::complete);

        this.hazelcastMaps.getMediaTracks().put(subject.trackId, subject, 100, TimeUnit.MILLISECONDS);
        CompletableFuture.allOf(expired).get(30, TimeUnit.SECONDS);
    }


    @Test
    @DisplayName("When SfuRtpPad is added, changed or removed Then corresponding events are triggered")
    void test_12() throws ExecutionException, InterruptedException, TimeoutException {
        var subject = dtoGenerators.getSfuRtpPadDTO();
        var added = new CompletableFuture<List<SfuRtpPadDTO>>();
        var updated = new CompletableFuture<List<RepositoryUpdatedEvent<SfuRtpPadDTO>>>();
        var removed = new CompletableFuture<List<SfuRtpPadDTO>>();


        repositoryEvents.addedSfuRtpPads().subscribe(added::complete);
        repositoryEvents.updatedSfuRtpPads().subscribe(updated::complete);
        repositoryEvents.removedSfuRtpPads().subscribe(removed::complete);

        this.hazelcastMaps.getSFURtpPads().put(subject.rtpPadId, subject);
        subject.added = Instant.now().getEpochSecond();
        this.hazelcastMaps.getSFURtpPads().put(subject.rtpPadId, subject);
        this.hazelcastMaps.getSFURtpPads().remove(subject.rtpPadId);
        CompletableFuture.allOf(added, updated, removed).get(30, TimeUnit.SECONDS);
    }

    @Test
    @DisplayName("When SfuRtpPad is added and waited to expire Then corresponding events are triggered")
    void test_13() throws ExecutionException, InterruptedException, TimeoutException {
        var subject = dtoGenerators.getSfuRtpPadDTO();
        var expired = new CompletableFuture<List<RepositoryExpiredEvent<SfuRtpPadDTO>>>();

        repositoryEvents.expiredSfuRtpPads().subscribe(expired::complete);

        this.hazelcastMaps.getSFURtpPads().put(subject.rtpPadId, subject, 100, TimeUnit.MILLISECONDS);
        CompletableFuture.allOf(expired).get(30, TimeUnit.SECONDS);
    }

    @Test
    @DisplayName("When SfuTransport is added, changed or removed Then corresponding events are triggered")
    void test_14() throws ExecutionException, InterruptedException, TimeoutException {
        var subject = dtoGenerators.getSfuTransportDTO();
        var added = new CompletableFuture<List<SfuTransportDTO>>();
        var updated = new CompletableFuture<List<RepositoryUpdatedEvent<SfuTransportDTO>>>();
        var removed = new CompletableFuture<List<SfuTransportDTO>>();


        repositoryEvents.addedSfuTransports().subscribe(added::complete);
        repositoryEvents.updatedSfuTransports().subscribe(updated::complete);
        repositoryEvents.removedSfuTransports().subscribe(removed::complete);

        this.hazelcastMaps.getSFUTransports().put(subject.transportId, subject);
        subject.opened = Instant.now().getEpochSecond();
        this.hazelcastMaps.getSFUTransports().put(subject.transportId, subject);
        this.hazelcastMaps.getSFUTransports().remove(subject.transportId);
        CompletableFuture.allOf(added, updated, removed).get(30, TimeUnit.SECONDS);
    }

    @Test
    @DisplayName("When SfuTransport is added and waited to expire Then corresponding events are triggered")
    void test_15() throws ExecutionException, InterruptedException, TimeoutException {
        var subject = dtoGenerators.getSfuTransportDTO();
        var expired = new CompletableFuture<List<RepositoryExpiredEvent<SfuTransportDTO>>>();

        repositoryEvents.expiredSfuTransports().subscribe(expired::complete);

        this.hazelcastMaps.getSFUTransports().put(subject.transportId, subject, 100, TimeUnit.MILLISECONDS);
        CompletableFuture.allOf(expired).get(30, TimeUnit.SECONDS);
    }


    @Test
    @DisplayName("When SfuDTO is added, changed or removed Then corresponding events are triggered")
    void test_16() throws ExecutionException, InterruptedException, TimeoutException {
        var subject = dtoGenerators.getSfuDTO();
        var added = new CompletableFuture<List<SfuDTO>>();
        var removed = new CompletableFuture<List<SfuDTO>>();


        repositoryEvents.addedSfu().subscribe(added::complete);
        repositoryEvents.removedSfu().subscribe(removed::complete);

        this.hazelcastMaps.getSFUs().put(subject.sfuId, subject);
        subject.joined = Instant.now().getEpochSecond();
        this.hazelcastMaps.getSFUs().put(subject.sfuId, subject);
        this.hazelcastMaps.getSFUs().remove(subject.sfuId);
        CompletableFuture.allOf(added, removed).get(30, TimeUnit.SECONDS);
    }

    @Test
    @DisplayName("When SfuDTO is added and waited to expire Then corresponding events are triggered")
    void test_17() throws ExecutionException, InterruptedException, TimeoutException {
        var subject = dtoGenerators.getSfuDTO();
        var expired = new CompletableFuture<List<RepositoryExpiredEvent<SfuDTO>>>();

        repositoryEvents.expiredSfu().subscribe(expired::complete);

        this.hazelcastMaps.getSFUs().put(subject.sfuId, subject, 100, TimeUnit.MILLISECONDS);
        CompletableFuture.allOf(expired).get(30, TimeUnit.SECONDS);
    }
}