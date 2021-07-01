package org.observertc.webrtc.observer.repositories.tasks;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.observertc.webrtc.observer.common.CallEventType;
import org.observertc.webrtc.observer.dto.CallDTO;
import org.observertc.webrtc.observer.dto.ClientDTO;
import org.observertc.webrtc.observer.dto.MediaTrackDTO;
import org.observertc.webrtc.observer.dto.PeerConnectionDTO;

import javax.inject.Inject;
import java.util.Map;
import java.util.UUID;

@MicronautTest
class CreateCallEventReportsTaskProviderTest {

    @Inject
    CallMapGenerator callMapGenerator;

    @Inject
    CreateCallEventReportsTaskProvider createCallEventReportsTaskProvider;

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
    public void createCallStartedReport() {
        var task = this.createCallEventReportsTaskProvider
                .getCreateCallStartedReportsTask()
                .withDTO(this.createdCallDTO)
                .execute();

        var reports = task.getResult();
        reports.forEach(report -> {
            Assertions.assertEquals(report.getCallId(), this.createdCallDTO.callId.toString());
            Assertions.assertEquals(report.getName(), CallEventType.CALL_STARTED.name());
        });
        Assertions.assertEquals(1, reports.size());
    }

    @Test
    public void createCallEndedReport() {
        var task = this.createCallEventReportsTaskProvider
                .getCreateCallEndedReportsTask()
                .withDTO(this.createdCallDTO)
                .execute();

        var reports = task.getResult();
        reports.forEach(report -> {
            Assertions.assertEquals(report.getCallId(), this.createdCallDTO.callId.toString());
            Assertions.assertEquals(report.getName(), CallEventType.CALL_ENDED.name());
        });
        Assertions.assertEquals(1, reports.size());
    }

    @Test
    public void createClientJoinedReport() {
        var task = this.createCallEventReportsTaskProvider
                .getCreateClientJoinedReportsTask();
        this.createdClientDTOs.values().forEach(task::withDTO);

        var reports = task.execute().getResult();
        reports.forEach(report -> {
            UUID clientId = UUID.fromString(report.getClientId());
            Assertions.assertTrue(this.createdClientDTOs.containsKey(clientId));
            Assertions.assertEquals(report.getName(), CallEventType.CLIENT_JOINED.name());
        });
        Assertions.assertEquals(this.createdClientDTOs.size(), reports.size());
    }

    @Test
    public void createClientLeftReport() {
        var task = this.createCallEventReportsTaskProvider
                .getCreateClientLeftReportsTask();
        this.createdClientDTOs.values().forEach(task::withDTO);

        var reports = task.execute().getResult();
        reports.forEach(report -> {
            UUID clientId = UUID.fromString(report.getClientId());
            Assertions.assertTrue(this.createdClientDTOs.containsKey(clientId));
            Assertions.assertEquals(report.getName(), CallEventType.CLIENT_LEFT.name());
        });
        Assertions.assertEquals(this.createdClientDTOs.size(), reports.size());
    }

    @Test
    public void createPeerConnectionOpenedReport() {
        var task = this.createCallEventReportsTaskProvider
                .getCreatePeerConnectionOpenedReportsTask();
        this.createdPeerConnectionDTOs.values().forEach(task::withDTO);

        var reports = task.execute().getResult();
        reports.forEach(report -> {
            UUID peerConnectionId = UUID.fromString(report.getPeerConnectionId());
            Assertions.assertTrue(this.createdPeerConnectionDTOs.containsKey(peerConnectionId));
            Assertions.assertEquals(report.getName(), CallEventType.PEER_CONNECTION_OPENED.name());
        });
        Assertions.assertEquals(this.createdPeerConnectionDTOs.size(), reports.size());
    }

    @Test
    public void createPeerConnectionClosedReport() {
        var task = this.createCallEventReportsTaskProvider
                .getCreatePeerConnectionClosedReportsTask();
        this.createdPeerConnectionDTOs.values().forEach(task::withDTO);

        var reports = task.execute().getResult();
        reports.forEach(report -> {
            UUID peerConnectionId = UUID.fromString(report.getPeerConnectionId());
            Assertions.assertTrue(this.createdPeerConnectionDTOs.containsKey(peerConnectionId));
            Assertions.assertEquals(report.getName(), CallEventType.PEER_CONNECTION_CLOSED.name());
        });
        Assertions.assertEquals(this.createdPeerConnectionDTOs.size(), reports.size());
    }

    @Test
    public void createMediaTrackAddedReport() {
        var task = this.createCallEventReportsTaskProvider
                .getCreateMediaTrackAddedReportsTask();
        this.createdMediaTrackDTOs.values().forEach(task::withDTO);

        var reports = task.execute().getResult();
        reports.forEach(report -> {
            UUID trackId = UUID.fromString(report.getMediaTrackId());
            Assertions.assertTrue(this.createdMediaTrackDTOs.containsKey(trackId));
            Assertions.assertEquals(report.getName(), CallEventType.MEDIA_TRACK_ADDED.name());
        });
        Assertions.assertEquals(this.createdMediaTrackDTOs.size(), reports.size());
    }

    @Test
    public void createMediaTrackRemovedReport() {
        var task = this.createCallEventReportsTaskProvider
                .getCreateMediaTrackRemovedReportsTask();
        this.createdMediaTrackDTOs.values().forEach(task::withDTO);

        var reports = task.execute().getResult();
        reports.forEach(report -> {
            UUID trackId = UUID.fromString(report.getMediaTrackId());
            Assertions.assertTrue(this.createdMediaTrackDTOs.containsKey(trackId));
            Assertions.assertEquals(report.getName(), CallEventType.MEDIA_TRACK_REMOVED.name());
        });
        Assertions.assertEquals(this.createdMediaTrackDTOs.size(), reports.size());
    }
}
