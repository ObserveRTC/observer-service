package org.observertc.observer.repositories.tasks;

import io.micronaut.context.BeanProvider;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.*;
import org.observertc.observer.dto.*;
import org.observertc.observer.repositories.HamokStorages;
import org.observertc.observer.repositories.RepositoryEvents;
import org.observertc.observer.utils.DTOMapGenerator;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

@MicronautTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class FetchTracksRelationsForP2PTaskTest {
    @Inject
    HamokStorages hazelcastMaps;

    @Inject
    RepositoryEvents repositoryEvents;

    @Inject
    BeanProvider<FetchTracksRelationsTask> fetchTracksRelationsTaskProvider;

    static final DTOMapGenerator dtoMapGenerator = new DTOMapGenerator().generateP2pCase();


    @BeforeEach
    void setup() throws ExecutionException, InterruptedException, TimeoutException {
        var tracksAreAdded = new CompletableFuture<List<MediaTrackDTO>>();
        var peerConnectionsAreAdded = new CompletableFuture<List<PeerConnectionDTO>>();
        var clientsAreAdded = new CompletableFuture<List<ClientDTO>>();
        var callIsAdded = new CompletableFuture<List<CallDTO>>();
        repositoryEvents.addedMediaTracks().subscribe(tracksAreAdded::complete);
        repositoryEvents.addedPeerConnection().subscribe(peerConnectionsAreAdded::complete);
        repositoryEvents.addedClients().subscribe(clientsAreAdded::complete);
        repositoryEvents.addedCalls().subscribe(callIsAdded::complete);
        dtoMapGenerator.saveTo(hazelcastMaps);
        CompletableFuture.allOf(tracksAreAdded, peerConnectionsAreAdded, clientsAreAdded, callIsAdded).get(30, TimeUnit.SECONDS);
    }

    @AfterEach
    void teardown() {
        dtoMapGenerator.deleteFrom(hazelcastMaps);
    }

    @Test
    @Order(1)
    @DisplayName("When the hazelcast map has been setup Then relations can be found")
    void shouldFindRelations_1() {
        var inboundMediaTracks = dtoMapGenerator.getMediaTrackDTOs().values().stream().filter(track -> track.direction == StreamDirection.INBOUND).collect(Collectors.toList());
        var outboundMediaTracks = dtoMapGenerator.getMediaTrackDTOs().values().stream().filter(track -> track.direction == StreamDirection.OUTBOUND).collect(Collectors.toMap(
                track -> track.trackId,
                track -> track
        ));

        var inboundTrackIds = inboundMediaTracks.stream().map(track -> track.trackId).collect(Collectors.toSet());
        var taskResult = fetchTracksRelationsTaskProvider.get()
                .whereInboundMediaTrackIds(inboundTrackIds)
                .execute()
                .getResult();

        this.assertReport(inboundMediaTracks, outboundMediaTracks, taskResult);

        for (var inboundMediaTrack : inboundMediaTracks) {

            var match = taskResult.inboundTrackMatchIds.get(inboundMediaTrack.trackId);
            Assertions.assertNotNull(match);

            var outboundTrack = outboundMediaTracks.get(match.outboundTrackId);
            Assertions.assertNotNull(outboundTrack);

            Assertions.assertEquals(match.callId, inboundMediaTrack.callId);
            Assertions.assertEquals(match.callId, outboundTrack.callId);

            Assertions.assertEquals(match.inboundTrackId, inboundMediaTrack.trackId);
            Assertions.assertEquals(match.inboundClientId, inboundMediaTrack.clientId);
            Assertions.assertEquals(match.inboundPeerConnectionId, inboundMediaTrack.peerConnectionId);
            Assertions.assertEquals(match.inboundUserId, inboundMediaTrack.userId);

            Assertions.assertEquals(match.outboundTrackId, outboundTrack.trackId);
            Assertions.assertEquals(match.outboundClientId, outboundTrack.clientId);
            Assertions.assertEquals(match.outboundPeerConnectionId, outboundTrack.peerConnectionId);
            Assertions.assertEquals(match.outboundUserId, outboundTrack.userId);
        }
    }

    @Test
    @Order(2)
    @DisplayName("When match do from cache Then it still works as intended")
    void shouldFindRelations_2() {
        var inboundMediaTracks = dtoMapGenerator.getMediaTrackDTOs().values().stream().filter(track -> track.direction == StreamDirection.INBOUND).collect(Collectors.toList());
        var inboundTrackIds = inboundMediaTracks.stream().map(track -> track.trackId).collect(Collectors.toSet());
        var taskResult_1 = fetchTracksRelationsTaskProvider.get()
                .whereInboundMediaTrackIds(inboundTrackIds)
                .execute()
                .getResult();

        var taskResult_2 = fetchTracksRelationsTaskProvider.get()
                .whereInboundMediaTrackIds(inboundTrackIds)
                .execute()
                .getResult();

//        var map_1 = taskResult_1.inboundTrackMatchIds;
//        var map_2 = taskResult_2.inboundTrackMatchIds;
        BiConsumer<FetchTracksRelationsTask.Report,  FetchTracksRelationsTask.Report> checkAll = (report_1, report_2) -> {
            var map_1 = report_1.inboundTrackMatchIds;
            var map_2 = report_2.inboundTrackMatchIds;
            for (var expected : map_1.values()) {
                var actual = map_2.get(expected.inboundTrackId);

                Assertions.assertEquals(expected.callId, actual.callId);

                Assertions.assertEquals(expected.inboundTrackId, actual.inboundTrackId);
                Assertions.assertEquals(expected.inboundClientId, actual.inboundClientId);
                Assertions.assertEquals(expected.inboundPeerConnectionId, actual.inboundPeerConnectionId);
                Assertions.assertEquals(expected.inboundUserId, actual.inboundUserId);

                Assertions.assertEquals(expected.outboundTrackId, actual.outboundTrackId);
                Assertions.assertEquals(expected.outboundClientId, actual.outboundClientId);
                Assertions.assertEquals(expected.outboundPeerConnectionId, actual.outboundPeerConnectionId);
                Assertions.assertEquals(expected.outboundUserId, actual.outboundUserId);
            }
        };
        checkAll.accept(taskResult_1, taskResult_2);
        checkAll.accept(taskResult_2, taskResult_1);
    }

    private void assertReport(List<MediaTrackDTO> inboundMediaTracks, Map<UUID, MediaTrackDTO> outboundMediaTracks, FetchTracksRelationsTask.Report taskResult) {

    }
}