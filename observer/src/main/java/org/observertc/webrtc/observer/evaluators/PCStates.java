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

import java.time.Instant;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.observertc.webrtc.observer.dto.AbstractPeerConnectionSampleVisitor;
import org.observertc.webrtc.observer.dto.PeerConnectionSampleVisitor;
import org.observertc.webrtc.observer.dto.v20200114.PeerConnectionSample;
import org.observertc.webrtc.observer.samples.ObservedPCS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class PCStates {

	private static final Logger logger = LoggerFactory.getLogger(PCStates.class);

	private final Map<UUID, PCState> activePCStates = new HashMap<>();
	private final Map<UUID, PCState> passivePCStates = new HashMap<>();
	private final PeerConnectionSampleVisitor<ObservedPCS> SSRCExtractor;
	private Optional<Long> lastUpdateHolder = Optional.empty();
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
				pcState.updated = observedPCS.timestamp;
				this.SSRCExtractor.accept(observedPCS, pcSample);
				return;
			}
			pcState = this.passivePCStates.get(peerConnectionUUID);
			if (pcState != null) {
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
		Long lastUpdate = null;
		synchronized (this) {
			Iterator<Map.Entry<UUID, PCState>> it = this.activePCStates.entrySet().iterator();
			for (; it.hasNext(); ) {
				Map.Entry<UUID, PCState> entry = it.next();
				PCState mediaStreamUpdate = entry.getValue();
				if (lastUpdate == null || lastUpdate < mediaStreamUpdate.updated) {
					lastUpdate = mediaStreamUpdate.updated;
				}
				result.add(entry.getValue());
				this.passivePCStates.put(entry.getKey(), entry.getValue());
				it.remove();
			}
			if (lastUpdate != null) {
				this.lastUpdateHolder = Optional.of(lastUpdate);
			}
		}

		return result;
	}

	public LinkedList<PCState> retrieveExpired() {
		LinkedList<PCState> result = new LinkedList<>();
		long threshold;
		synchronized (this) {
			if (this.lastUpdateHolder.isPresent()) {
				threshold = this.lastUpdateHolder.get() - TimeUnit.SECONDS.toMillis(this.maxIdleTimeInS);
				this.lastUpdateHolder = Optional.empty();
			} else {
				threshold = Instant.now().toEpochMilli() - TimeUnit.SECONDS.toMillis(this.maxIdleTimeInS);
			}
			Iterator<Map.Entry<UUID, PCState>> it = this.passivePCStates.entrySet().iterator();
			for (; it.hasNext(); ) {
				Map.Entry<UUID, PCState> entry = it.next();
//				UUID peerConnectionUUID = entry.getKey();
				PCState pcState = entry.getValue();
				if (pcState == null) {
					logger.warn("Null mediastreamupdate");
					it.remove();
					continue;
				}
				if (threshold < pcState.updated) {
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
