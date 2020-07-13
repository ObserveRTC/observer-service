package org.observertc.webrtc.service.samples;

import io.micronaut.core.annotation.Introspected;

@Introspected
public class MediaStreamSampleRecord {
	public int count;
	public long sum;
	public Integer min = null;
	public Integer max = null;

}