package org.observertc.webrtc.common.reports;

import java.time.LocalDateTime;
import java.util.UUID;

public interface MediaStreamSample {

	UUID getPeerConnectionUUID();

	UUID getObserverUUID();

	Long getSSRC();

	MediaStreamSampleRecord getRTTRecord();

	MediaStreamSampleRecord getBytesReceivedRecord();

	MediaStreamSampleRecord getBytesSentRecord();

	MediaStreamSampleRecord getPacketsSentRecord();

	MediaStreamSampleRecord getPacketsReceivedRecord();

	MediaStreamSampleRecord getPacketsLostRecord();

	LocalDateTime getFirstSampleTimestamp();

	LocalDateTime getLastSampleTimestamp();
}
