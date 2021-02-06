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

import java.util.Arrays;
import java.util.Objects;
import java.util.function.BiConsumer;

public interface PeerConnectionSampleVisitor<T> extends BiConsumer<T, PeerConnectionSample> {

	@Override
	default void accept(T obj, PeerConnectionSample sample) {
		if (sample == null) {
			return;
		}
		if (sample.userMediaErrors != null) {
			for (PeerConnectionSample.UserMediaError userMediaError : sample.userMediaErrors) {
				this.visitUserMediaError(obj, sample, userMediaError);
			}
		}
		if (Objects.nonNull(sample.extensionStats)) {
			for (int i = 0; i < sample.extensionStats.length; ++i) {
				PeerConnectionSample.ExtensionStat subject = sample.extensionStats[i];
				this.visitExtensionStat(obj, sample, subject);
			}
		}
		for (PeerConnectionSample.RTCStats rtcStats : Arrays.asList(sample.receiverStats, sample.senderStats)) {
			if (Objects.isNull(rtcStats)) {
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

			if (rtcStats.tracks != null) {
				for (PeerConnectionSample.RTCTrackStats subject : rtcStats.tracks) {
					this.visitTrack(obj, sample, subject);
				}
			}

			if (rtcStats.mediaSources != null) {
				for (PeerConnectionSample.MediaSourceStats subject : rtcStats.mediaSources) {
					this.visitMediaSource(obj, sample, subject);
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

		if (sample.clientDetails != null) {
			this.visitClientDetails(obj, sample, sample.clientDetails);
		}

		if (sample.deviceList != null) {
			for (PeerConnectionSample.MediaDeviceInfo deviceInfo : sample.deviceList) {
				this.visitMediaDeviceInfo(obj, sample, deviceInfo);
			}
		}
	}

	void visitExtensionStat(T obj, PeerConnectionSample sample, PeerConnectionSample.ExtensionStat subject);

	void visitUserMediaError(T obj, PeerConnectionSample sample, PeerConnectionSample.UserMediaError userMediaError);

	void visitMediaSource(T obj, PeerConnectionSample sample, PeerConnectionSample.MediaSourceStats subject);

	void visitTrack(T obj, PeerConnectionSample sample, PeerConnectionSample.RTCTrackStats subject);

	void visitRemoteInboundRTP(T obj, PeerConnectionSample sample, PeerConnectionSample.RemoteInboundRTPStreamStats subject);

	void visitInboundRTP(T obj, PeerConnectionSample sample, PeerConnectionSample.InboundRTPStreamStats subject);

	void visitOutboundRTP(T obj, PeerConnectionSample sample, PeerConnectionSample.OutboundRTPStreamStats subject);

	void visitICECandidatePair(T obj, PeerConnectionSample sample, PeerConnectionSample.ICECandidatePair subject);

	void visitICELocalCandidate(T obj, PeerConnectionSample sample, PeerConnectionSample.ICELocalCandidate subject);

	void visitICERemoteCandidate(T obj, PeerConnectionSample sample, PeerConnectionSample.ICERemoteCandidate subject);

	void visitMediaDeviceInfo(T obj, PeerConnectionSample sample, PeerConnectionSample.MediaDeviceInfo deviceInfo);

	void visitClientDetails(T obj, PeerConnectionSample sample, PeerConnectionSample.ClientDetails clientDetails);
}
