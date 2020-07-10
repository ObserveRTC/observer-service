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
		public int runPeriodInS;
	}
}

