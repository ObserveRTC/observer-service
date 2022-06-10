package org.observertc.schemas.reports.csvsupport;

import java.util.LinkedList;
import org.observertc.observer.reports.Report;
import java.util.function.Function;
import org.observertc.schemas.reports.SfuInboundRtpPadReport;

public class SfuInboundRtpPadReportToIterable implements Function<Report, Iterable<?>> {
	@Override
	public Iterable<?> apply(Report report) {
		var result = new LinkedList();
		var payload = (SfuInboundRtpPadReport) report.payload;
		result.add(payload.serviceId);
		result.add(payload.mediaUnitId);
		result.add(payload.sfuId);
		result.add(payload.timestamp);
		result.add(payload.transportId);
		result.add(payload.sfuStreamId);
		result.add(payload.rtpPadId);
		result.add(payload.ssrc);
		result.add(payload.bytesReceived);
		result.add(payload.callId);
		result.add(payload.clientId);
		result.add(payload.clockRate);
		result.add(payload.fecPacketsDiscarded);
		result.add(payload.fecPacketsReceived);
		result.add(payload.firCount);
		result.add(payload.fractionLost);
		result.add(payload.framesDecoded);
		result.add(payload.framesReceived);
		result.add(payload.internal);
		result.add(payload.jitter);
		result.add(payload.keyFramesDecoded);
		result.add(payload.marker);
		result.add(payload.mediaType);
		result.add(payload.mimeType);
		result.add(payload.nackCount);
		result.add(payload.packetsDiscarded);
		result.add(payload.packetsDuplicated);
		result.add(payload.packetsFailedDecryption);
		result.add(payload.packetsLost);
		result.add(payload.packetsReceived);
		result.add(payload.packetsRepaired);
		result.add(payload.payloadType);
		result.add(payload.pliCount);
		result.add(payload.remoteRtpPadId);
		result.add(payload.remoteSfuId);
		result.add(payload.remoteSinkId);
		result.add(payload.remoteTransportId);
		result.add(payload.rid);
		result.add(payload.roundTripTime);
		result.add(payload.rtcpRrSent);
		result.add(payload.rtcpSrReceived);
		result.add(payload.rtxPacketsDiscarded);
		result.add(payload.rtxPacketsReceived);
		result.add(payload.rtxSsrc);
		result.add(payload.sdpFmtpLine);
		result.add(payload.sliCount);
		result.add(payload.targetBitrate);
		result.add(payload.trackId);
		result.add(payload.voiceActivityFlag);

		return result;
	}
}