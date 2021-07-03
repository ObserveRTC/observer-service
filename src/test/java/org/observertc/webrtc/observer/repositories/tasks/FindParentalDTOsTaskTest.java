package org.observertc.webrtc.observer.repositories.tasks;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.observertc.webrtc.observer.dto.CallDTO;
import org.observertc.webrtc.observer.dto.ClientDTO;
import org.observertc.webrtc.observer.dto.MediaTrackDTO;
import org.observertc.webrtc.observer.dto.PeerConnectionDTO;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@MicronautTest
class FindParentalDTOsTaskTest {

    @Inject
    CallMapGenerator callMapGenerator;

    @Inject
    Provider<FindParentalDTOsTask> findParentalDTOsTaskProvider;

    private CallDTO createdCallDTO;
    private Map<UUID, ClientDTO> createdClientDTOs;
    private Map<UUID, PeerConnectionDTO> createdPeerConnectionDTOs;
    private Map<UUID, MediaTrackDTO> createdMediaTrackDTOs;

    @BeforeEach
    void setup() {
        this.callMapGenerator.generate();
        this.createdCallDTO = this.callMapGenerator.getCallDTO();
        this.createdClientDTOs = this.callMapGenerator.getClientDTOs();
        this.createdPeerConnectionDTOs = this.callMapGenerator.getPeerConnectionDTOs();
        this.createdMediaTrackDTOs = this.callMapGenerator.getMediaTrackDTOs();
    }

    @Test
    public void findCallByCallId() {
        var task = findParentalDTOsTaskProvider.get()
                .whereCallIds(Set.of(this.createdCallDTO.callId))
                ;

        var report = task.execute().getResult();

        var foundDTO = report.callDTOs.get(this.createdCallDTO.callId);
        Assertions.assertEquals(this.createdCallDTO, foundDTO);
    }

    @Test
    public void findCallByClientId() {
        var task = findParentalDTOsTaskProvider.get()
                .whereClientIds(this.createdClientDTOs.keySet())
                ;

        var report = task.execute().getResult();

        var foundDTO = report.callDTOs.get(this.createdCallDTO.callId);
        Assertions.assertEquals(this.createdCallDTO, foundDTO);
    }

    @Test
    public void findCallByPeerConnectionId() {
        var task = findParentalDTOsTaskProvider.get()
                .wherePeerConnectionIds(this.createdPeerConnectionDTOs.keySet())
                ;

        var report = task.execute().getResult();

        var foundDTO = report.callDTOs.get(this.createdCallDTO.callId);
        Assertions.assertEquals(this.createdCallDTO, foundDTO);
    }

    @Test
    public void findCallByTrackId() {
        var task = findParentalDTOsTaskProvider.get()
                .whereTrackIds(this.createdMediaTrackDTOs.keySet())
                ;

        var report = task.execute().getResult();

        var foundDTO = report.callDTOs.get(this.createdCallDTO.callId);
        Assertions.assertEquals(this.createdCallDTO, foundDTO);
    }

    @Test
    public void findClientsByClientIds() {
        var task = findParentalDTOsTaskProvider.get()
                .whereClientIds(this.createdClientDTOs.keySet())
                ;

        var report = task.execute().getResult();

        this.createdClientDTOs.forEach((clientId, foundDTO) -> {
            var createdDTO = report.clientDTOs.get(clientId);
            Assertions.assertEquals(createdDTO, foundDTO);
        });
        Assertions.assertEquals(report.clientDTOs.size(), this.createdClientDTOs.size());
    }

    @Test
    public void findClientsByPeerConnectionIds() {
        var task = findParentalDTOsTaskProvider.get()
                .wherePeerConnectionIds(this.createdPeerConnectionDTOs.keySet())
                ;

        var report = task.execute().getResult();

        this.createdClientDTOs.forEach((clientId, foundDTO) -> {
            var createdDTO = report.clientDTOs.get(clientId);
            Assertions.assertEquals(createdDTO, foundDTO);
        });
        Assertions.assertEquals(report.clientDTOs.size(), this.createdClientDTOs.size());
    }

    @Test
    public void findClientsByTrackIds() {
        var task = findParentalDTOsTaskProvider.get()
                .whereTrackIds(this.createdMediaTrackDTOs.keySet())
                ;

        var report = task.execute().getResult();

        this.createdClientDTOs.forEach((clientId, foundDTO) -> {
            var createdDTO = report.clientDTOs.get(clientId);
            Assertions.assertEquals(createdDTO, foundDTO);
        });
        Assertions.assertEquals(report.clientDTOs.size(), this.createdClientDTOs.size());
    }

    @Test
    public void findPeerConnectionsByPeerConnectionIds() {
        var task = findParentalDTOsTaskProvider.get()
                .wherePeerConnectionIds(this.createdPeerConnectionDTOs.keySet())
                ;

        var report = task.execute().getResult();

        this.createdPeerConnectionDTOs.forEach((peerConnectionId, foundDTO) -> {
            var createdDTO = report.peerConnectionDTOs.get(peerConnectionId);
            Assertions.assertEquals(createdDTO, foundDTO);
        });
        Assertions.assertEquals(report.peerConnectionDTOs.size(), this.createdPeerConnectionDTOs.size());
    }

    @Test
    public void findPeerConnectionsByTrackIds() {
        var task = findParentalDTOsTaskProvider.get()
                .whereTrackIds(this.createdMediaTrackDTOs.keySet())
                ;

        var report = task.execute().getResult();

        this.createdPeerConnectionDTOs.forEach((peerConnectionId, foundDTO) -> {
            var createdDTO = report.peerConnectionDTOs.get(peerConnectionId);
            Assertions.assertEquals(createdDTO, foundDTO);
        });
        Assertions.assertEquals(report.peerConnectionDTOs.size(), this.createdPeerConnectionDTOs.size());
    }

    @Test
    public void findMediaTracksByTrackIds() {
        var task = findParentalDTOsTaskProvider.get()
                .whereTrackIds(this.createdMediaTrackDTOs.keySet())
                ;

        var report = task.execute().getResult();

        this.createdMediaTrackDTOs.forEach((trackId, foundDTO) -> {
            var createdDTO = report.mediaTrackDTOs.get(trackId);
            Assertions.assertEquals(createdDTO, foundDTO);
        });
        Assertions.assertEquals(report.mediaTrackDTOs.size(), this.createdMediaTrackDTOs.size());
    }


    /**
     * THis negative test is placed here, because easyrandom do not give random
     * in terms of UUID if you do not specify a randomizer
     */
    @Test
    public void dontFindCallByMisplacedWhereCondition() {
        var task = findParentalDTOsTaskProvider.get()
                .wherePeerConnectionIds(this.createdMediaTrackDTOs.keySet())
                ;

        var report = task.execute().getResult();

        var foundDTO = report.callDTOs.get(this.createdCallDTO.callId);
        Assertions.assertNull(foundDTO);
    }
}