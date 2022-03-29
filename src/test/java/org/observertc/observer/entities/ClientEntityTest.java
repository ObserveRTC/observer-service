package org.observertc.observer.entities;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.observertc.observer.utils.DTOGenerators;

class ClientEntityTest {

    DTOGenerators generator = new DTOGenerators();

    @Test
    void shouldHasExpectedValues() {
        var peerConnectionDTO = this.generator.getPeerConnectionDTO();
        var peerConnectionEntity = PeerConnectionEntity.builder()
                .withPeerConnectionDTO(peerConnectionDTO)
                .build();
        var clientDTO = this.generator.getClientDTO();
        var clientEntity = ClientEntity.builder()
                .withPeerConnectionEntity(peerConnectionEntity)
                .withClientDTO(clientDTO)
                .build();

        boolean hasClientDTO = clientEntity.getClientDTO().equals(clientDTO);
        boolean hasPeerConnectionDTO = clientEntity.getPeerConnections().get(peerConnectionEntity.getPeerConnectionId()).getPeerConnectionDTO().equals(peerConnectionDTO);
        Assertions.assertTrue(hasClientDTO);
        Assertions.assertTrue(hasPeerConnectionDTO);
    }

    @Test
    void shouldBeEquals() {
        var peerConnectionDTO = this.generator.getPeerConnectionDTO();
        var peerConnectionEntity = PeerConnectionEntity.builder()
                .withPeerConnectionDTO(peerConnectionDTO)
                .build();
        var clientDTO = this.generator.getClientDTO();
        var actual = ClientEntity.builder()
                .withPeerConnectionEntity(peerConnectionEntity)
                .withClientDTO(clientDTO)
                .build();
        var expected = ClientEntity.builder().from(actual).build();

        boolean equals = actual.equals(expected);
        Assertions.assertTrue(equals);
    }
}