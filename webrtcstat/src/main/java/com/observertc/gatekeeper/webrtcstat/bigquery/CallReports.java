package com.observertc.gatekeeper.webrtcstat.bigquery;

import java.time.LocalDateTime;
import java.util.UUID;
import javax.inject.Singleton;

@Singleton
public class CallReports {
	private final InitiatedCalls initiatedCalls;
	private final FinishedCalls finishedCalls;
	private final JoinedPeerConnections joinedPeerConnections;
	private final DetachedPeerConnections detachedPeerConnections;
	private final StreamSamples streamSamples;

	public CallReports(InitiatedCalls initiatedCalls, FinishedCalls finishedCalls, JoinedPeerConnections joinedPeerConnections,
					   DetachedPeerConnections detachedPeerConnections, StreamSamples streamSamples) {
		this.initiatedCalls = initiatedCalls;
		this.finishedCalls = finishedCalls;
		this.joinedPeerConnections = joinedPeerConnections;
		this.detachedPeerConnections = detachedPeerConnections;
		this.streamSamples = streamSamples;
	}


	public void initiatedCall(UUID observerUUID, UUID callUUID, LocalDateTime initiated) {
		InitiatedCall initiatedCall = new InitiatedCall();
		initiatedCall.initiated = initiated;
		initiatedCall.callUUID = callUUID;
		initiatedCall.observerUUID = observerUUID;
		this.initiatedCalls.insert(initiatedCall);
	}

	public void finishedCall(UUID observerUUID, UUID callUUID, LocalDateTime finished) {
		FinishedCall finishedCall = new FinishedCall();
		finishedCall.finished = finished;
		finishedCall.observerUUID = observerUUID;
		finishedCall.callUUID = callUUID;
		this.finishedCalls.insert(finishedCall);
	}

	public void joinedPeerConnections(UUID observerUUID, UUID callUUID, UUID peerConnectionUUID, LocalDateTime joined) {
		JoinedPeerConnection joinedPeerConnection = new JoinedPeerConnection();
		joinedPeerConnection.peerConnectionUUID = peerConnectionUUID;
		joinedPeerConnection.joined = joined;
		joinedPeerConnection.callUUID = callUUID;
		joinedPeerConnection.observerUUID = observerUUID;
		this.joinedPeerConnections.insert(joinedPeerConnection);
	}

	public void detachedPeerConnections(UUID observerUUID, UUID callUUID, UUID peerConnectionUUID, LocalDateTime detached) {
		DetachedPeerConnection detachedPeerConnection = new DetachedPeerConnection();
		detachedPeerConnection.peerConnectionUUID = peerConnectionUUID;
		detachedPeerConnection.detached = detached;
		detachedPeerConnection.callUUID = callUUID;
		detachedPeerConnection.observerUUID = observerUUID;
		this.detachedPeerConnections.insert(detachedPeerConnection);
	}

	public void reportStreamSample(StreamSample streamSample) {
		this.streamSamples.insert(streamSample);
	}
}
