package org.observertc.webrtc.observer.repositories.tasks;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.logging.Logger;
import org.junit.platform.commons.logging.LoggerFactory;
import org.observertc.webrtc.observer.dto.*;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@MicronautTest
class FindRemoteClientIdsForMediaTrackIdsTest {

    private static final Logger logger = LoggerFactory.getLogger(FindRemoteClientIdsForMediaTrackIdsTest.class);

    @Inject
    CallMapGenerator callMapGenerator;

    @Inject
    Provider<FindRemoteClientIdsForMediaTrackIds> remoteClientIdsForMediaTrackKeysProvider;

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
    public void shouldFindRemoteKeys() {
        var task = remoteClientIdsForMediaTrackKeysProvider.get();
        var inboundTrackIds = this.createdMediaTrackDTOs.values()
                .stream()
                .filter(e -> e.direction == StreamDirection.INBOUND)
                .map(e -> e.trackId).collect(Collectors.toSet());
        task.whereMediaTrackIds(inboundTrackIds);

        var matches = task.execute().getResult();

        matches.forEach(matchedIds -> {
            var inboundMediaTrackDTO = createdMediaTrackDTOs.get(matchedIds.inboundTrackId);
            var outboundMediaTrackDTO = createdMediaTrackDTOs.get(matchedIds.outboundTrackId);
            Assertions.assertNotNull(inboundMediaTrackDTO);
            Assertions.assertNotNull(outboundMediaTrackDTO);
            Assertions.assertEquals(inboundMediaTrackDTO.ssrc, outboundMediaTrackDTO.ssrc);
        });
        Assertions.assertTrue(0 < matches.size());
    }


}