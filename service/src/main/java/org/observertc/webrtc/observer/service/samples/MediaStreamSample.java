package org.observertc.webrtc.observer.service.samples;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import io.micronaut.core.annotation.Introspected;
import java.time.LocalDateTime;

@Introspected
public class MediaStreamSample {

	@JsonUnwrapped
	public SampleDescription RTTInMs = new SampleDescription();
	@JsonUnwrapped
	public SampleDescription bytesSent = new SampleDescription();
	@JsonUnwrapped
	public SampleDescription bytesReceived = new SampleDescription();
	@JsonUnwrapped
	public SampleDescription packetsReceived = new SampleDescription();
	@JsonUnwrapped
	public SampleDescription packetsSent = new SampleDescription();
	@JsonUnwrapped
	public SampleDescription packetsLost = new SampleDescription();

	@JsonIgnore
	public LocalDateTime first;

	@JsonIgnore
	public LocalDateTime last;

	public MediaStreamSample(MediaStreamSample src) {
		this.RTTInMs = new SampleDescription(src.RTTInMs);
		this.bytesSent = new SampleDescription(src.bytesSent);
		this.bytesReceived = new SampleDescription(src.bytesReceived);
		this.packetsSent = new SampleDescription(src.packetsSent);
		this.packetsLost = new SampleDescription(src.packetsLost);
		this.first = src.first;
		this.last = src.last;
	}

	public MediaStreamSample() {
		
	}
}
