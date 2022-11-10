package org.observertc.schemas.reports.csvsupport;

import java.util.LinkedList;
import org.observertc.observer.reports.Report;
import java.util.function.Function;
import org.observertc.schemas.reports.InboundAudioTrackReport;

public class InboundAudioTrackReportToIterable implements Function<Report, Iterable<?>> {
	@Override
	public Iterable<?> apply(Report report) {
		var result = new LinkedList();
		var payload = (InboundAudioTrackReport) report.payload;
		result.add(payload.serviceId);
		result.add(payload. mediaUnitId);
		result.add(payload. timestamp);
		result.add(payload. callId);
		result.add(payload. clientId);
		result.add(payload. peerConnectionId);
		result.add(payload. sampleSeq);
		result.add(payload. ssrc);
		result.add(payload. audioLevel);
		result.add(payload. bytesReceived);
		result.add(payload. bytesSent);
		result.add(payload. concealedSamples);
		result.add(payload. concealmentEvents);
		result.add(payload. decoderImplementation);
		result.add(payload. estimatedPlayoutTimestamp);
		result.add(payload. fecPacketsDiscarded);
		result.add(payload. fecPacketsReceived);
		result.add(payload. headerBytesReceived);
		result.add(payload. insertedSamplesForDeceleration);
		result.add(payload. jitter);
		result.add(payload. jitterBufferDelay);
		result.add(payload. jitterBufferEmittedCount);
		result.add(payload. jitterBufferMinimumDelay);
		result.add(payload. jitterBufferTargetDelay);
		result.add(payload. label);
		result.add(payload. lastPacketReceivedTimestamp);
		result.add(payload. marker);
		result.add(payload. nackCount);
		result.add(payload. packetsDiscarded);
		result.add(payload. packetsLost);
		result.add(payload. packetsReceived);
		result.add(payload. packetsSent);
		result.add(payload. remoteClientId);
		result.add(payload. remotePeerConnectionId);
		result.add(payload. remoteTimestamp);
		result.add(payload. remoteTrackId);
		result.add(payload. remoteUserId);
		result.add(payload. removedSamplesForAcceleration);
		result.add(payload. reportsSent);
		result.add(payload. roomId);
		result.add(payload. roundTripTime);
		result.add(payload. roundTripTimeMeasurements);
		result.add(payload. sfuSinkId);
		result.add(payload. sfuStreamId);
		result.add(payload. silentConcealedSamples);
		result.add(payload. synthesizedSamplesDuration);
		result.add(payload. synthesizedSamplesEvents);
		result.add(payload. totalAudioEnergy);
		result.add(payload. totalPlayoutDelay);
		result.add(payload. totalProcessingDelay);
		result.add(payload. totalRoundTripTime);
		result.add(payload. totalSamplesCount);
		result.add(payload. totalSamplesDuration);
		result.add(payload. totalSamplesReceived);
		result.add(payload. trackId);
		result.add(payload. userId);

		return result;
	}
}