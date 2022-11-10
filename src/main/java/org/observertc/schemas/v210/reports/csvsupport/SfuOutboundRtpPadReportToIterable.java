package org.observertc.schemas.reports.csvsupport;

import java.util.LinkedList;
import org.observertc.observer.reports.Report;
import java.util.function.Function;
import org.observertc.schemas.reports.SfuOutboundRtpPadReport;

public class SfuOutboundRtpPadReportToIterable implements Function<Report, Iterable<?>> {
	@Override
	public Iterable<?> apply(Report report) {
		var result = new LinkedList();
		var payload = (SfuOutboundRtpPadReport) report.payload;
		result.add(payload.serviceId);
		result.add(payload. mediaUnitId);
		result.add(payload. sfuId);
		result.add(payload. timestamp);
		result.add(payload. transportId);
		result.add(payload. sfuStreamId);
		result.add(payload. sfuSinkId);
		result.add(payload. rtpPadId);
		result.add(payload. ssrc);
		result.add(payload. bytesSent);
		result.add(payload. callId);
		result.add(payload. clientId);
		result.add(payload. clockRate);
		result.add(payload. fecPacketsDiscarded);
		result.add(payload. fecPacketsSent);
		result.add(payload. firCount);
		result.add(payload. framesEncoded);
		result.add(payload. framesSent);
		result.add(payload. internal);
		result.add(payload. keyFramesEncoded);
		result.add(payload. marker);
		result.add(payload. mediaType);
		result.add(payload. mimeType);
		result.add(payload. nackCount);
		result.add(payload. packetsDiscarded);
		result.add(payload. packetsDuplicated);
		result.add(payload. packetsFailedEncryption);
		result.add(payload. packetsLost);
		result.add(payload. packetsRetransmitted);
		result.add(payload. packetsSent);
		result.add(payload. payloadType);
		result.add(payload. pliCount);
		result.add(payload. rid);
		result.add(payload. roundTripTime);
		result.add(payload. rtcpRrReceived);
		result.add(payload. rtcpSrSent);
		result.add(payload. rtxPacketsDiscarded);
		result.add(payload. rtxPacketsSent);
		result.add(payload. rtxSsrc);
		result.add(payload. sdpFmtpLine);
		result.add(payload. sliCount);
		result.add(payload. targetBitrate);
		result.add(payload. trackId);
		result.add(payload. voiceActivityFlag);

		return result;
	}
}