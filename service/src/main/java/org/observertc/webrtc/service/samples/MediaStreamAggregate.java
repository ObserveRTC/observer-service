package org.observertc.webrtc.service.samples;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import io.micronaut.core.annotation.Introspected;
import java.time.LocalDateTime;

@Introspected
public class MediaStreamAggregate {


	@JsonUnwrapped(prefix = "RTTInMs_")
	public MediaStreamSampleRecord RTTInMs = new MediaStreamSampleRecord();

	@JsonUnwrapped(prefix = "bytesSent_")
	public MediaStreamSampleRecord bytesSent = new MediaStreamSampleRecord();

	@JsonUnwrapped(prefix = "packetsSent_")
	public MediaStreamSampleRecord packetsSent = new MediaStreamSampleRecord();

	@JsonIgnore
	public LocalDateTime first;

	@JsonIgnore
	public LocalDateTime last;


}
