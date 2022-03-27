package org.observertc.observer.entities;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.observertc.observer.dto.StreamDirection;
import org.observertc.observer.utils.DTOGenerators;

import javax.inject.Inject;

class PeerConnectionEntityTest {
    @Inject
    DTOGenerators generator;

    @Test
    void shouldHasExpectedValues() {
        var mediaTrackDTO = this.generator.getMediaTrackDTO();
        mediaTrackDTO.direction = StreamDirection.INBOUND;
        var peerConnectionDTO = this.generator.getPeerConnectionDTO();
        var peerConnectionEntity = PeerConnectionEntity.builder()
                .withPeerConnectionDTO(peerConnectionDTO)
                .withInboundMediaTrackDTO(mediaTrackDTO)
                .build();

        boolean hasPeerConnectionDTO = peerConnectionEntity.getPeerConnectionDTO().equals(peerConnectionDTO);
        boolean hasMediaTrackDTO = peerConnectionEntity.getInboundMediaTrackDTOs().get(mediaTrackDTO.trackId).equals(mediaTrackDTO);
        Assertions.assertTrue(hasPeerConnectionDTO);
        Assertions.assertTrue(hasMediaTrackDTO);
    }

    @Test
    void shouldBeEquals() {
        var mediaTrackDTO = this.generator.getMediaTrackDTO();
        var peerConnectionDTO = this.generator.getPeerConnectionDTO();
        var actual = PeerConnectionEntity.builder()
                .withPeerConnectionDTO(peerConnectionDTO)
                .withInboundMediaTrackDTO(mediaTrackDTO)
                .build();
        var expected = PeerConnectionEntity.builder().from(actual).build();

        boolean equals = actual.equals(expected);
        Assertions.assertTrue(equals);
    }
}