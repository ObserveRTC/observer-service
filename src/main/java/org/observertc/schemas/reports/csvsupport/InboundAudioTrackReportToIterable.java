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
		result.add(payload.mediaUnitId);
		result.add(payload.timestamp);
		result.add(payload.callId);
		result.add(payload.clientId);
		result.add(payload.peerConnectionId);
		result.add(payload.sampleSeq);
		result.add(payload.ssrc);
		result.add(payload.averageRtcpInterval);
		result.add(payload.burstDiscardCount);
		result.add(payload.burstDiscardRate);
		result.add(payload.burstLossCount);
		result.add(payload.burstLossRate);
		result.add(payload.burstPacketsDiscarded);
		result.add(payload.burstPacketsLost);
		result.add(payload.bytesReceived);
		result.add(payload.bytesSent);
		result.add(payload.channels);
		result.add(payload.clockRate);
		result.add(payload.decoderImplementation);
		result.add(payload.ended);
		result.add(payload.estimatedPlayoutTimestamp);
		result.add(payload.fecPacketsDiscarded);
		result.add(payload.fecPacketsReceived);
		result.add(payload.gapDiscardRate);
		result.add(payload.gapLossRate);
		result.add(payload.headerBytesReceived);
		result.add(payload.jitter);
		result.add(payload.jitterBufferDelay);
		result.add(payload.jitterBufferEmittedCount);
		result.add(payload.label);
		result.add(payload.lastPacketReceivedTimestamp);
		result.add(payload.marker);
		result.add(payload.mimeType);
		result.add(payload.nackCount);
		result.add(payload.packetsDiscarded);
		result.add(payload.packetsDuplicated);
		result.add(payload.packetsFailedDecryption);
		result.add(payload.packetsLost);
		result.add(payload.packetsReceived);
		result.add(payload.packetsRepaired);
		result.add(payload.packetsSent);
		result.add(payload.payloadType);
		result.add(payload.perDscpPacketsReceived);
		result.add(payload.remoteClientId);
		result.add(payload.remotePeerConnectionId);
		result.add(payload.remoteTimestamp);
		result.add(payload.remoteTrackId);
		result.add(payload.remoteUserId);
		result.add(payload.reportsSent);
		result.add(payload.roomId);
		result.add(payload.sdpFmtpLine);
		result.add(payload.sfuSinkId);
		result.add(payload.sfuStreamId);
		result.add(payload.totalProcessingDelay);
		result.add(payload.trackId);
		result.add(payload.userId);
		result.add(payload.voiceActivityFlag);

		return result;
	}
}