package org.observertc.observer.repositories;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.observertc.observer.utils.ModelsMapGenerator;
import org.observertc.schemas.dtos.Models;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@MicronautTest
class RepositoryEventsTest {

    @Inject
    HamokStorages hamokStorages;

    @Inject
    RepositoryEvents repositoryEvents;

    ModelsMapGenerator modelsMapGenerator = new ModelsMapGenerator();

//    ModelsGenerator modelsGenerator = new ModelsGenerator();

    @Test
    @DisplayName("Scenario: a p2p is added to the hamok. When a call is removed Then corresponding clients are removed as well and events are triggered")
    void test_1() throws ExecutionException, InterruptedException, TimeoutException {
        modelsMapGenerator.generateP2pCase().saveTo(hamokStorages);

        var addedClientModels = modelsMapGenerator.getClientModels();
        var callId = modelsMapGenerator.getCallModel().getCallId();
        var promise = new CompletableFuture<List<Models.Client>>();

        repositoryEvents.deletedClients().subscribe(promise::complete);
        this.hamokStorages.getCallsRepository().removeAll(Set.of(callId));
        var deletedClientModels = promise.get(30, TimeUnit.SECONDS);

        Assertions.assertEquals(addedClientModels.size(), deletedClientModels.size());
        for (var deletedClient : deletedClientModels) {
            var addedClient = addedClientModels.get(deletedClient.getClientId());
            Assertions.assertNotNull(addedClient);
        }
    }

    @Test
    @DisplayName("Scenario: a p2p is added to the hamok. When a call is removed Then corresponding peer connections are removed as well and events are triggered")
    void test_2() throws ExecutionException, InterruptedException, TimeoutException {
        modelsMapGenerator.generateP2pCase().saveTo(hamokStorages);

        var addedPeerConnectionModels = modelsMapGenerator.getPeerConnectionModels();
        var callId = modelsMapGenerator.getCallModel().getCallId();
        var promise = new CompletableFuture<List<Models.PeerConnection>>();
        repositoryEvents.deletedPeerConnections().subscribe(promise::complete);
//
        this.hamokStorages.getCallsRepository().removeAll(Set.of(callId));
        var deletedPeerConnectionModels = promise.get(30, TimeUnit.SECONDS);

        Assertions.assertEquals(addedPeerConnectionModels.size(), deletedPeerConnectionModels.size());
        for (var deletedClient : deletedPeerConnectionModels) {
            var deletedPeerConnection = addedPeerConnectionModels.get(deletedClient.getPeerConnectionId());
            Assertions.assertNotNull(deletedPeerConnection);
        }
    }

    @Test
    @DisplayName("Scenario: a p2p is added to the hamok. When a call is removed Then corresponding inbound tracks are removed as well and events are triggered")
    void test_3() throws ExecutionException, InterruptedException, TimeoutException {
        modelsMapGenerator.generateP2pCase().saveTo(hamokStorages);
        var callId = modelsMapGenerator.getCallModel().getCallId();
        var promise = new CompletableFuture<List<Models.InboundTrack>>();
        repositoryEvents.deletedInboundTrack().subscribe(promise::complete);
//
        this.hamokStorages.getCallsRepository().removeAll(Set.of(callId));
        var addedInboundTrackModels = modelsMapGenerator.getInboundTrackModels();
        var deletedInboundTrackModels = promise.get(30, TimeUnit.SECONDS);

        Assertions.assertEquals(addedInboundTrackModels.size(), deletedInboundTrackModels.size());
        for (var deletedClient : deletedInboundTrackModels) {
            var deletedInboundTrack = addedInboundTrackModels.get(deletedClient.getTrackId());
            Assertions.assertNotNull(deletedInboundTrack);
        }
    }


    @Test
    @DisplayName("Scenario: a p2p is added to the hamok. When a call is removed Then corresponding outbound tracks are removed as well and events are triggered")
    void test_4() throws ExecutionException, InterruptedException, TimeoutException {
        modelsMapGenerator.generateP2pCase().saveTo(hamokStorages);

        var addedOutboundTrackModels = modelsMapGenerator.getOutboundTrackModels();
        var callId = modelsMapGenerator.getCallModel().getCallId();
        var promise = new CompletableFuture<List<Models.OutboundTrack>>();
        repositoryEvents.deletedOutboundTrack().subscribe(promise::complete);
//
        this.hamokStorages.getCallsRepository().removeAll(Set.of(callId));
        var deletedOutboundTrackModels = promise.get(30, TimeUnit.SECONDS);

        Assertions.assertEquals(addedOutboundTrackModels.size(), deletedOutboundTrackModels.size());
        for (var deletedClient : deletedOutboundTrackModels) {
            var deletedInboundTrack = addedOutboundTrackModels.get(deletedClient.getTrackId());
            Assertions.assertNotNull(deletedInboundTrack);
        }
    }

    @Test
    @DisplayName("Scenario: a single sfu for 2 participants are added to the hamok. When a call is removed Then corresponding sfu is removed as well and events are triggered")
    void test_5() throws ExecutionException, InterruptedException, TimeoutException {
        modelsMapGenerator.generateSingleSfuCase().saveTo(hamokStorages);

        var addedSfus = modelsMapGenerator.getSfuModels();
        var promise = new CompletableFuture<List<Models.Sfu>>();
        repositoryEvents.deletedSfu().subscribe(promise::complete);
//
        this.hamokStorages.getSfusRepository().deleteAll(addedSfus.keySet());
        this.hamokStorages.getSfusRepository().save();
        var deletedSfus = promise.get(30, TimeUnit.SECONDS);

        Assertions.assertEquals(addedSfus.size(), deletedSfus.size());
        for (var deletedModel : deletedSfus) {
            var addedSfu = addedSfus.get(deletedModel.getSfuId());
            Assertions.assertNotNull(addedSfu);
        }
    }

    @Test
    @DisplayName("Scenario: a single sfu for 2 participants are added to the hamok. When a call is removed Then corresponding sfu transports are removed as well and events are triggered")
    void test_6() throws ExecutionException, InterruptedException, TimeoutException {
        modelsMapGenerator.generateSingleSfuCase().saveTo(hamokStorages);

        var addedSfuTransports = modelsMapGenerator.getSfuTransports();
        var promise = new CompletableFuture<List<Models.SfuTransport>>();
        repositoryEvents.deletedSfuTransports().subscribe(promise::complete);
//
        this.hamokStorages.getSfuTransportsRepository().deleteAll(addedSfuTransports.keySet());
        this.hamokStorages.getSfusRepository().save();
        var deletedSfuTransports = promise.get(30, TimeUnit.SECONDS);

        Assertions.assertEquals(addedSfuTransports.size(), deletedSfuTransports.size());
        for (var deletedModel : deletedSfuTransports) {
            var addedSfuTransport = addedSfuTransports.get(deletedModel.getTransportId());
            Assertions.assertNotNull(addedSfuTransport);
        }
    }

    @Test
    @DisplayName("Scenario: a single sfu for 2 participants are added to the hamok. When a call is removed Then corresponding sfu inbound rtp pads are removed as well and events are triggered")
    void test_7() throws ExecutionException, InterruptedException, TimeoutException {
        modelsMapGenerator.generateSingleSfuCase().saveTo(hamokStorages);

        var addedSfuInboundRtpPads = modelsMapGenerator.getSfuInboundRtpPads();
        var promise = new CompletableFuture<List<Models.SfuInboundRtpPad>>();
        repositoryEvents.deletedSfuInboundRtpPads().subscribe(promise::complete);
//
        this.hamokStorages.getSfuInboundRtpPadsRepository().deleteAll(addedSfuInboundRtpPads.keySet());
        this.hamokStorages.getSfusRepository().save();
        var deletedSfuInboundRtpPads = promise.get(30, TimeUnit.SECONDS);

        Assertions.assertEquals(addedSfuInboundRtpPads.size(), deletedSfuInboundRtpPads.size());
        for (var deletedModel : deletedSfuInboundRtpPads) {
            var addedSfuInboundRtpPad = addedSfuInboundRtpPads.get(deletedModel.getRtpPadId());
            Assertions.assertNotNull(addedSfuInboundRtpPad);
        }
    }


    @Test
    @DisplayName("Scenario: a single sfu for 2 participants are added to the hamok. When a call is removed Then corresponding sfu outbound rtp pads are removed as well and events are triggered")
    void test_8() throws ExecutionException, InterruptedException, TimeoutException {
        modelsMapGenerator.generateSingleSfuCase().saveTo(hamokStorages);

        var addedSfOutboundRtpPads = modelsMapGenerator.getSfuOutboundRtpPads();
        var promise = new CompletableFuture<List<Models.SfuOutboundRtpPad>>();
        repositoryEvents.deletedSfuOutboundRtpPads().subscribe(promise::complete);
//
        this.hamokStorages.getSfuOutboundRtpPadsRepository().deleteAll(addedSfOutboundRtpPads.keySet());
        this.hamokStorages.getSfusRepository().save();
        var deletedSfuOutboundRtpPads = promise.get(30, TimeUnit.SECONDS);

        Assertions.assertEquals(addedSfOutboundRtpPads.size(), deletedSfuOutboundRtpPads.size());
        for (var deletedModel : deletedSfuOutboundRtpPads) {
            var addedSfuInboundRtpPad = addedSfOutboundRtpPads.get(deletedModel.getRtpPadId());
            Assertions.assertNotNull(addedSfuInboundRtpPad);
        }
    }

    @Test
    @DisplayName("When PeerConnectionDTO is added and waited to expire Then corresponding events are triggered")
    void test_9() throws ExecutionException, InterruptedException, TimeoutException {
//        var subject = modelsGenerator.getPeerConnectionModel();
//        var expired = new CompletableFuture<List<RepositoryExpiredEvent<PeerConnectionDTO>>>();
//
//        repositoryEvents.expiredPeerConnection().subscribe(expired::complete);
//
//        this.hamokStorages.getPeerConnections().put(subject.peerConnectionId, subject, 100, TimeUnit.MILLISECONDS);
//        CompletableFuture.allOf(expired).get(30, TimeUnit.SECONDS);
    }

    @Test
    @DisplayName("When PeerConnectionDTO is added and removed Then corresponding events are triggered")
    void test_10() throws ExecutionException, InterruptedException, TimeoutException {
//        var subject = modelsGenerator.getMediaTrackDTO();
//        var added = new CompletableFuture<List<MediaTrackDTO>>();
//        var removed = new CompletableFuture<List<MediaTrackDTO>>();
//
//        repositoryEvents.addedMediaTracks().subscribe(added::complete);
//        repositoryEvents.removedMediaTracks().subscribe(removed::complete);
//
//        this.hamokStorages.getMediaTracks().put(subject.trackId, subject);
//        this.hamokStorages.getMediaTracks().remove(subject.trackId);
//        CompletableFuture.allOf(added, removed).get(30, TimeUnit.SECONDS);
    }

    @Test
    @DisplayName("When PeerConnectionDTO is added and waited to expire Then corresponding events are triggered")
    void test_11() throws ExecutionException, InterruptedException, TimeoutException {
//        var subject = modelsGenerator.getMediaTrackDTO();
//        var expired = new CompletableFuture<List<RepositoryExpiredEvent<MediaTrackDTO>>>();
//
//        repositoryEvents.expiredMediaTracks().subscribe(expired::complete);
//
//        this.hamokStorages.getMediaTracks().put(subject.trackId, subject, 100, TimeUnit.MILLISECONDS);
//        CompletableFuture.allOf(expired).get(30, TimeUnit.SECONDS);
    }


    @Test
    @DisplayName("When SfuRtpPad is added, changed or removed Then corresponding events are triggered")
    void test_12() throws ExecutionException, InterruptedException, TimeoutException {
//        var subject = modelsGenerator.getSfuInboundRtpPad();
//        var added = new CompletableFuture<List<SfuRtpPadDTO>>();
//        var updated = new CompletableFuture<List<RepositoryUpdatedEvent<SfuRtpPadDTO>>>();
//        var removed = new CompletableFuture<List<SfuRtpPadDTO>>();
//
//
//        repositoryEvents.addedSfuRtpPads().subscribe(added::complete);
//        repositoryEvents.updatedSfuRtpPads().subscribe(updated::complete);
//        repositoryEvents.removedSfuRtpPads().subscribe(removed::complete);
//
//        this.hamokStorages.getSFURtpPads().put(subject.rtpPadId, subject);
//        subject.added = Instant.now().getEpochSecond();
//        this.hamokStorages.getSFURtpPads().put(subject.rtpPadId, subject);
//        this.hamokStorages.getSFURtpPads().remove(subject.rtpPadId);
//        CompletableFuture.allOf(added, updated, removed).get(30, TimeUnit.SECONDS);
    }

    @Test
    @DisplayName("When SfuRtpPad is added and waited to expire Then corresponding events are triggered")
    void test_13() throws ExecutionException, InterruptedException, TimeoutException {
//        var subject = modelsGenerator.getSfuInboundRtpPad();
//        var expired = new CompletableFuture<List<RepositoryExpiredEvent<SfuRtpPadDTO>>>();
//
//        repositoryEvents.expiredSfuRtpPads().subscribe(expired::complete);
//
//        this.hamokStorages.getSFURtpPads().put(subject.rtpPadId, subject, 100, TimeUnit.MILLISECONDS);
//        CompletableFuture.allOf(expired).get(30, TimeUnit.SECONDS);
    }

    @Test
    @DisplayName("When SfuTransport is added, changed or removed Then corresponding events are triggered")
    void test_14() throws ExecutionException, InterruptedException, TimeoutException {
//        var subject = modelsGenerator.getSfuTransportModel();
//        var added = new CompletableFuture<List<SfuTransportDTO>>();
//        var updated = new CompletableFuture<List<RepositoryUpdatedEvent<SfuTransportDTO>>>();
//        var removed = new CompletableFuture<List<SfuTransportDTO>>();
//
//
//        repositoryEvents.addedSfuTransports().subscribe(added::complete);
//        repositoryEvents.updatedSfuTransports().subscribe(updated::complete);
//        repositoryEvents.removedSfuTransports().subscribe(removed::complete);
//
//        this.hamokStorages.getSFUTransports().put(subject.transportId, subject);
//        subject.opened = Instant.now().getEpochSecond();
//        this.hamokStorages.getSFUTransports().put(subject.transportId, subject);
//        this.hamokStorages.getSFUTransports().remove(subject.transportId);
//        CompletableFuture.allOf(added, updated, removed).get(30, TimeUnit.SECONDS);
    }

    @Test
    @DisplayName("When SfuTransport is added and waited to expire Then corresponding events are triggered")
    void test_15() throws ExecutionException, InterruptedException, TimeoutException {
//        var subject = modelsGenerator.getSfuTransportModel();
//        var expired = new CompletableFuture<List<RepositoryExpiredEvent<SfuTransportDTO>>>();
//
//        repositoryEvents.expiredSfuTransports().subscribe(expired::complete);
//
//        this.hamokStorages.getSFUTransports().put(subject.transportId, subject, 100, TimeUnit.MILLISECONDS);
//        CompletableFuture.allOf(expired).get(30, TimeUnit.SECONDS);
    }


    @Test
    @DisplayName("When SfuDTO is added, changed or removed Then corresponding events are triggered")
    void test_16() throws ExecutionException, InterruptedException, TimeoutException {
//        var subject = modelsGenerator.getSfuModel();
//        var added = new CompletableFuture<List<SfuDTO>>();
//        var removed = new CompletableFuture<List<SfuDTO>>();
//
//
//        repositoryEvents.addedSfu().subscribe(added::complete);
//        repositoryEvents.removedSfu().subscribe(removed::complete);
//
//        this.hamokStorages.getSFUs().put(subject.sfuId, subject);
//        subject.joined = Instant.now().getEpochSecond();
//        this.hamokStorages.getSFUs().put(subject.sfuId, subject);
//        this.hamokStorages.getSFUs().remove(subject.sfuId);
//        CompletableFuture.allOf(added, removed).get(30, TimeUnit.SECONDS);
    }

    @Test
    @DisplayName("When SfuDTO is added and waited to expire Then corresponding events are triggered")
    void test_17() throws ExecutionException, InterruptedException, TimeoutException {
//        var subject = modelsGenerator.getSfuModel();
//        var expired = new CompletableFuture<List<RepositoryExpiredEvent<SfuDTO>>>();
//
//        repositoryEvents.expiredSfu().subscribe(expired::complete);
//
//        this.hamokStorages.getSFUs().put(subject.sfuId, subject, 100, TimeUnit.MILLISECONDS);
//        CompletableFuture.allOf(expired).get(30, TimeUnit.SECONDS);
    }
}