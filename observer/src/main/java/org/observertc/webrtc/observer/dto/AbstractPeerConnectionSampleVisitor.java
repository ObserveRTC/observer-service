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

package org.observertc.webrtc.observer.dto;

import org.observertc.webrtc.observer.dto.v20200114.PeerConnectionSample;

public abstract class AbstractPeerConnectionSampleVisitor<T> implements PeerConnectionSampleVisitor<T> {


	public void visitRemoteInboundRTP(T obj, PeerConnectionSample sample, PeerConnectionSample.RemoteInboundRTPStreamStats subject) {

	}

	public void visitInboundRTP(T obj, PeerConnectionSample sample, PeerConnectionSample.InboundRTPStreamStats subject) {

	}

	public void visitOutboundRTP(T obj, PeerConnectionSample sample, PeerConnectionSample.OutboundRTPStreamStats subject) {

	}

	public void visitICECandidatePair(T obj, PeerConnectionSample sample, PeerConnectionSample.ICECandidatePair subject) {

	}

	public void visitICELocalCandidate(T obj, PeerConnectionSample sample, PeerConnectionSample.ICELocalCandidate subject) {

	}

	public void visitICERemoteCandidate(T obj, PeerConnectionSample sample, PeerConnectionSample.ICERemoteCandidate subject) {

	}

}
