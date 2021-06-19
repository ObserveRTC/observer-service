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

package org.observertc.webrtc.observer.entities;

import org.observertc.webrtc.observer.common.ObjectToString;
import org.observertc.webrtc.observer.dto.MediaTrackDTO;
import org.observertc.webrtc.observer.dto.PeerConnectionDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class PeerConnectionEntity {
	private static final Logger logger = LoggerFactory.getLogger(PeerConnectionEntity.class);

	public static PeerConnectionEntity.Builder builder() {
	    return new Builder();
    }

    private PeerConnectionDTO peerConnectionDTO;
	private Map<Long, MediaTrackDTO> outboundTracks = new HashMap<>();
	private Map<Long, MediaTrackDTO> inboundTracks = new HashMap<>();

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
        return ObjectToString.toString(this);
    }

    public Map<Long, MediaTrackDTO> getInboundMediaTrackDTOs() {
        return this.inboundTracks;
    }

    public Map<Long, MediaTrackDTO> getOutboundMediaTrackDTOs() {
	    return this.outboundTracks;
    }

    public static class Builder {

        private final PeerConnectionEntity result = new PeerConnectionEntity();

        public PeerConnectionEntity build() {
            Objects.requireNonNull(this.result.peerConnectionDTO);
            return this.result;
        }

        public PeerConnectionEntity.Builder withPeerConnectionDTO(PeerConnectionDTO peerConnectionDTO) {
            this.result.peerConnectionDTO = peerConnectionDTO;
            return this;
        }

        public PeerConnectionEntity.Builder withOutboundMediaTrackDTOs(Map<Long, MediaTrackDTO> mediaTrackDTOs) {
            this.result.outboundTracks.putAll(mediaTrackDTOs);
            return this;
        }

        public PeerConnectionEntity.Builder withInboundMediaTrackDTOs(Map<Long, MediaTrackDTO> mediaTrackDTOs) {
            this.result.inboundTracks.putAll(mediaTrackDTOs);
            return this;
        }

        public PeerConnectionEntity.Builder withInboundMediaTrackDTO(MediaTrackDTO mediaTrackDTO) {
            this.result.inboundTracks.put(mediaTrackDTO.ssrc, mediaTrackDTO);
            return this;
        }

        public PeerConnectionEntity.Builder withOutboundMediaTrackDTO(MediaTrackDTO mediaTrackDTO) {
            this.result.outboundTracks.put(mediaTrackDTO.ssrc, mediaTrackDTO);
            return this;
        }
    }
}
