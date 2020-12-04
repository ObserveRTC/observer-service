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

package org.observertc.webrtc.observer.evaluators.trash;

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

@Deprecated
class PeerConnections {

	private static final Logger logger = LoggerFactory.getLogger(PeerConnections.class);

	private final Map<UUID, MediaStreamUpdate> activePeerConnections = new HashMap<>();
	private final Map<UUID, MediaStreamUpdate> passivePeerConnections = new HashMap<>();
	private final PeerConnectionSampleVisitor<ObservedPCS> SSRCExtractor;
	private Optional<Long> lastUpdateHolder = Optional.empty();

	PeerConnections() {
		this.SSRCExtractor = this.makeSSRCExtractor();
	}

	public void add(UUID peerConnectionUUID, ObservedPCS observedPCS) {
		PeerConnectionSample pcSample = observedPCS.peerConnectionSample;
		if (pcSample == null) {
			logger.warn("Peer connection sample is null");
			return;
		}
		synchronized (this) {
			MediaStreamUpdate mediaStreamUpdate = this.activePeerConnections.get(peerConnectionUUID);
			if (mediaStreamUpdate != null) {
				mediaStreamUpdate.updated = observedPCS.timestamp;
				this.SSRCExtractor.accept(observedPCS, pcSample);
				return;
			}
			mediaStreamUpdate = this.passivePeerConnections.get(peerConnectionUUID);
			if (mediaStreamUpdate != null) {
				mediaStreamUpdate.updated = observedPCS.timestamp;
				this.activePeerConnections.put(peerConnectionUUID, mediaStreamUpdate);
				this.passivePeerConnections.remove(peerConnectionUUID);
				this.SSRCExtractor.accept(observedPCS, pcSample);
				return;
			}
			mediaStreamUpdate = MediaStreamUpdate.of(
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
			this.activePeerConnections.put(peerConnectionUUID, mediaStreamUpdate);
			this.SSRCExtractor.accept(observedPCS, pcSample);
		}
	}

	public LinkedList<MediaStreamUpdate> retrieveActives() {
		LinkedList<MediaStreamUpdate> result = new LinkedList<>();
		Long lastUpdate = null;
		synchronized (this) {
			Iterator<Map.Entry<UUID, MediaStreamUpdate>> it = this.activePeerConnections.entrySet().iterator();
			for (; it.hasNext(); ) {
				Map.Entry<UUID, MediaStreamUpdate> entry = it.next();
				MediaStreamUpdate mediaStreamUpdate = entry.getValue();
				if (lastUpdate == null || lastUpdate < mediaStreamUpdate.updated) {
					lastUpdate = mediaStreamUpdate.updated;
				}
				result.add(entry.getValue());
				this.passivePeerConnections.put(entry.getKey(), entry.getValue());
				it.remove();
			}
			if (lastUpdate != null) {
				this.lastUpdateHolder = Optional.of(lastUpdate);
			}
		}

		return result;
	}

	public LinkedList<UUID> retrieveExpired(int maxIdleTimeInS) {
		LinkedList<UUID> result = new LinkedList<>();
		long threshold;
		synchronized (this) {
			if (this.lastUpdateHolder.isPresent()) {
				threshold = this.lastUpdateHolder.get() - TimeUnit.SECONDS.toMillis(maxIdleTimeInS);
				this.lastUpdateHolder = Optional.empty();
			} else {
				threshold = Instant.now().toEpochMilli() - TimeUnit.SECONDS.toMillis(maxIdleTimeInS);
			}
			Iterator<Map.Entry<UUID, MediaStreamUpdate>> it = this.passivePeerConnections.entrySet().iterator();
			for (; it.hasNext(); ) {
				Map.Entry<UUID, MediaStreamUpdate> entry = it.next();
				UUID peerConnectionUUID = entry.getKey();
				MediaStreamUpdate mediaStreamUpdate = entry.getValue();
				if (mediaStreamUpdate == null) {
					logger.warn("Null mediastreamupdate");
					it.remove();
					continue;
				}
				if (threshold < mediaStreamUpdate.updated) {
					continue;
				}
				result.add(peerConnectionUUID);
				it.remove();
			}

		}
		return result;
	}

	private PeerConnectionSampleVisitor<ObservedPCS> makeSSRCExtractor() {
		return new AbstractPeerConnectionSampleVisitor<ObservedPCS>() {
			@Override
			public void visitRemoteInboundRTP(ObservedPCS obj, PeerConnectionSample sample, PeerConnectionSample.RemoteInboundRTPStreamStats subject) {
				MediaStreamUpdate mediaStreamUpdate = activePeerConnections.get(obj.peerConnectionUUID);
				if (mediaStreamUpdate != null) {
					mediaStreamUpdate.SSRCs.add(subject.ssrc);
				}
			}

			@Override
			public void visitInboundRTP(ObservedPCS obj, PeerConnectionSample sample, PeerConnectionSample.InboundRTPStreamStats subject) {
				MediaStreamUpdate mediaStreamUpdate = activePeerConnections.get(obj.peerConnectionUUID);
				if (mediaStreamUpdate != null) {
					mediaStreamUpdate.SSRCs.add(subject.ssrc);
				}
			}

			@Override
			public void visitOutboundRTP(ObservedPCS obj, PeerConnectionSample sample, PeerConnectionSample.OutboundRTPStreamStats subject) {
				MediaStreamUpdate mediaStreamUpdate = activePeerConnections.get(obj.peerConnectionUUID);
				if (mediaStreamUpdate != null) {
					mediaStreamUpdate.SSRCs.add(subject.ssrc);
				}
			}
		};
	}
}
