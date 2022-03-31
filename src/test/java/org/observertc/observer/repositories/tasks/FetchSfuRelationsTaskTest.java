package org.observertc.observer.repositories.tasks;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.*;
import org.observertc.observer.dto.SfuSinkDTO;
import org.observertc.observer.dto.SfuStreamDTO;
import org.observertc.observer.repositories.HazelcastMaps;
import org.observertc.observer.repositories.RepositoryEvents;
import org.observertc.observer.repositories.SfuRtpPadToMediaTrackBinder;
import org.observertc.observer.utils.DTOMapGenerator;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

@MicronautTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class FetchSfuRelationsTaskTest {

    @Inject
    HazelcastMaps hazelcastMaps;

    @Inject
    RepositoryEvents repositoryEvents;

    @Inject
    SfuRtpPadToMediaTrackBinder sfuRtpPadToMediaTrackBinder;

    @Inject
    Provider<FetchSfuRelationsTask> fetchSfuRelationsTaskProvider;

    static final DTOMapGenerator dtoMapGenerator = new DTOMapGenerator().generateSingleSfuCase();


    @BeforeEach
    void setup() throws ExecutionException, InterruptedException, TimeoutException {
        var streamsAreAdded = new CompletableFuture<List<SfuStreamDTO>>();
        var sinksAreAdded = new CompletableFuture<List<SfuSinkDTO>>();
        repositoryEvents.addedSfuRtpPads().subscribe(sfuRtpPadToMediaTrackBinder::onSfuRtpPadsAdded);
        repositoryEvents.addedMediaTracks().subscribe(sfuRtpPadToMediaTrackBinder::onMediaTracksAdded);
        repositoryEvents.addedSfuStreams().subscribe(streamsAreAdded::complete);
        repositoryEvents.addedSfuSinks().subscribe(sinksAreAdded::complete);
        dtoMapGenerator.saveTo(hazelcastMaps);
        CompletableFuture.allOf(streamsAreAdded, sinksAreAdded).get(30, TimeUnit.SECONDS);
    }

    @AfterEach
    void teardown() {
        dtoMapGenerator.deleteFrom(hazelcastMaps);
    }

    @Test
    @Order(1)
    @DisplayName("When the hazelcast map has been setup Then relations can be found")
    void shouldFindRelations() {
        var sfuSinkIds = dtoMapGenerator.getSfuRtpPads().values().stream()
                .map(sinkDTO -> sinkDTO.sinkId).filter(Objects::nonNull).collect(Collectors.toSet());
        var sfuStreamIds = dtoMapGenerator.getSfuRtpPads().values().stream()
                .map(sinkDTO -> sinkDTO.streamId).filter(Objects::nonNull).collect(Collectors.toSet());

        var taskResult = fetchSfuRelationsTaskProvider.get()
                .whereSfuRtpPadIds(dtoMapGenerator.getSfuRtpPads().keySet())
                .execute()
                .getResult();
        boolean allStreamFound = sfuStreamIds.stream().allMatch(taskResult.sfuStreams::containsKey);
        boolean allSinkFound = sfuSinkIds.stream().allMatch(taskResult.sfuSinks::containsKey);
        Assertions.assertTrue(allStreamFound);
        Assertions.assertTrue(allSinkFound);
    }
}