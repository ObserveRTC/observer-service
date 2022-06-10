package org.observertc.schemas.reports.csvsupport;

import java.util.LinkedList;
import org.observertc.observer.reports.Report;
import java.util.function.Function;
import org.observertc.schemas.reports.SFUTransportReport;

public class SFUTransportReportToIterable implements Function<Report, Iterable<?>> {
	@Override
	public Iterable<?> apply(Report report) {
		var result = new LinkedList();
		var payload = (SFUTransportReport) report.payload;
		result.add(payload.serviceId);
		result.add(payload.mediaUnitId);
		result.add(payload.sfuId);
		result.add(payload.timestamp);
		result.add(payload.transportId);
		result.add(payload.callId);
		result.add(payload.dtlsState);
		result.add(payload.iceRole);
		result.add(payload.iceState);
		result.add(payload.internal);
		result.add(payload.localAddress);
		result.add(payload.localPort);
		result.add(payload.marker);
		result.add(payload.protocol);
		result.add(payload.remoteAddress);
		result.add(payload.remotePort);
		result.add(payload.roomId);
		result.add(payload.rtpBytesReceived);
		result.add(payload.rtpBytesSent);
		result.add(payload.rtpPacketsLost);
		result.add(payload.rtpPacketsReceived);
		result.add(payload.rtpPacketsSent);
		result.add(payload.rtxBytesReceived);
		result.add(payload.rtxBytesSent);
		result.add(payload.rtxPacketsDiscarded);
		result.add(payload.rtxPacketsLost);
		result.add(payload.rtxPacketsReceived);
		result.add(payload.rtxPacketsSent);
		result.add(payload.sctpBytesReceived);
		result.add(payload.sctpBytesSent);
		result.add(payload.sctpPacketsReceived);
		result.add(payload.sctpPacketsSent);
		result.add(payload.sctpState);

		return result;
	}
}