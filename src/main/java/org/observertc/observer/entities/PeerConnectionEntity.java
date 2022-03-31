/*
 * Copyright  2020 Balazs Kreith
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.observertc.observer.entities;

import org.observertc.observer.common.JsonUtils;
import org.observertc.observer.dto.MediaTrackDTO;
import org.observertc.observer.dto.PeerConnectionDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Stream;

public class PeerConnectionEntity implements Iterable<MediaTrackDTO> {
	private static final Logger logger = LoggerFactory.getLogger(PeerConnectionEntity.class);

	public static PeerConnectionEntity.Builder builder() {
	    return new Builder();
    }
    public static PeerConnectionEntity from(PeerConnectionDTO peerConnectionDTO, Map<UUID, MediaTrackDTO> mediaTrackDTOMap) {
        var result = new PeerConnectionEntity();
        result.peerConnectionDTO = peerConnectionDTO;
        for (var mediaTrackDTO : mediaTrackDTOMap.values()) {
            if (mediaTrackDTO.peerConnectionId != mediaTrackDTO.peerConnectionId) continue;
            switch (mediaTrackDTO.direction) {
                case INBOUND:
                    result.inboundTracks.put(mediaTrackDTO.trackId, mediaTrackDTO);
                    break;
                case OUTBOUND:
                    result.outboundTracks.put(mediaTrackDTO.trackId, mediaTrackDTO);
                    break;
            }
        }
        return result;
    }

    private PeerConnectionDTO peerConnectionDTO;
	private Map<UUID, MediaTrackDTO> outboundTracks = new HashMap<>();
	private Map<UUID, MediaTrackDTO> inboundTracks = new HashMap<>();

	PeerConnectionEntity() {

	}



    public UUID getPeerConnectionId() {
	    return this.peerConnectionDTO.peerConnectionId;
    }

    @Override
    public boolean equals(Object other) {
        if (Objects.isNull(other) || other instanceof PeerConnectionEntity == false) {
            return false;
        }
        PeerConnectionEntity otherPC = (PeerConnectionEntity) other;
        return this.peerConnectionDTO.equals(otherPC.peerConnectionDTO);
    }

    public PeerConnectionDTO getPeerConnectionDTO() {
        return this.peerConnectionDTO;
    }

    @Override
    public int hashCode() {
        return this.peerConnectionDTO.hashCode();
    }

    @Override
    public String toString() {
        return JsonUtils.objectToString(this);
    }

    public Map<UUID, MediaTrackDTO> getInboundMediaTrackDTOs() {
        return this.inboundTracks;
    }

    public Map<UUID, MediaTrackDTO> getOutboundMediaTrackDTOs() {
	    return this.outboundTracks;
    }

    @Override
    public Iterator<MediaTrackDTO> iterator() {
        var stream = Stream.concat(this.inboundTracks.values().stream(), this.outboundTracks.values().stream());
        return stream.iterator();
    }

    public static class Builder {

        private final PeerConnectionEntity result = new PeerConnectionEntity();

        public PeerConnectionEntity.Builder from(PeerConnectionEntity source) {
            return this.withPeerConnectionDTO(source.getPeerConnectionDTO())
                    .withInboundMediaTrackDTOs(source.inboundTracks)
                    .withOutboundMediaTrackDTOs(source.outboundTracks)
                    ;
        }

        public PeerConnectionEntity build() {
            Objects.requireNonNull(this.result.peerConnectionDTO);
            return this.result;
        }

        public PeerConnectionEntity.Builder withPeerConnectionDTO(PeerConnectionDTO peerConnectionDTO) {
            this.result.peerConnectionDTO = peerConnectionDTO;
            return this;
        }

        public PeerConnectionEntity.Builder withOutboundMediaTrackDTOs(Map<UUID, MediaTrackDTO> mediaTrackDTOs) {
            this.result.outboundTracks.putAll(mediaTrackDTOs);
            return this;
        }

        public PeerConnectionEntity.Builder withInboundMediaTrackDTOs(Map<UUID, MediaTrackDTO> mediaTrackDTOs) {
            this.result.inboundTracks.putAll(mediaTrackDTOs);
            return this;
        }

        public PeerConnectionEntity.Builder withInboundMediaTrackDTO(MediaTrackDTO mediaTrackDTO) {
            this.result.inboundTracks.put(mediaTrackDTO.trackId, mediaTrackDTO);
            return this;
        }

        public PeerConnectionEntity.Builder withOutboundMediaTrackDTO(MediaTrackDTO mediaTrackDTO) {
            this.result.outboundTracks.put(mediaTrackDTO.trackId, mediaTrackDTO);
            return this;
        }
    }
}
