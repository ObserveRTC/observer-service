package org.observertc.webrtc.observer.dto;

import java.util.Arrays;
import java.util.function.BiConsumer;
import org.observertc.webrtc.observer.dto.v20200114.PeerConnectionSample;

public interface PeerConnectionSampleVisitor<T> extends BiConsumer<T, PeerConnectionSample> {

	@Override
	default void accept(T obj, PeerConnectionSample sample) {
		if (sample == null) {
			return;
		}
		for (PeerConnectionSample.RTCStats rtcStats : Arrays.asList(sample.receiverStats, sample.senderStats)) {
			if (rtcStats == null) {
				continue;
			}
			if (rtcStats.remoteInboundRTPStats != null) {
				for (PeerConnectionSample.RemoteInboundRTPStreamStats subject : rtcStats.remoteInboundRTPStats) {
					this.visitRemoteInboundRTP(obj, sample, subject);
				}
			}

			if (rtcStats.inboundRTPStats != null) {
				for (PeerConnectionSample.InboundRTPStreamStats subject : rtcStats.inboundRTPStats) {
					this.visitInboundRTP(obj, sample, subject);
				}
			}

			if (rtcStats.outboundRTPStats != null) {
				for (PeerConnectionSample.OutboundRTPStreamStats subject : rtcStats.outboundRTPStats) {
					this.visitOutboundRTP(obj, sample, subject);
				}
			}
		}
		if (sample.iceStats != null) {
			PeerConnectionSample.ICEStats iceStats = sample.iceStats;
			if (iceStats.candidatePairs != null) {
				for (PeerConnectionSample.ICECandidatePair candidatePair : iceStats.candidatePairs) {
					this.visitICECandidatePair(obj, sample, candidatePair);
				}
			}

			if (iceStats.localCandidates != null) {
				for (PeerConnectionSample.ICELocalCandidate localCandidate : iceStats.localCandidates) {
					this.visitICELocalCandidate(obj, sample, localCandidate);
				}
			}

			if (iceStats.remoteCandidates != null) {
				for (PeerConnectionSample.ICERemoteCandidate remoteCandidate : iceStats.remoteCandidates) {
					this.visitICERemoteCandidate(obj, sample, remoteCandidate);
				}
			}
		}
	}


	void visitRemoteInboundRTP(T obj, PeerConnectionSample sample, PeerConnectionSample.RemoteInboundRTPStreamStats subject);

	void visitInboundRTP(T obj, PeerConnectionSample sample, PeerConnectionSample.InboundRTPStreamStats subject);

	void visitOutboundRTP(T obj, PeerConnectionSample sample, PeerConnectionSample.OutboundRTPStreamStats subject);

	void visitICECandidatePair(T obj, PeerConnectionSample sample, PeerConnectionSample.ICECandidatePair subject);

	void visitICELocalCandidate(T obj, PeerConnectionSample sample, PeerConnectionSample.ICELocalCandidate subject);

	void visitICERemoteCandidate(T obj, PeerConnectionSample sample, PeerConnectionSample.ICERemoteCandidate subject);

}
