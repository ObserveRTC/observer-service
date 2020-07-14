package org.observertc.webrtc.service;

import io.micronaut.context.annotation.ConfigurationProperties;

@ConfigurationProperties("reports")
public class ReportsConfig {

	public CallReportsConfig callReports;

	public StreamReportsConfig streamReports;

	@ConfigurationProperties("callReports")
	public static class CallReportsConfig {
		public boolean enabled;
		public int peerConnectionMaxIdleTimeInS;
		public int reportPeriodInS;
		public int updatePeriodInS;
		public int archiveRetentionTimeInDays;
	}

	@ConfigurationProperties("streamReports")
	public static class StreamReportsConfig {
		public boolean enabled;
		public int peerConnectionMaxIdleTimeInS;
		public int aggregationTimeInS;
	}
}

