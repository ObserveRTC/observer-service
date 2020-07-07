package org.observertc.webrtc.service.samples;

import io.micronaut.core.annotation.Introspected;

@Introspected
public class MediaStreamAggregateRecord {
	public int empty;
	public int presented;
	public long sum;
	public Integer last = null;
	public Integer min = null;
	public Integer max = null;

	public MediaStreamAggregateRecord() {

	}

	public MediaStreamAggregateRecord(MediaStreamAggregateRecord source) {
		this.empty = source.empty;
		this.presented = source.presented;
		this.sum = source.sum;
		this.last = source.last;
		this.min = source.min;
		this.max = source.max;
	}
}