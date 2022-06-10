package org.observertc.schemas.reports.csvsupport;

import java.util.LinkedList;
import org.observertc.observer.reports.Report;
import java.util.function.Function;
import org.observertc.schemas.reports.InboundVideoTrackReport;

public class InboundVideoTrackReportToIterable implements Function<Report, Iterable<?>> {
	@Override
	public Iterable<?> apply(Report report) {
		var result = new LinkedList();
		var payload = (InboundVideoTrackReport) report.payload;
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
		result.add(payload.clockRate);
		result.add(payload.decoderImplementation);
		result.add(payload.ended);
		result.add(payload.estimatedPlayoutTimestamp);
		result.add(payload.fecPacketsDiscarded);
		result.add(payload.fecPacketsReceived);
		result.add(payload.firCount);
		result.add(payload.frameBitDepth);
		result.add(payload.frameHeight);
		result.add(payload.framesDecoded);
		result.add(payload.framesDropped);
		result.add(payload.framesPerSecond);
		result.add(payload.framesReceived);
		result.add(payload.frameWidth);
		result.add(payload.fullFramesLost);
		result.add(payload.gapDiscardRate);
		result.add(payload.gapLossRate);
		result.add(payload.headerBytesReceived);
		result.add(payload.jitter);
		result.add(payload.jitterBufferDelay);
		result.add(payload.jitterBufferEmittedCount);
		result.add(payload.keyFramesDecoded);
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
		result.add(payload.partialFramesLost);
		result.add(payload.payloadType);
		result.add(payload.perDscpPacketsReceived);
		result.add(payload.pliCount);
		result.add(payload.qpSum);
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
		result.add(payload.sliCount);
		result.add(payload.totalDecodeTime);
		result.add(payload.totalInterFrameDelay);
		result.add(payload.totalProcessingDelay);
		result.add(payload.totalSquaredInterFrameDelay);
		result.add(payload.trackId);
		result.add(payload.userId);

		return result;
	}
}