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
