package org.observertc.webrtc.observer.evaluators;

import java.util.Iterator;
import org.observertc.webrtc.observer.samples.WebExtrAppSample;
import org.observertc.webrtc.observer.dto.webextrapp.RTCStats;

public class WebExtrAppSampleIteratorProvider {

	public static Iterator<RTCStats> RTCStatsIt(WebExtrAppSample sample) {
		return new Iterator<RTCStats>() {
			private int index = 0;
			private boolean finishedSenders = false;
			private RTCStats[] rtcStats = sample.peerConnectionSample.getSenderStats();

			@Override
			public boolean hasNext() {
				if (rtcStats == null) {
					if (this.finishedSenders) {
						return false;
					}
					this.finishedSenders = true;
					this.rtcStats = sample.peerConnectionSample.getReceiverStats();
					return this.hasNext();
				}
				if (this.index < rtcStats.length) {
					return true;
				}
				if (!this.finishedSenders) {
					this.rtcStats = sample.peerConnectionSample.getReceiverStats();
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
}
