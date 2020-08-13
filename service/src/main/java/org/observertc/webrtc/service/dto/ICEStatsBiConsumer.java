package org.observertc.webrtc.service.dto;

import org.observertc.webrtc.service.dto.webextrapp.CandidatePair;
import org.observertc.webrtc.service.dto.webextrapp.LocalCandidate;
import org.observertc.webrtc.service.dto.webextrapp.ObserveRTCCIceStats;
import org.observertc.webrtc.service.dto.webextrapp.RemoteCandidate;

public interface ICEStatsBiConsumer<U> {

	default void accept(ObserveRTCCIceStats iceStats, U meta) {
		if (iceStats == null) {
			this.unprocessable(meta, iceStats);
			return;
		}
		CandidatePair[] pairs = iceStats.getIceCandidatePair();
		if (pairs != null) {
			for (int i = 0; i < pairs.length; ++i) {
				CandidatePair candidatePair = pairs[i];
				this.processCandidatePair(meta, candidatePair);
			}
		}
		LocalCandidate[] localCandidates = iceStats.getLocalCandidates();
		if (localCandidates != null) {
			for (int i = 0; i < localCandidates.length; ++i) {
				LocalCandidate localCandidate = localCandidates[i];
				this.processLocalCandidate(meta, localCandidate);
			}
		}
		RemoteCandidate[] remoteCandidates = iceStats.getRemoteCandidates();
		if (remoteCandidates != null) {
			for (int i = 0; i < remoteCandidates.length; ++i) {
				RemoteCandidate remoteCandidate = remoteCandidates[i];
				this.processRemoteCandidate(meta, remoteCandidate);
			}
		}
	}

	default void unprocessable(U meta, ObserveRTCCIceStats iceStatsts) {

	}

	void processRemoteCandidate(U meta, RemoteCandidate remoteCandidate);

	void processLocalCandidate(U meta, LocalCandidate localCandidate);

	void processCandidatePair(U meta, CandidatePair candidatePair);

}
