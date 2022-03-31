package org.observertc.observer.repositories.tasks;

import io.micronaut.context.BeanProvider;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.observertc.observer.repositories.HazelcastMaps;
import org.observertc.observer.utils.DTOMapGenerator;

import java.util.Set;

@MicronautTest
class FindParentalDTOsTaskTest {


    @Inject
    HazelcastMaps hazelcastMaps;

    @Inject
    BeanProvider<FindParentalDTOsTask> findParentalDTOsTaskProvider;

    DTOMapGenerator dtoMapGenerator = new DTOMapGenerator().generateP2pCase();

    @BeforeEach
    void setup() {
        dtoMapGenerator.saveTo(hazelcastMaps);
    }

    @AfterEach
    void teardown() {
        dtoMapGenerator.deleteFrom(hazelcastMaps);
    }


    @Test
    public void findByCallId() {
        var call = dtoMapGenerator.getCallDTO();
        var task = findParentalDTOsTaskProvider.get()
                .whereCallIds(Set.of(call.callId))
                ;

        var report = task.execute().getResult();

        var foundDTO = report.callDTOs.get(call.callId);
        var equals = call.equals(foundDTO);
        Assertions.assertTrue(equals);
    }

    @Test
    public void findByClientIds() {
        var clients = dtoMapGenerator.getClientDTOs();
        var task = findParentalDTOsTaskProvider.get()
                .whereClientIds(clients.keySet())
                ;

        var report = task.execute().getResult();

        for (var client : clients.values()) {

            var foundClient = report.clientDTOs.get(client.clientId);
            boolean equals = client.equals(foundClient);
            Assertions.assertTrue(equals);

            var foundCall = report.callDTOs.get(foundClient.callId);
            Assertions.assertNotNull(foundCall);
        }
    }

    @Test
    public void findByPeerConnectionIds() {
        var peerConnections = dtoMapGenerator.getPeerConnectionDTOs();
        var task = findParentalDTOsTaskProvider.get()
                .wherePeerConnectionIds(peerConnections.keySet())
                ;

        var report = task.execute().getResult();

        for (var peerConnection : peerConnections.values()) {

            var foundPeerConnection = report.peerConnectionDTOs.get(peerConnection.peerConnectionId);
            var equals = peerConnection.equals(foundPeerConnection);
            Assertions.assertTrue(equals);

            var foundClient = report.clientDTOs.get(peerConnection.clientId);
            Assertions.assertNotNull(foundClient);

            var foundCall = report.callDTOs.get(foundClient.callId);
            Assertions.assertNotNull(foundCall);
        }
    }

    @Test
    public void findByTrackIds() {
        var tracks = dtoMapGenerator.getMediaTrackDTOs();
        var task = findParentalDTOsTaskProvider.get()
                .whereTrackIds(tracks.keySet())
                ;

        var report = task.execute().getResult();

        for (var track : tracks.values()) {

            var foundTrack = report.mediaTrackDTOs.get(track.trackId);
            var equals = track.equals(foundTrack);
            Assertions.assertTrue(equals);

            var foundPeerConnection = report.peerConnectionDTOs.get(track.peerConnectionId);
            Assertions.assertNotNull(foundPeerConnection);

            var foundClient = report.clientDTOs.get(track.clientId);
            Assertions.assertNotNull(foundClient);

            var foundCall = report.callDTOs.get(foundClient.callId);
            Assertions.assertNotNull(foundCall);
        }

    }
}