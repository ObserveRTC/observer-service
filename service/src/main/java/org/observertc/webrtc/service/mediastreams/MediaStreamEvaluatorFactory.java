package org.observertc.webrtc.service.mediastreams;//package com.observertc.gatekeeper.webrtcstat.processors.samples;

import javax.inject.Singleton;
import org.observertc.webrtc.service.repositories.CallPeerConnectionsRepository;
import org.observertc.webrtc.service.repositories.PeerConnectionSSRCsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class MediaStreamEvaluatorFactory {

	private static final Logger logger = LoggerFactory.getLogger(MediaStreamEvaluatorFactory.class);
	private final MediaStreamEvaluatorConfiguration configuration;
	private final PeerConnectionSSRCsRepository peerConnectionSSRCsRepository;
	private final CallPeerConnectionsRepository callPeerConnectionsRepository;

	public MediaStreamEvaluatorFactory(
			PeerConnectionSSRCsRepository peerConnectionSSRCsRepository,
			CallPeerConnectionsRepository callPeerConnectionsRepository,
			MediaStreamEvaluatorConfiguration configuration
	) {
		this.configuration = configuration;
		this.callPeerConnectionsRepository = callPeerConnectionsRepository;
		this.peerConnectionSSRCsRepository = peerConnectionSSRCsRepository;
	}

	public CallsEvaluator makeCallsEvaluator() {
//		CallsReporter callsReporter = this.makeCallsReporter();
//		new CallsEvaluator(this.peerConnectionSSRCsRepository)
		return null;
	}

	private CallsReporter makeCallsReporter() {
//		new CallsReporter(this.peerConnectionSSRCsRepository, this.callPeerConnectionsRepository)
		return null;
	}


}
