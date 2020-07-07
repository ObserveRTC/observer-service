package org.observertc.webrtc.common.reportsink;


import org.observertc.webrtc.common.reports.MediaStreamSample;

public interface MediaStreamReports {

	ReportResponse sample(MediaStreamSample mediaStreamSample);
}
