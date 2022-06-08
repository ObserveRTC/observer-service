package org.observertc.schemas.reports.csvsupport;

import java.util.LinkedList;
import org.observertc.observer.reports.Report;
import java.util.function.Function;
import org.observertc.schemas.reports.OutboundAudioTrackReport;

public class OutboundAudioTrackReportToIterable implements Function<Report, Iterable<?>> {
	@Override
	public Iterable<?> apply(Report report) {
		var result = new LinkedList();
		var payload = (OutboundAudioTrackReport) report.payload;
		result.add(payload.serviceId);
		result.add(payload.mediaUnitId);
		result.add(payload.timestamp);
		result.add(payload.callId);
		result.add(payload.clientId);
		result.add(payload.peerConnectionId);
		result.add(payload.sampleSeq);
		result.add(payload.ssrc);
		result.add(payload.audioLevel);
		result.add(payload.averageRtcpInterval);
		result.add(payload.burstDiscardCount);
		result.add(payload.burstDiscardRate);
		result.add(payload.burstLossCount);
		result.add(payload.burstLossRate);
		result.add(payload.burstPacketsDiscarded);
		result.add(payload.burstPacketsLost);
		result.add(payload.bytesDiscardedOnSend);
		result.add(payload.bytesSent);
		result.add(payload.channels);
		result.add(payload.clockRate);
		result.add(payload.echoReturnLoss);
		result.add(payload.echoReturnLossEnhancement);
		result.add(payload.encoderImplementation);
		result.add(payload.ended);
		result.add(payload.fecPacketsSent);
		result.add(payload.fractionLost);
		result.add(payload.gapDiscardRate);
		result.add(payload.gapLossRate);
		result.add(payload.headerBytesSent);
		result.add(payload.jitter);
		result.add(payload.label);
		result.add(payload.lastPacketSentTimestamp);
		result.add(payload.marker);
		result.add(payload.mimeType);
		result.add(payload.nackCount);
		result.add(payload.packetsDiscarded);
		result.add(payload.packetsDiscardedOnSend);
		result.add(payload.packetsLost);
		result.add(payload.packetsReceived);
		result.add(payload.packetsRepaired);
		result.add(payload.packetsSent);
		result.add(payload.payloadType);
		result.add(payload.perDscpPacketsSent);
		result.add(payload.relayedSource);
		result.add(payload.reportsReceived);
		result.add(payload.retransmittedBytesSent);
		result.add(payload.retransmittedPacketsSent);
		result.add(payload.rid);
		result.add(payload.roomId);
		result.add(payload.roundTripTime);
		result.add(payload.roundTripTimeMeasurements);
		result.add(payload.samplesEncodedWithCelt);
		result.add(payload.samplesEncodedWithSilk);
		result.add(payload.sdpFmtpLine);
		result.add(payload.sfuStreamId);
		result.add(payload.targetBitrate);
		result.add(payload.totalAudioEnergy);
		result.add(payload.totalEncodedBytesTarget);
		result.add(payload.totalPacketSendDelay);
		result.add(payload.totalRoundTripTime);
		result.add(payload.totalSamplesDuration);
		result.add(payload.totalSamplesSent);
		result.add(payload.trackId);
		result.add(payload.userId);
		result.add(payload.voiceActivityFlag);

		return result;
	}
}