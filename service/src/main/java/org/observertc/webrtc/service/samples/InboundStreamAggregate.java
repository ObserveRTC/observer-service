package org.observertc.webrtc.service.samples;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import io.micronaut.core.annotation.Introspected;
import java.time.LocalDateTime;
import java.util.UUID;

@Introspected
public class InboundStreamAggregate {

	public int count = 0;
	public UUID peerConnectionUUID;

	@JsonUnwrapped(prefix = "bytesReceived")
	public MediaStreamSampleRecord bytesReceived = new MediaStreamSampleRecord();
	@JsonUnwrapped(prefix = "packetsReceived")
	public MediaStreamSampleRecord packetsReceived = new MediaStreamSampleRecord();
	@JsonUnwrapped(prefix = "packetsLost")
	public MediaStreamSampleRecord packetsLost = new MediaStreamSampleRecord();

	@JsonSerialize(using = LocalDateTimeSerializer.class)
	@JsonDeserialize(using = LocalDateTimeDeserializer.class)
	public LocalDateTime firstSample;

	@JsonSerialize(using = LocalDateTimeSerializer.class)
	@JsonDeserialize(using = LocalDateTimeDeserializer.class)
	public LocalDateTime lastSample;

}
