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
import org.observertc.webrtc.observer.dto.PeerConnectionDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class PeerConnectionEntity {
	private static final Logger logger = LoggerFactory.getLogger(PeerConnectionEntity.class);

	public static PeerConnectionEntity.Builder builder() {
	    return new Builder();
    }

    public final UUID callUUID;
    public final UUID pcUUID;
    public final UUID serviceUUID;
	public final PeerConnectionDTO peerConnection;
	public final Set<Long> SSRCs;
	public final Set<String> remoteIPs;

    private PeerConnectionEntity(PeerConnectionDTO pcDTO, Set<Long> SSRCs, Set<String> remoteIPs) {
        this.callUUID = pcDTO.callUUID;
        this.pcUUID = pcDTO.peerConnectionUUID;
        this.serviceUUID = pcDTO.serviceUUID;
        this.peerConnection = pcDTO;
        this.SSRCs = SSRCs;
        this.remoteIPs = remoteIPs;
    }

    @Override
    public boolean equals(Object other) {
        if (Objects.isNull(other) || other instanceof PeerConnectionEntity == false) {
            return false;
        }
        PeerConnectionEntity otherPC = (PeerConnectionEntity) other;
        return this.callUUID.equals(otherPC.callUUID) &&
                this.SSRCs.stream().allMatch(otherPC.SSRCs::contains) &&
                otherPC.SSRCs.stream().allMatch(this.SSRCs::contains) &&
                this.pcUUID.equals(otherPC.pcUUID) &&
                this.serviceUUID.equals(otherPC.serviceUUID) &&
                this.peerConnection.equals(otherPC.peerConnection);
    }

    @Override
    public int hashCode() {
        return this.callUUID.hashCode();
    }

    @Override
    public String toString() {
        return ObjectToString.toString(this);
    }

    public static class Builder {

        public PeerConnectionDTO pcDTO = null;
        public Set<Long> SSRCs = new HashSet<>();
        public Set<String> remoteIPs = new HashSet<>();

        public PeerConnectionEntity build() {
            Objects.requireNonNull(this.pcDTO);
            Objects.requireNonNull(this.SSRCs);
            return new PeerConnectionEntity(this.pcDTO,
                    Collections.unmodifiableSet(this.SSRCs),
                    Collections.unmodifiableSet(this.remoteIPs)
            );
        }

        public PeerConnectionEntity.Builder withPCDTO(PeerConnectionDTO pcDTO) {
            this.pcDTO = pcDTO;
            return this;
        }

        public PeerConnectionEntity.Builder withSSRCs(Set<Long> ssrCs) {
            this.SSRCs.addAll(ssrCs);
            return this;
        }

        public PeerConnectionEntity.Builder withRemoteIPs(Collection<String> remoteIPs) {
            this.remoteIPs.addAll(remoteIPs);
            return this;
        }
    }
}
