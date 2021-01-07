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

package org.observertc.webrtc.observer.evaluators;

import org.observertc.webrtc.observer.dto.AbstractPeerConnectionSampleVisitor;
import org.observertc.webrtc.observer.dto.PeerConnectionSampleVisitor;
import org.observertc.webrtc.observer.dto.v20200114.PeerConnectionSample;
import org.observertc.webrtc.observer.samples.ObservedPCS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.*;

class PCStates {

	private static final Logger logger = LoggerFactory.getLogger(PCStates.class);

	private final Map<UUID, PCState> activePCStates = new HashMap<>();
	private final Map<UUID, PCState> passivePCStates = new HashMap<>();
	private final PeerConnectionSampleVisitor<ObservedPCS> SSRCExtractor;
	private int maxIdleTimeInS;

	PCStates(int maxIdleTimeInS) {
		this.SSRCExtractor = this.makeSSRCExtractor();
		this.maxIdleTimeInS = maxIdleTimeInS;
	}

	public void add(UUID peerConnectionUUID, ObservedPCS observedPCS) {
		PeerConnectionSample pcSample = observedPCS.peerConnectionSample;
		if (pcSample == null) {
			logger.warn("Peer connection sample is null");
			return;
		}
		synchronized (this) {
			PCState pcState = this.activePCStates.get(peerConnectionUUID);
			if (pcState != null) {
				pcState.touched = Instant.now();
				pcState.updated = observedPCS.timestamp;
				this.SSRCExtractor.accept(observedPCS, pcSample);
				return;
			}
			pcState = this.passivePCStates.get(peerConnectionUUID);
			if (pcState != null) {
				pcState.touched = Instant.now();
				pcState.updated = observedPCS.timestamp;
				this.activePCStates.put(peerConnectionUUID, pcState);
				this.passivePCStates.remove(peerConnectionUUID);
				this.SSRCExtractor.accept(observedPCS, pcSample);
				return;
			}
			pcState = PCState.of(
					observedPCS.serviceUUID,
					observedPCS.peerConnectionUUID,
					observedPCS.timestamp,
					pcSample.browserId,
					pcSample.callId,
					observedPCS.timeZoneID,
					pcSample.userId,
					observedPCS.mediaUnitId,
					observedPCS.serviceName,
					observedPCS.marker
			);
			this.activePCStates.put(peerConnectionUUID, pcState);
			this.SSRCExtractor.accept(observedPCS, pcSample);
		}
	}

	public LinkedList<PCState> retrieveActives() {
		LinkedList<PCState> result = new LinkedList<>();
		synchronized (this) {
			Iterator<Map.Entry<UUID, PCState>> it = this.activePCStates.entrySet().iterator();
			for (; it.hasNext(); ) {
				Map.Entry<UUID, PCState> entry = it.next();
				result.add(entry.getValue());
				this.passivePCStates.put(entry.getKey(), entry.getValue());
				it.remove();
			}
		}

		return result;
	}

	public LinkedList<PCState> retrieveExpired() {
		LinkedList<PCState> result = new LinkedList<>();
		final Instant threshold = Instant.now().minusSeconds(this.maxIdleTimeInS);
		synchronized (this) {
			Iterator<Map.Entry<UUID, PCState>> it = this.passivePCStates.entrySet().iterator();
			for (; it.hasNext(); ) {
				Map.Entry<UUID, PCState> entry = it.next();
//				UUID peerConnectionUUID = entry.getKey();
				PCState pcState = entry.getValue();
				if (pcState == null) {
					logger.warn("Null pcState");
					it.remove();
					continue;
				}
				if (threshold.compareTo(pcState.touched) < 0) {
					continue;
				}
				result.add(pcState);
				it.remove();
			}
		}
		return result;
	}

	private PeerConnectionSampleVisitor<ObservedPCS> makeSSRCExtractor() {
		return new AbstractPeerConnectionSampleVisitor<ObservedPCS>() {
			@Override
			public void visitRemoteInboundRTP(ObservedPCS obj, PeerConnectionSample sample, PeerConnectionSample.RemoteInboundRTPStreamStats subject) {
				PCState pcState = activePCStates.get(obj.peerConnectionUUID);
				if (pcState != null) {
					pcState.SSRCs.add(subject.ssrc);
				}
			}

			@Override
			public void visitInboundRTP(ObservedPCS obj, PeerConnectionSample sample, PeerConnectionSample.InboundRTPStreamStats subject) {
				PCState pcState = activePCStates.get(obj.peerConnectionUUID);
				if (pcState != null) {
					pcState.SSRCs.add(subject.ssrc);
				}
			}

			@Override
			public void visitOutboundRTP(ObservedPCS obj, PeerConnectionSample sample, PeerConnectionSample.OutboundRTPStreamStats subject) {
				PCState pcState = activePCStates.get(obj.peerConnectionUUID);
				if (pcState != null) {
					pcState.SSRCs.add(subject.ssrc);
				}
			}
		};
	}
}
