package org.observertc.webrtc.service.samples;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import io.micronaut.core.annotation.Introspected;
import java.time.LocalDateTime;

@Introspected
public class MediaStreamAggregate {

	@JsonUnwrapped
	public MediaStreamAggregateRecord RTTInMs = new MediaStreamAggregateRecord();
	@JsonUnwrapped
	public MediaStreamAggregateRecord bytesSent = new MediaStreamAggregateRecord();
	@JsonUnwrapped
	public MediaStreamAggregateRecord bytesReceived = new MediaStreamAggregateRecord();
	@JsonUnwrapped
	public MediaStreamAggregateRecord packetsReceived = new MediaStreamAggregateRecord();
	@JsonUnwrapped
	public MediaStreamAggregateRecord packetsSent = new MediaStreamAggregateRecord();
	@JsonUnwrapped
	public MediaStreamAggregateRecord packetsLost = new MediaStreamAggregateRecord();

	@JsonIgnore
	public LocalDateTime first;

	@JsonIgnore
	public LocalDateTime last;

	public MediaStreamAggregate(MediaStreamAggregate src) {
		this.RTTInMs = new MediaStreamAggregateRecord(src.RTTInMs);
		this.bytesSent = new MediaStreamAggregateRecord(src.bytesSent);
		this.bytesReceived = new MediaStreamAggregateRecord(src.bytesReceived);
		this.packetsSent = new MediaStreamAggregateRecord(src.packetsSent);
		this.packetsLost = new MediaStreamAggregateRecord(src.packetsLost);
		this.first = src.first;
		this.last = src.last;
	}

	public MediaStreamAggregate() {

	}

}
