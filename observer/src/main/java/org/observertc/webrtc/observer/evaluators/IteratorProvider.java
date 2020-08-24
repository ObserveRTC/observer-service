package org.observertc.webrtc.observer.evaluators;

import java.util.Iterator;
import javax.inject.Singleton;
import org.observertc.webrtc.observer.dto.webextrapp.PeerConnectionSample;
import org.observertc.webrtc.observer.dto.webextrapp.RTCStats;
import org.observertc.webrtc.observer.samples.WebExtrAppSample;

@Singleton
public class IteratorProvider {

	public static Iterator<RTCStats> makeRTCStatsIt(PeerConnectionSample sample) {
		return new Iterator<RTCStats>() {
			private int index = 0;
			private boolean finishedSenders = false;
			private RTCStats[] rtcStats = sample.getSenderStats();

			@Override
			public boolean hasNext() {
				if (rtcStats == null) {
					if (this.finishedSenders) {
						return false;
					}
					this.finishedSenders = true;
					this.rtcStats = sample.getReceiverStats();
					return this.hasNext();
				}
				if (this.index < rtcStats.length) {
					return true;
				}
				if (!this.finishedSenders) {
					this.rtcStats = sample.getReceiverStats();
					this.finishedSenders = true;
					this.index = 0;
					return this.hasNext();
				}
				return false;
			}

			@Override
			public RTCStats next() {
				RTCStats result = this.rtcStats[this.index];
				++this.index;
				return result;
			}
		};
	}

	public static Iterator<RTCStats> makeRTCStatsIt(WebExtrAppSample sample) {
		return makeRTCStatsIt(sample.peerConnectionSample);
	}
}
