package org.observertc.webrtc.observer.service.reportsink;

import org.observertc.webrtc.observer.service.reports.TrackSample;

public interface TrackReports {

	ReportResponse sample(TrackSample trackSample);
}
