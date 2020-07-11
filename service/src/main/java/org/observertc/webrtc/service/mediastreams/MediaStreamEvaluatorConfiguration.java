package org.observertc.webrtc.service.mediastreams;

import io.micronaut.context.annotation.ConfigurationProperties;

@ConfigurationProperties("mediaStreamEvaluator")
public class MediaStreamEvaluatorConfiguration {

	public CallReportsConfiguration callReports;
	public int reportPeriodInS;

	@ConfigurationProperties("callReports")
	public static class CallReportsConfiguration {
		public boolean enabled;
		public int peerConnectionMaxIdleTimeInS;
		public int reportPeriodInS;
		public int updatePeriodInS;
	}

	@Override
	public String toString() {
		return String.format(
				"reportPeriodInS: %d\n" +
						"CallReportsConfiguration:\n" +
						"\tenabled: %b\n" +
						"\tpeerConnectionMaxIdleTimeInS: %d\n" +
						"\treportPeriodInS: %d\n" +
						"\tupdatePeriodInS: %d\n",
				this.reportPeriodInS,
				this.callReports.enabled,
				this.callReports.peerConnectionMaxIdleTimeInS,
				this.callReports.reportPeriodInS,
				this.callReports.updatePeriodInS);
	}
}

